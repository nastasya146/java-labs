package ru.ifmo.rain.demyanenko.crawler.task;

public interface ITask {
	public void execute();
	public void addITaskListener(ITaskListener taskListener);
}
