package ru.ifmo.rain.demyanenko.arrayset;

import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ArraySet<E> implements NavigableSet<E> {
	private final List<E> array;
	private final List<E> fullArray;
	private Comparator<? super E> comparator;
	private boolean isNatural = false; 
	
	public static void main(String[] args) {

	}
	
	private ArrayList<E> toSet(ArrayList<E> array, Comparator<? super E> comparator) {
		Set<E> set = new TreeSet<E>(comparator);
		set.addAll(array);
		return new ArrayList<E>(set);
	}
	
	public ArraySet(ArrayList<E> array, Comparator<? super E> comparator) {
		this.comparator = comparator;
		this.array = this.toSet(array, comparator);
		this.fullArray = array;
		Collections.sort(this.array, comparator);
	}
	
	public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
		ArrayList<E> array = new ArrayList<E>(collection);
		this.comparator = comparator;
		this.array = this.toSet(array, comparator);
		this.fullArray = array;
		Collections.sort(array, comparator);
	}
	
	private ArraySet(List<E> array, Comparator<? super E> comparator, boolean sorting) {
		this.comparator = comparator;
		this.array = array;
		this.fullArray = array;
		if (sorting) {
			Collections.sort(array, comparator);
		}
	} 
	
	public ArraySet(E[] array, Comparator<? super E> comparator) {
		List<E> collection = Arrays.asList(array);
		ArrayList<E> arrayList = new ArrayList<E>(collection);
		this.comparator = comparator;
		this.array = this.toSet(arrayList, comparator);
		this.fullArray = arrayList;
		Collections.sort(this.array, comparator);
	}
	
	public ArraySet(Collection<E> collection)
	{
		this(collection, (Comparator<? super E>)Comparator.naturalOrder());
		this.isNatural = true;
	
	}
	
	public ArraySet(E[] array)
	{
		this(array, (Comparator<? super E>)Comparator.naturalOrder());
		this.isNatural = true;
	}

	public ArraySet() {
		this(new ArrayList<E>(), (Comparator<? super E>)Comparator.naturalOrder());
		this.isNatural = true;
	}
	
	public ArraySet(ArrayList<E> array)
	{
		this(array, (Comparator<? super E>)Comparator.naturalOrder());
	}

	@Override
	public Comparator<? super E> comparator() {
		if (this.isNatural) {
			return null;
		}
		return this.comparator;
	}
	
	@Override
	public E first() {
		if (this.array.size() > 0) {
			return this.array.get(0);
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public E last() {
		if (this.array.size() > 0) {
			return this.array.get(this.array.size() - 1);
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public int size() {
		return this.array.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.array.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		E element = null;
		try {
			element = (E) o;
		} catch (ClassCastException e) {
			return false;
		}
		return Collections.binarySearch(this.array, element, this.comparator) >= 0;
	}

	@Override
	public E pollFirst() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E pollLast() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Iterator<?> it = c.iterator();
		while (it.hasNext()) {
			if (!this.contains(it.next())){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object[] toArray() {
		return this.array.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.array.toArray(a);
	}

	private int ceilingIndex(E e) {
		int index = Collections.binarySearch(this.array, e, this.comparator);
		if (index >= 0) {
			return index;
		}
		
		if (-this.array.size() - 1 < index && index < 0) {
			return -(index + 1);
		}
		
		return -1;
	}
	
	@Override
	public E ceiling(E e) {
		int index = this.ceilingIndex(e);
		
		if (index >= 0) {
			return this.array.get(index);
		}
		
		return null;
	}

	@Override
	public Iterator<E> descendingIterator() {
		ListIterator<E> listIterator = this.array.listIterator(this.array.size());
		return new Iterator<E>() {

			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public E next() {
				return listIterator.previous();
			}
		};
	}
	
	@Override
	public NavigableSet<E> descendingSet() {	
		return new ArraySet<>(this.array, this.comparator == null ? (Comparator<? super E>)Comparator.naturalOrder().reversed() : comparator.reversed());
	}

	private int floorIndex(E e) {
		int index = Collections.binarySearch(this.array, e, this.comparator);
		if (index >= 0) {
			return index;
		}

		if (index < -1) {
			return -(index + 1) - 1;
		}

		return -1;
	}
	
	@Override
	public E floor(E e) {
		int index = floorIndex(e);

		if (index >= 0) {
			return this.array.get(index);
		}

		return null;
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return this.headSet(toElement, false);
	}

	@Override
	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return this.subSet(this.pollFirstInner(), true, toElement, inclusive, true);
	}
	
	private int higherIndex(E e) {
		int index = Collections.binarySearch(this.array, e, this.comparator);
		if (index >= 0 && index < this.array.size() - 1) {
			return index + 1;
		}
		
		if (-this.array.size() - 1 < index && index < 0) {
			return -(index + 1);
		}
		
		return -1;
	}
	
	@Override
	public E higher(E e) {
		int index = this.higherIndex(e);
		if (index >= 0) {
			return this.array.get(index);
		}
		return null;
	}

	@Override
	public Iterator<E> iterator() {
		Iterator<E> iterator = this.array.iterator();
		return new Iterator<E>() {

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public E next() {
				return iterator.next();
			}
		};
	}

	
	private int lowerIndex(E e) {
		int index = Collections.binarySearch(this.array, e, this.comparator);
		if (index > 0) {
			return index - 1;
		}
		
		if (index < -1) {
			return -(index + 1) - 1;
		}
		
		return -1;
	}
	
	@Override
	public E lower(E e) {
		int index = this.lowerIndex(e);
		if (index >= 0) {
			return this.array.get(index);
		}
		return null;
	}

	public E pollFirstInner() {
		if (this.array.size() > 0) {
			return this.array.get(0);
		}
		return null;
	}

	public E pollLastInner() {
		if (this.array.size() > 0) {
			return this.array.get(this.array.size() - 1);
		}
		return null;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return this.subSet(fromElement, true, toElement, false, false);
	}

	@Override
	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
		return this.subSet(fromElement, fromInclusive, toElement, toInclusive, false);
	}
	
	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive, boolean inner) {
		int fromIndex;

		if (!inner) {
			if (this.comparator == null ? ((Comparable<? super E>) fromElement).compareTo(toElement) > 0 : this.comparator.compare(fromElement, toElement) > 0) {
				throw new IllegalArgumentException();
			}
		} 

		if (fromInclusive) {
			fromIndex = this.ceilingIndex(fromElement);
		} else {
			fromIndex = this.higherIndex(fromElement);
		}

		if (fromIndex < 0) {
			return new ArraySet<E>(new ArrayList<>(), this.comparator, false);
		}

		int toIndex;
		if (toInclusive) {
			toIndex = this.floorIndex(toElement);
		} else {
			toIndex = this.lowerIndex(toElement);
		}

		if (toIndex < 0 || fromIndex > toIndex) {
			return new ArraySet<E>(new ArrayList<>(), this.comparator, false);
		}
		return new ArraySet<E>(this.array.subList(fromIndex, toIndex + 1), this.comparator, false);
	}
	

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return this.tailSet(fromElement, true);
	}

	@Override
	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		NavigableSet<E> result = this.subSet(fromElement, inclusive, this.pollLastInner(), true, true);
		return result;
		
	}
	
}
