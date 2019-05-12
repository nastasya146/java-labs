package ru.ifmo.rain.demyanenko.crawler;

import ru.ifmo.rain.demyanenko.crawler.task.ITask;
import ru.ifmo.rain.demyanenko.crawler.taskManager.ITaskManager;

public class Worker implements Runnable {
	private ITaskManager taskManager;
	
	public Worker(ITaskManager taskManager) {
		super();
		this.taskManager = taskManager;
	}

	@Override
	public void run() {
		try {

			while (!Thread.currentThread().isInterrupted()) {
				ITask task = taskManager.getTask();
				task.execute();
			}

		} catch (InterruptedException e) {
			//e.printStackTrace();
			
		}
	}
}
