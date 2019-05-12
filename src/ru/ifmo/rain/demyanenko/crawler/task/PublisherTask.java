package ru.ifmo.rain.demyanenko.crawler.task;

import java.util.ArrayList;
import java.util.Collection;

public abstract class PublisherTask implements ITask{
	protected Collection<ITaskListener> listeners;
	
	public PublisherTask() {
		listeners = new ArrayList<>();
	}
	
	@Override
	public void addITaskListener(ITaskListener taskListener) {
		listeners.add(taskListener);
	}
	
	protected void publishTaskWasDone() {
		for(ITaskListener listener: listeners) {
			listener.handleTaskWasDone(this);
		}
	}
}
