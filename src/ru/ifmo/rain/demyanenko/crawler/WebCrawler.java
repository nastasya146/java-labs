package ru.ifmo.rain.demyanenko.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import ru.ifmo.rain.demyanenko.crawler.taskCreator.TaskCreator;
import ru.ifmo.rain.demyanenko.crawler.taskCreator.TaskCreatorIsAlreadyUsedException;
import ru.ifmo.rain.demyanenko.crawler.taskManager.ITaskManager;
import ru.ifmo.rain.demyanenko.crawler.taskManager.TaskManager;
import ru.ifmo.rain.demyanenko.crawler.taskManager.UnsupportedTaskException;

public class WebCrawler implements Crawler {

	private Downloader downloader;
	private ITaskManager taskManager;
	private Collection<Thread> workers;

	public static void main(String args[]) throws IOException {
		WebCrawler webCrawler = null;

		if (args.length == 1) {
			webCrawler = new WebCrawler(new CachingDownloader(), Integer.MAX_VALUE, Integer.MAX_VALUE,
					Integer.MAX_VALUE);
		}

		if (args.length == 2) {
			int downloaderTotal = Integer.parseInt(args[1]);
			webCrawler = new WebCrawler(new CachingDownloader(), downloaderTotal, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		if (args.length == 3) {
			int downloaderTotal = Integer.parseInt(args[1]);
			int extractorTotal = Integer.parseInt(args[2]);
			webCrawler = new WebCrawler(new CachingDownloader(), downloaderTotal, extractorTotal, Integer.MAX_VALUE);
		}

		if (args.length == 4) {
			int downloaderTotal = Integer.parseInt(args[1]);
			int extractorTotal = Integer.parseInt(args[2]);
			int totalPerHost = Integer.parseInt(args[3]);
			webCrawler = new WebCrawler(new CachingDownloader(), downloaderTotal, extractorTotal, totalPerHost);
		}

		String url = args[0];
		webCrawler.download(url, Integer.MAX_VALUE);
	}

	public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
		this.downloader = downloader;

		this.taskManager = new TaskManager(downloaders, extractors, perHost);
		
		int workerTotal = defineWorkerTotal(downloaders, extractors);
		this.workers = createWorkerThreads(workerTotal, taskManager);
		runThreads(workers);
	}
	
	private int defineWorkerTotal(int downloaders, int extractors) {
		//int processors = Runtime.getRuntime().availableProcessors();
		if((downloaders > 1000) || (extractors > 1000)) {
			return 80;
		}
		return downloaders + extractors;
	}

	private void runThreads(Collection<Thread> threads) {
		for (Thread thread : threads) {
			thread.start();
		}
	}

	private Collection<Thread> createWorkerThreads(int total, ITaskManager taskManager) {
		Collection<Thread> workers = new ArrayList<>();
		for (int i = 0; i < total; i++) {
			workers.add(new Thread(new Worker(taskManager)));
		}
		return workers;
	}

	@Override
	public void close() {
		for(Thread worker: workers) {
			worker.interrupt();
		}
	}

	@Override
	public Result download(String url, int depth) {
		try {
			
			TaskCreator taskCreator = new TaskCreator(taskManager, this.downloader);
			return taskCreator.download(url, depth);
			
		} catch (InterruptedException | TaskCreatorIsAlreadyUsedException | UnsupportedTaskException e) {
			e.printStackTrace();
		}
		return null;
	}

}
