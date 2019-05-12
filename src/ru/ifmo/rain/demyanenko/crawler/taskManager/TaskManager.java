package ru.ifmo.rain.demyanenko.crawler.taskManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.ifmo.rain.demyanenko.crawler.task.DownloaderTask;
import ru.ifmo.rain.demyanenko.crawler.task.ExtractorTask;
import ru.ifmo.rain.demyanenko.crawler.task.ITask;
import ru.ifmo.rain.demyanenko.crawler.task.ITaskListener;

public class TaskManager implements ITaskManager, ITaskListener {
	private final int limitDownloaderTotal;
	private final int limitExtractorTotal;
	private final int limitTotalPerHost;

	private final Map<String, Integer> nowTotalPerHost;
	private int nowDownloaderTotal;
	private int nowExtractorTotal;

	private final Lock loadingParametersLock;
	private final Condition newTaskOrReduceLoading;

	private final ConcurrentLinkedQueue<ExtractorTask> extractorTasks;
	private final Map<String, Queue<DownloaderTask>> hostNameAndDownloaderTaskMap;
	private int downloaderTaskTotal;

	public TaskManager(int downloaderTotal, int extractorTotal, int totalPerHost) {
		this.limitDownloaderTotal = downloaderTotal;
		this.limitExtractorTotal = extractorTotal;
		this.limitTotalPerHost = totalPerHost;
		this.nowTotalPerHost = new HashMap<>();
		this.nowDownloaderTotal = 0;
		this.nowExtractorTotal = 0;

		this.extractorTasks = new ConcurrentLinkedQueue<>();
		this.hostNameAndDownloaderTaskMap = new HashMap<>();
		this.downloaderTaskTotal = 0;

		this.loadingParametersLock = new ReentrantLock();
		this.newTaskOrReduceLoading = loadingParametersLock.newCondition();
	}

	public void addTask(ITask task) throws UnsupportedTaskException {
		if (task instanceof ExtractorTask) {
			extractorTasks.add((ExtractorTask) task);

			task.addITaskListener(this);

			loadingParametersLock.lock();
			newTaskOrReduceLoading.signal();
			loadingParametersLock.unlock();

			return;
		}

		if (task instanceof DownloaderTask) {

			DownloaderTask downloaderTask = (DownloaderTask) task;
			ifNewHostThenRegister(downloaderTask.getHostName());
			addDownloaderTask(downloaderTask);

			loadingParametersLock.lock();
			newTaskOrReduceLoading.signal();
			loadingParametersLock.unlock();

			return;
		}

		throw new UnsupportedTaskException();
	}

	private void addDownloaderTask(DownloaderTask task) {
		loadingParametersLock.lock();

		String hostName = task.getHostName();
		Queue<DownloaderTask> queue = hostNameAndDownloaderTaskMap.get(hostName);
		queue.add(task);
		downloaderTaskTotal++;
		task.addITaskListener(this);

		loadingParametersLock.unlock();
	}

	private void ifNewHostThenRegister(String hostName) {
		loadingParametersLock.lock();

		if (!hostNameAndDownloaderTaskMap.containsKey(hostName)) {
			hostNameAndDownloaderTaskMap.put(hostName, new LinkedList<>());
		}

		if (!nowTotalPerHost.containsKey(hostName)) {
			nowTotalPerHost.put(hostName, 0);
		}

		loadingParametersLock.unlock();
	}

	private ITask reserveExtractorTask() {
		if (nowExtractorTotal >= limitExtractorTotal || extractorTasks.size() == 0) {
			return null;
		}

		ITask task = extractorTasks.poll();
		nowExtractorTotal++;

		return task;
	}

	private ITask reserveDownloaderTask() {
		if (nowDownloaderTotal >= limitDownloaderTotal || downloaderTaskTotal == 0) {
			return null;
		}

		String hostNameWithTask = null;

		for (Entry<String, Integer> totalPerHost : nowTotalPerHost.entrySet()) {
			String hostName = totalPerHost.getKey();
			if (totalPerHost.getValue() >= limitTotalPerHost) {
				continue;
			}

			if (hostNameAndDownloaderTaskMap.get(hostName).size() == 0) {
				continue;
			}

			hostNameWithTask = hostName;
			break;
		}

		if (hostNameWithTask == null) {
			return null;
		}

		ITask task = hostNameAndDownloaderTaskMap.get(hostNameWithTask).poll();
		nowDownloaderTotal++;
		downloaderTaskTotal--;
		nowTotalPerHost.put(hostNameWithTask, nowTotalPerHost.get(hostNameWithTask) + 1);
		return task;
	}

	@Override
	public ITask getTask() throws InterruptedException {
		loadingParametersLock.lock();
		try {
			while (true) {

				ITask extractorTask = reserveExtractorTask();
				if (extractorTask != null) {
					return extractorTask;
				}

				ITask downloaderTask = reserveDownloaderTask();
				if (downloaderTask != null) {
					return downloaderTask;
				}

				newTaskOrReduceLoading.await();
			}
		} finally {
			loadingParametersLock.unlock();
		}
	}

	@Override
	public void handleTaskWasDone(ITask task) {
		loadingParametersLock.lock();

		if (task instanceof ExtractorTask) {
			nowExtractorTotal--;
		}

		if (task instanceof DownloaderTask) {
			nowDownloaderTotal--;
			DownloaderTask downloaderTask = (DownloaderTask) task;
			decrementNowTotalPerHost(downloaderTask.getHostName());
		}

		newTaskOrReduceLoading.signal();
		loadingParametersLock.unlock();
	}

	private void decrementNowTotalPerHost(String hostName) {
		int total = nowTotalPerHost.get(hostName);
		total--;
		nowTotalPerHost.put(hostName, total);
	}
}
