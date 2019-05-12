package ru.ifmo.rain.demyanenko.mapper;

import java.util.function.Function;

public class Worker implements Runnable{
	public ConcurrentPipe<ITask<Object, Object>> tasks;
	
	public Worker(ConcurrentPipe<ITask<Object, Object>> tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		try {
			while(true) {
				ITask<Object, Object> task = tasks.pull();
				
				Function<Object, ? extends Object> function = task.getFunction();
				Object result = function.apply(task.getArg());
				task.setResult(result);
			}
		} catch (InterruptedException e) {
			return;
		}
	}
}
