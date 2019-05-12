package ru.ifmo.rain.demyanenko.crawler.taskCreator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import ru.ifmo.rain.demyanenko.crawler.task.DownloaderTask;
import ru.ifmo.rain.demyanenko.crawler.task.ExtractorTask;
import ru.ifmo.rain.demyanenko.crawler.task.ITask;
import ru.ifmo.rain.demyanenko.crawler.task.ITaskListener;
import ru.ifmo.rain.demyanenko.crawler.taskManager.ITaskManager;
import ru.ifmo.rain.demyanenko.crawler.taskManager.UnsupportedTaskException;

public class TaskCreator implements ITaskListener {
	private final ITaskManager taskManager;
	private final Downloader downloader;
	private final ConcurrentHashMap<String, IOException> errors;
	private final ConcurrentSkipListSet<String> triedToDownloadLinks;
	private final CopyOnWriteArrayList<String> downloadedLinks;
	private final AtomicInteger totalOfWaitedTasks;
	private final Lock lock;
	private final Condition allTasksWereDone;
	private boolean isAlreadyUsed;
	private int limitDepth;

	public TaskCreator(ITaskManager taskManager, Downloader downloader) {
		super();
		this.taskManager = taskManager;
		this.downloader = downloader;
		this.errors = new ConcurrentHashMap<>();
		this.triedToDownloadLinks = new ConcurrentSkipListSet<>();
		this.downloadedLinks = new CopyOnWriteArrayList<>();
		this.totalOfWaitedTasks = new AtomicInteger(0);
		this.lock = new ReentrantLock();
		this.allTasksWereDone = lock.newCondition();
		this.isAlreadyUsed = false;
	}

	public Result download(String url, int depth)
			throws InterruptedException, TaskCreatorIsAlreadyUsedException, UnsupportedTaskException {

		setThatIsAlreadyUsed();

		limitDepth = depth;

		sendFirstTaskToTaskManager(url);

		return waitingForResult();
	}

	private void setThatIsAlreadyUsed() throws TaskCreatorIsAlreadyUsedException {
		if (isAlreadyUsed) {
			throw new TaskCreatorIsAlreadyUsedException();
		}
		isAlreadyUsed = true;
	}

	private void sendFirstTaskToTaskManager(String url) throws UnsupportedTaskException {
		DownloaderTask task = new DownloaderTask(url, downloader, 1);
		subscribeOnTask(task);
		triedToDownloadLinks.add(task.getLink());
		taskManager.addTask(task);
	}

	private Result waitingForResult() throws InterruptedException {
		lock.lock();
		try {

			while (totalOfWaitedTasks.get() != 0) {
				allTasksWereDone.await();
			}

			Result result = new Result(downloadedLinks, errors);
			return result;

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleTaskWasDone(ITask task) {
		try {

			if (task instanceof DownloaderTask) {
				handleDownloaderTaskWasDone((DownloaderTask) task);
			}

			if (task instanceof ExtractorTask) {
				handleExtractorTaskWasDone((ExtractorTask) task);
			}

			lock.lock();
			totalOfWaitedTasks.decrementAndGet();

			ifAllTasksWereDoneThenSignal();
			lock.unlock();

		} catch (UnsupportedTaskException e) {
			e.printStackTrace();
		}
	}

	private void ifAllTasksWereDoneThenSignal() {
		if (totalOfWaitedTasks.get() != 0) {
			return;
		}

		allTasksWereDone.signal();

	}

	private void handleDownloaderTaskWasDone(DownloaderTask task) throws UnsupportedTaskException {
		if (!task.wasExecutedWithoutException()) {
			errors.put(task.getLink(), task.getRaisedException());
			return;
		}

		downloadedLinks.add(task.getLink());

		if (task.getDepth() == limitDepth) {
			return;
		}

		Document document = task.getResult();
		ITask newTask = new ExtractorTask(document, task.getLink(), task.getDepth());
		subscribeOnTask(newTask);
		taskManager.addTask(newTask);
	}

	private void handleExtractorTaskWasDone(ExtractorTask task) throws UnsupportedTaskException {
		if (!task.wasExecutedWithoutException()) {
			errors.put(task.getLink(), task.getRaisedException());
			return;
		}

		List<String> links = task.getResult();
		for (String link : links) {

			if(!ifNotTriedToDownloadThenAdd(link)) {
				continue;
			}

			ITask newTask = new DownloaderTask(link, downloader, task.getDepth() + 1);
			subscribeOnTask(newTask);
			taskManager.addTask(newTask);
		}
	}
	
	private boolean ifNotTriedToDownloadThenAdd(String link) {
		lock.lock();
		if (!triedToDownloadLinks.contains(link)) {
			triedToDownloadLinks.add(link);
			lock.unlock();
			return true;
		} else {
			lock.unlock();
			return false;
		}
	}

	private void subscribeOnTask(ITask task) {
		task.addITaskListener(this);

		totalOfWaitedTasks.incrementAndGet();
	}
}
