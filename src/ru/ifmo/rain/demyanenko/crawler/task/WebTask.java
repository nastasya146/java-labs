package ru.ifmo.rain.demyanenko.crawler.task;

import java.io.IOException;

public abstract class WebTask extends PublisherTask{
	private String link;
	private IOException exception;
	private int depth;
	
	protected WebTask(String link, int depth) {
		this.link = link;
		this.depth = depth;
	}
	
	protected void setRaisedException(IOException exception) {
		this.exception = exception;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public String getLink() {
		return link;
	}
	
	public IOException getRaisedException() {
		return exception;
	}
	
	public boolean wasExecutedWithoutException() {
		return exception == null;
	}
}
