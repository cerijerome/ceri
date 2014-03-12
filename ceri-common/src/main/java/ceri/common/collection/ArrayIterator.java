package ceri.common.collection;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ceri.common.util.BasicUtil;

/**
 * An array iterator primarily for primitive types.
 */
public class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
	private final Object array;
	private final int len;
	private int index = 0;

	ArrayIterator(Object array) {
		if (array == null) throw new NullPointerException("Array cannot be null");
		this.array = array;
		len = Array.getLength(array);
	}

	@SafeVarargs
	public static <T> ArrayIterator<T> createFrom(T... array) {
		return new ArrayIterator<>(array);
	}

	public static ArrayIterator<Boolean> createBoolean(final boolean... array) {
		return new ArrayIterator<Boolean>(array) {
			@Override
			protected Boolean get(int i) {
				return Boolean.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Byte> createByte(final byte... array) {
		return new ArrayIterator<Byte>(array) {
			@Override
			protected Byte get(int i) {
				return Byte.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Character> createChar(final char... array) {
		return new ArrayIterator<Character>(array) {
			@Override
			protected Character get(int i) {
				return Character.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Short> createShort(final short... array) {
		return new ArrayIterator<Short>(array) {
			@Override
			protected Short get(int i) {
				return Short.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Integer> createInt(final int... array) {
		return new ArrayIterator<Integer>(array) {
			@Override
			protected Integer get(int i) {
				return Integer.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Long> createLong(final long... array) {
		return new ArrayIterator<Long>(array) {
			@Override
			protected Long get(int i) {
				return Long.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Float> createFloat(final float... array) {
		return new ArrayIterator<Float>(array) {
			@Override
			protected Float get(int i) {
				return Float.valueOf(array[i]);
			}
		};
	}

	public static ArrayIterator<Double> createDouble(final double... array) {
		return new ArrayIterator<Double>(array) {
			@Override
			protected Double get(int i) {
				return Double.valueOf(array[i]);
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return index < len;
	}

	@Override
	public T next() {
		if (!hasNext()) throw new NoSuchElementException("Max index " + len + " exceeded");
		return get(index++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove items from an array iterator");
	}

	protected T get(int i) {
		return BasicUtil.<T>uncheckedCast(Array.get(array, i));
	}

}
