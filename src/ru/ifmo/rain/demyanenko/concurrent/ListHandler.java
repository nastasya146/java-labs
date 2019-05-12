package ru.ifmo.rain.demyanenko.concurrent;

import java.util.List;

public abstract class ListHandler<T, E> implements Runnable {
	protected List<? extends T> list;
	protected DataStorage<List<E>> storage;
	
	public ListHandler(List<? extends T> list, DataStorage<List<E>> data) {
		this.list = list;
		this.storage = data;
	}
}
