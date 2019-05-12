package ru.ifmo.rain.demyanenko.crawler.taskCreator;

public class TaskCreatorIsAlreadyUsedException extends Exception{
	public TaskCreatorIsAlreadyUsedException() {
		super("TaskCreator is already used. You have to use TaskCreator only once.");
	}
}
