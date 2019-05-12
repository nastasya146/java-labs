package ru.ifmo.rain.demyanenko.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class ParallelMapperImpl implements ParallelMapper {
	private Collection<Thread> threads;
	private ConcurrentPipe<ITask<Object, Object>> tasks;
	
	@Override
	public void close() {
		for(Thread thread: threads) {
			thread.interrupt();
		}
	}
	
	public ParallelMapperImpl(int threadTotal) {
		threads = new ArrayList<>();
		tasks = new ConcurrentPipe<>();

		for(int i = 0; i < threadTotal; i++) {
			Thread thread = new Thread(new Worker(tasks));
			threads.add(thread);
			thread.start();
		}
	}
	
	
	@Override
	public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> args)
			throws InterruptedException {
		List<Task<Object, Object>> relatedTasks = null;
		
		synchronized(tasks) {
			relatedTasks = createRelatedTasks(function, args);
			tasks.push(relatedTasks);
		}
		
		for(Task<Object, Object> task: relatedTasks) {
			task.waitIsDone();
		}
		
		return this.<R>getResults(relatedTasks);
	}

	
	@SuppressWarnings("unchecked")
	private <R> List<R> getResults(List<Task<Object, Object>> relatedTasks) {
		if (relatedTasks.isEmpty()) {
			return new ArrayList<>();
		}
		
		return (List<R>) relatedTasks.get(0).getResults();
	}
	
	private <T, R> List<Task<Object, Object>> createRelatedTasks(Function<? super T, ? extends R> function, List<? extends T> args) {
		List<Task<Object, Object>> tasks = new ArrayList<>();
		List<R> results = new ArrayList<>();
		for(int i = 0; i < args.size(); i++) {
			results.add(null);
		}
		
		for(int i = 0; i < args.size(); i++) {
			T arg = args.get(i);
			@SuppressWarnings("unchecked")
			Function<? super Object, ? extends Object> f = (Function<? super Object, ? extends Object>) function;
			@SuppressWarnings("unchecked")
			List<Object> a = (List<Object>) arg;
			@SuppressWarnings("unchecked")
			List<Object> r = (List<Object>) results;
			tasks.add(new Task<Object, Object>(f, a, i, r));
		}
		
		return tasks;
	}
	
}
