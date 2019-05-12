package ru.ifmo.rain.demyanenko.concurrent;

public class DataStorage<T> {
	private T data;
	
	public DataStorage() {
		
	}
	
	public DataStorage(T data) {
		this.data = data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	public T getData() {
		return this.data;
	}
}
