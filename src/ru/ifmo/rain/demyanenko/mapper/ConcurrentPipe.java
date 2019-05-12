package ru.ifmo.rain.demyanenko.mapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class ConcurrentPipe<T> {
	private final Queue<T> queue;

	public ConcurrentPipe() {
		this.queue = new LinkedList<>();
	}
	
	public T pull() throws InterruptedException {
		synchronized(queue) {
			while(queue.isEmpty()) {
				queue.wait();
			}
			
			return queue.poll();
		}
	}
	
	public void push(Collection<? extends T> elements) {
		synchronized(queue) {
			for(T element: elements) {
				push(element);
			}
		}
	}
	
	public void push(T element) {
		synchronized(queue) {
			queue.add(element);
			queue.notify();
		}
	}
}
