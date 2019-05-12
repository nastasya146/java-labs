package ru.ifmo.rain.demyanenko.crawler.taskManager;

import ru.ifmo.rain.demyanenko.crawler.task.ITask;

public interface ITaskManager {
	ITask getTask() throws InterruptedException;
	void addTask(ITask task) throws UnsupportedTaskException;
}
