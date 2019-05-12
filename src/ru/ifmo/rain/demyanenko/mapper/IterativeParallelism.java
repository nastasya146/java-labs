package ru.ifmo.rain.demyanenko.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class IterativeParallelism implements ScalarIP, ListIP {
	ParallelMapper mapper;

	public IterativeParallelism(ParallelMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T> boolean all(int threadTotal, List<? extends T> list, Predicate<? super T> predicate)
			throws InterruptedException {

		Function<List<? extends T>, Boolean> function = createFunctionForAll(predicate);

		List<List<? extends T>> args = new ArrayList<>();

		for (int i = 0; i < threadTotal; i++) {
			List<? extends T> subList = getSubList(list, i, threadTotal);

			if (subList == null) {
				break;
			}
			args.add(subList);
		}

		List<Boolean> results = mapper.map(function, args);

		for (Boolean result : results) {
			if (!result) {
				return false;
			}
		}

		return true;
	}

	public <T> Function<List<? extends T>, Boolean> createFunctionForAll(Predicate<? super T> predicate) {
		return new Function<List<? extends T>, Boolean>() {
			@Override
			public Boolean apply(List<? extends T> elements) {
				for (T element : elements) {
					if (!predicate.test(element)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	@Override
	public <T> boolean any(int threadTotal, List<? extends T> list, Predicate<? super T> predicate)
			throws InterruptedException {
		return !all(threadTotal, list, new Predicate<T>() {

			@Override
			public boolean test(T t) {
				return !predicate.test(t);
			}
		});
	}

	@Override
	public <T> T maximum(int threadTotal, List<? extends T> list, Comparator<? super T> comparator)
			throws InterruptedException {
		Function<List<? extends T>, T> function = createFunctionForMax(comparator);

		List<List<? extends T>> args = new ArrayList<>();

		for (int i = 0; i < threadTotal; i++) {
			List<? extends T> subList = getSubList(list, i, threadTotal);

			if (subList == null) {
				break;
			}
			args.add(subList);
		}

		List<T> results = mapper.map(function, args);

		T max = results.get(0);
		for (T result : results) {
			if (comparator.compare(max, result) < 0) {
				max = result;
			}
		}

		return max;
	}
	
	private <T> Function<List<? extends T>, T>  createFunctionForMax(Comparator<? super T> comparator) {
		return new Function<List<? extends T>, T>() {

			@Override
			public T apply(List<? extends T> list) {
				T max = list.get(0);
				for (T element : list) {
					if (comparator.compare(max, element) < 0) {
						max = element;
					}
				}

				return max;
			}
		};
	}

	private <T> List<? extends T> getSubList(List<? extends T> list, int numOfThread, int threadTotal) {
		int subListSize;

		if (list.size() % threadTotal != 0) {
			subListSize = list.size() / threadTotal + 1;
		} else {
			subListSize = list.size() / threadTotal;
		}

		int start = subListSize * numOfThread;
		int end = 0;

		if (numOfThread == threadTotal - 1) {
			end = list.size();
		} else {
			end = subListSize * (numOfThread + 1);
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

	@Override
	public <T> T minimum(int threadTotal, List<? extends T> list, Comparator<? super T> comparator)
			throws InterruptedException {
		return maximum(threadTotal, list, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return (-1) * comparator.compare(o1, o2);
			}

		});
	}

	@Override
	public <T> List<T> filter(int threadTotal, List<? extends T> list, Predicate<? super T> predicate)
			throws InterruptedException {
		Function<List<? extends T>, List<T>> function = createFunctionForFilter(predicate);

		List<List<? extends T>> args = new ArrayList<>();

		for (int i = 0; i < threadTotal; i++) {
			List<? extends T> subList = getSubList(list, i, threadTotal);

			if (subList == null) {
				break;
			}
			args.add(subList);
		}

		List<List<T>> resultParts = mapper.map(function, args);

		List<T> result = new ArrayList<>();
		for(List<T> resultPart: resultParts) {
			result.addAll(resultPart);
		}

		return result;
	}
	
	private <T> Function<List<? extends T>, List<T>> createFunctionForFilter(Predicate<? super T> predicate) {
		return new Function<List<? extends T>, List<T>>() {

			@Override
			public List<T> apply(List<? extends T> list) {
				List<T> result = new ArrayList<>();
				for(T element: list) {
					if(predicate.test(element)) {
						result.add(element);
					}
				}
				
				return result;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public String join(int threadTotal, List<?> list) throws InterruptedException {
		Function<List<Object>, StringBuffer> function = this.<Object>createFunctionForJoin();

		List<List<Object>> args = new ArrayList<>();

		for (int i = 0; i < threadTotal; i++) {
			@SuppressWarnings("rawtypes")
			List subList = getSubList(list, i, threadTotal);

			if (subList == null) {
				break;
			}
			args.add(subList);
		}

		List<StringBuffer> resultParts = mapper.map(function, args);

		StringBuffer result = new StringBuffer();
		for(StringBuffer resultPart: resultParts) {
			result.append(resultPart);
		}

		return result.toString();
	}
	
	public <T> Function<List<T>, StringBuffer> createFunctionForJoin() {
		return new Function<List<T>, StringBuffer>() {

			@Override
			public StringBuffer apply(List<T> list) {
				StringBuffer result = new StringBuffer();
				for(T element: list) {
					result.append(element.toString());
				}
				return result;
			}
		};
	}

	@Override
	public <T, R> List<R> map(int threadTotal, List<? extends T> list, Function<? super T, ? extends R> function)
			throws InterruptedException {

		List<List<? extends T>> args = new ArrayList<>();

		for (int i = 0; i < threadTotal; i++) {
			List<? extends T> subList = getSubList(list, i, threadTotal);

			if (subList == null) {
				break;
			}
			args.add(subList);
		}

		Function<List<? extends T>, List<R>> localFunctions = (List<? extends T> sublist) -> sublist.stream().map(function).collect(Collectors.toList());
		List<List<R>> resultParts = mapper.map(localFunctions, args);

		List<R> result = new ArrayList<>();
		for(List<R> resultPart: resultParts) {
			result.addAll(resultPart);
		}

		return result;
	}

}
