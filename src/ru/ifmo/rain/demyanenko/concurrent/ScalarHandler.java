package ru.ifmo.rain.demyanenko.concurrent;

import java.util.List;

public abstract class ScalarHandler<T, E> implements Runnable {
	protected List<? extends T> list;
	protected DataStorage<E> storage;
	
	public ScalarHandler(List<? extends T> list, DataStorage<E> data) {
		this.list = list;
		this.storage = data;
	}
	
	public DataStorage<E> getData() {
		return this.storage;
	}
}
