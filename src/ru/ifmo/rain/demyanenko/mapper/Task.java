package ru.ifmo.rain.demyanenko.mapper;

import java.util.List;
import java.util.function.Function;

public class Task<T, R> implements ITask<T, R> {
	private T arg;
	private Function<? super T, ? extends R> function;
	private int index;
	private List<R> results;
	private final Object doneLock = new Object();
	private Boolean done;

	public Task(Function<? super T, ? extends R> function, T arg, int index, List<R> results) {
		super();
		this.arg = arg;
		this.function = function;
		this.index = index;
		this.results = results;
		this.done = false;
	}

	public T getArg() {
		return arg;
	}

	public Function<? super T, ? extends R> getFunction() {
		return function;
	}
	
	public void waitIsDone() throws InterruptedException {
		synchronized(doneLock) {
			if(!done) {
				doneLock.wait();
			}
		}
	}
	
	public void setResult(R result) {
		results.set(index, result);
		setDone();
	}
	
	public List<R> getResults() {
		return results;
	}
	
	private void setDone() {
		synchronized(doneLock) {
			done = true;
			doneLock.notifyAll();
		}
	}
}
