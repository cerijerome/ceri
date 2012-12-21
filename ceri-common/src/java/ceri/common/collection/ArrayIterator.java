/**
 * Created on Mar 29, 2007
 */
package ceri.common.collection;

import java.lang.reflect.Array;
import java.util.Iterator;
import ceri.common.util.BasicUtil;

public class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
	private final Object array;
	private int index = 0;

	private ArrayIterator(Object array) {
		if (array == null) throw new NullPointerException("Array cannot be null");
		this.array = array;
	}

	@SafeVarargs
	public static <T> ArrayIterator<T> create(T... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Boolean> create(boolean... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Byte> create(byte... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Character> create(char... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Short> create(short... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Integer> create(int... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Long> create(long... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Float> create(float... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Double> create(double... array) {
		return new ArrayIterator<>(array);
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return index < Array.getLength(array);
	}

	@Override
	public T next() {
		if (!hasNext()) throw new IndexOutOfBoundsException("Max index " + Array.getLength(array) +
			" exceeded");
		return BasicUtil.<T>uncheckedCast(Array.get(array, index++));
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove items from an array iterator");
	}

}
