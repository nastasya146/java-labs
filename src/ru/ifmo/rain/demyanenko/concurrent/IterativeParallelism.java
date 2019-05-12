package ru.ifmo.rain.demyanenko.concurrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

public class IterativeParallelism implements ScalarIP, ListIP{

	@Override
	public <T> List<T> filter(int threadTotal, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		List<DataStorage<List<T>>> threadResults = new ArrayList<>();
		for (int i = 0; i < threadTotal; i++) {
			List <? extends T> subList = getSubList(list, i, threadTotal);
			if (subList == null) {
				break;
			}
			DataStorage<List<T>> threadResult = new DataStorage<>(new ArrayList<>());
			threadResults.add(threadResult);
			
			threads.add(new Thread(new ListHandler<T, T>(subList, threadResult) {
				@Override
				public void run() {
					for (T element: this.list) {
						if (predicate.test(element)) {
							this.storage.getData().add(element);
						}
					}
				}
			}));
		}
		
		for(Thread thread: threads) {
			thread.start();
		}
		
		for(Thread thread: threads) {
			thread.join();
		}
		
		List<T> result = new ArrayList<>();
		for (DataStorage<List<T>> threadResult: threadResults) {
			result.addAll(threadResult.getData());
		}
		
		return result;
	}

	@Override
	public String join(int threadTotal, List<?> list) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		List<DataStorage<String>> threadResults = new ArrayList<>();
		
		for (int i = 0; i < threadTotal; i++) {
			List <?> subList = getSubList(list, i, threadTotal);
			if (subList == null) {
				break;
			}
			DataStorage<String> threadResult = new DataStorage<>();
			threadResults.add(threadResult);
			
			threads.add(new Thread(new ScalarHandler<Object, String>(subList, threadResult) {
				@Override
				public void run() {
					StringBuffer bufferedResult = new StringBuffer();
					for(Object element: list) {
						bufferedResult.append(element.toString());
					}
					this.storage.setData(bufferedResult.toString());
				}
			}));
		}
		
		for(Thread thread: threads) {
			thread.start();
		}
		
		for(Thread thread: threads) {
			thread.join();
		}
		
		StringBuffer result = new StringBuffer();
		for(DataStorage<String> threadResult : threadResults) {
			result.append(threadResult.getData().toString());
		}
		
		return result.toString();
	}

	@Override
	public <T, U> List<U> map(int threadTotal, List<? extends T> list, Function<? super T, ? extends U> function)
			throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		List<DataStorage<List<U>>> threadResults = new ArrayList<>();
		for (int i = 0; i < threadTotal; i++) {
			List <? extends T> subList = getSubList(list, i, threadTotal);
			if (subList == null) {
				break;
			}
			DataStorage<List<U>> threadResult = new DataStorage<>(new ArrayList<>());
			threadResults.add(threadResult);
			
			threads.add(new Thread(new ListHandler<T, U>(subList, threadResult) {
				@Override
				public void run() {
					for (T element: this.list) {
						this.storage.getData().add(function.apply(element));
					}
				}
			}));
		}
		
		for(Thread thread: threads) {
			thread.start();
		}
		
		for(Thread thread: threads) {
			thread.join();
		}
		
		List<U> result = new ArrayList<>();
		for (DataStorage<List<U>> threadResult: threadResults) {
			result.addAll(threadResult.getData());
		}
		
		return result;
	}

	@Override
	public <T> boolean all(int threadTotal, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		DataStorage<Boolean> threadsResult = new DataStorage<>(true);		
		for (int i = 0; i < threadTotal; i++) {
			List <? extends T> subList = getSubList(list, i, threadTotal);
			if (subList == null) {
				break;
			}
			
			threads.add(new Thread(new ScalarHandler<T, Boolean>(subList, threadsResult) {
				@Override
				public void run() {
					for (T element: list) {
						if (!predicate.test(element)) {
							synchronized (this.storage) {
								this.storage.setData(false);
							}
							break;
						}
					}
				}
				
			}));
		}
		
		for(Thread thread: threads) {
			thread.start();
		}
		
		for(Thread thread: threads) {
			thread.join();
		}
		
		return threadsResult.getData();
	}
	
	@Override
	public <T> boolean any(int threadTotal, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
		return !all(threadTotal, list, new Predicate<T>() {
			@Override
			public boolean test(T t) {
				return !predicate.test(t);
			}
		});
	}

	@Override
	public <T> T maximum(int threadTotal, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		List<DataStorage<T>> threadMaxResults = new ArrayList<>();
		
		for (int i = 0; i < threadTotal; i++) {
			List <? extends T> subList = getSubList(list, i, threadTotal);
			if (subList == null) {
				break;
			}
			DataStorage<T> threadResult = new DataStorage<>();
			threadMaxResults.add(threadResult);
			
			threads.add(new Thread(new ScalarHandler<T, T>(subList, threadResult) {
				@Override
				public void run() {
					T max = this.list.get(0);
					for (T element: this.list) {
						if (comparator.compare(max, element) < 0) {
							max = element;
						}
					}
					this.storage.setData(max);
				}
			}));
		}
		
		for(Thread thread: threads) {
			thread.start();
		}
		
		for(Thread thread: threads) {
			thread.join();
		}
		
		T max = threadMaxResults.get(0).getData();
		for(DataStorage<T> storagedResult: threadMaxResults) {
			T threadResult = storagedResult.getData();
			if (comparator.compare(max, threadResult) < 0) {
				max = threadResult;
			}
		}
		
		return max;
	}

	@Override
	public <T> T minimum(int threadTotal, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
		return maximum(threadTotal, list, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return (-1) * comparator.compare(o1, o2);
			}		
		});
	}
	
	private <T> List<? extends T> getSubList(List<? extends T> list, int threadCounter, int threadTotal) {
		if (list.size() < threadTotal) {
			return getSubListMin(list, threadCounter, threadTotal);
		}
		
		int subListSize;

		if (list.size() % threadTotal != 0) {
			subListSize = list.size() / threadTotal + 1;
		} else {
			subListSize = list.size() / threadTotal;
		}

		int start = subListSize * threadCounter;
		int end = 0;

		if (threadCounter == threadTotal - 1) {
			end = list.size();
		} else {
			end = subListSize * (threadCounter + 1);
		}

		if (end > list.size() && start < list.size()) {
			end = list.size();
		}

		if (end > list.size() || start > list.size() - 1) {
			return null;
		}

		List<? extends T> subList = list.subList(start, end);

		if (subList.size() == 0) {
			return null;
		}

		return subList;
	}
	private <T> List<? extends T> getSubListMin(List<? extends T> list, int threadCounter, int threadTotal) {
		int start = threadCounter;
		int end = threadCounter + 1;

		if (end > list.size() || start > list.size() - 1) {
			return null;
		}

		List<? extends T> subList = list.subList(start, end);

		if (subList.size() == 0) {
			return null;
		}

		return subList;
	}
	
	
}
