package ru.ifmo.rain.demyanenko.mapper;

import java.util.function.Function;

public interface ITask<T, R> {
	public T getArg();

	public Function<? super T, ? extends R> getFunction();
	
	public void waitIsDone() throws InterruptedException;
	
	public void setResult(R result);
}
