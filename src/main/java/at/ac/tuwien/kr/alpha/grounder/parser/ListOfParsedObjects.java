package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ListOfParsedObjects extends CommonParsedObject implements List<CommonParsedObject> {
	public List<CommonParsedObject> delegate;

	public ListOfParsedObjects(List<CommonParsedObject> delegate) {
		this.delegate = delegate;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public Iterator<CommonParsedObject> iterator() {
		return delegate.iterator();
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean add(CommonParsedObject commonParsedObject) {
		return delegate.add(commonParsedObject);
	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends CommonParsedObject> c) {
		return delegate.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean addAll(int index, Collection<? extends CommonParsedObject> c) {
		return delegate.addAll(index, c);
	}

	@Override
	public CommonParsedObject get(int index) {
		return delegate.get(index);
	}

	@Override
	public CommonParsedObject set(int index, CommonParsedObject element) {
		return delegate.set(index, element);
	}

	@Override
	public void add(int index, CommonParsedObject element) {
		delegate.add(index, element);
	}

	@Override
	public CommonParsedObject remove(int index) {
		return delegate.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public ListIterator<CommonParsedObject> listIterator() {
		return delegate.listIterator();
	}

	@Override
	public ListIterator<CommonParsedObject> listIterator(int index) {
		return delegate.listIterator(index);
	}

	@Override
	public List<CommonParsedObject> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}
}
