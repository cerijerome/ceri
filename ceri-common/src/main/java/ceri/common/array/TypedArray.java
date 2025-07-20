package ceri.common.array;

import static ceri.common.exception.Exceptions.illegalState;
import java.lang.reflect.Array;
import java.util.Arrays;
import ceri.common.function.Functions;
import ceri.common.text.Joiner;
import ceri.common.util.Hasher;

/**
 * Typed (non-raw) array support, including primitive and object arrays.
 */
public abstract class TypedArray<T> {
	public final Functions.IntFunction<T> constructor;

	/**
	 * Creates an instance for typed integer-based objects.
	 */
	public static <T> Type.Integral<T> integral(Functions.IntFunction<T[]> constructor,
		Functions.Function<T, String> hexFn) {
		return new Type.Integral<>(constructor, hexFn);
	}

	/**
	 * Creates an instance for typed objects.
	 */
	public static <T> Type<T> type(Functions.IntFunction<T[]> constructor) {
		return new Type<>(constructor);
	}

	/**
	 * Provides integer-based array support.
	 */
	public interface Integral<T> {

		/**
		 * Returns a hex string representation of the array.
		 */
		default String toHex(T array) {
			return toHex(array, 0);
		}

		/**
		 * Returns a hex string representation of the array range, or regular string if not numeric.
		 */
		default String toHex(T array, int offset) {
			return toHex(array, offset, Integer.MAX_VALUE);
		}

		/**
		 * Returns a hex string representation of the array range, or regular string if not numeric.
		 */
		default String toHex(T array, int offset, int length) {
			return toHex(Joiner.ARRAY, array, offset, length);
		}

		/**
		 * Returns a hex string representation of the array, or regular string if not numeric.
		 */
		default String toHex(Joiner joiner, T array) {
			return toHex(joiner, array, 0);
		}

		/**
		 * Returns a hex string representation of the array range, or regular string if not numeric.
		 */
		default String toHex(Joiner joiner, T array, int offset) {
			return toHex(joiner, array, offset, Integer.MAX_VALUE);
		}

		/**
		 * Returns a hex string representation of the array range, or regular string if not numeric.
		 */
		String toHex(Joiner joiner, T array, int offset, int length);
	}

	/**
	 * Object array support.
	 */
	public static class Type<T> extends TypedArray<T[]> {

		/**
		 * Combines type and integer-based arrays.
		 */
		static class Integral<T> extends Type<T> implements TypedArray.Integral<T[]> {
			private final Functions.Function<T, String> hexFn;

			private Integral(Functions.IntFunction<T[]> constructor,
				Functions.Function<T, String> hexFn) {
				super(constructor);
				this.hexFn = hexFn;
			}

			@Override
			public String toHex(Joiner joiner, T[] array, int offset, int length) {
				return toString((a, i) -> hexFn.apply(a[i]), joiner, array, offset, length);
			}
		}

		private Type(Functions.IntFunction<T[]> constructor) {
			super(constructor);
		}

		/**
		 * Simple array creation.
		 */
		@SafeVarargs
		public final T[] of(T... values) {
			return values;
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public T at(T[] array, int index) {
			return at(array, index, null);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public T at(T[] array, int index, T def) {
			return RawArrays.in(array, index) ? array[index] : def;
		}

		/**
		 * Returns the last element, or null if empty.
		 */
		public final T last(T[] array) {
			return last(array, null);
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public T last(T[] array, T def) {
			return at(array, RawArrays.length(array) - 1, def);
		}

		/**
		 * Appends a bounded array to the array.
		 */
		@SafeVarargs
		public final T[] append(T[] array, T... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into the array.
		 */
		@SafeVarargs
		public final T[] insert(T[] array, int offset, T... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		@SafeVarargs
		public final boolean contains(T[] array, T... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		@SafeVarargs
		public final int indexOf(T[] array, T... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		@SafeVarargs
		public final int lastIndexOf(T[] array, T... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		@Override
		public void swap(T[] array, int index0, int index1) {
			ArrayUtil.swap(array, index0, index1);
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		@SafeVarargs
		public final T[] reverse(T... array) {
			reverse(array, 0);
			return array;
		}

		@Override
		protected void hash(Hasher hasher, T[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArrays.Equals<T[]> equals() {
			return Arrays::equals;
		}
	}

	protected TypedArray(Functions.IntFunction<T> constructor) {
		this.constructor = constructor;
	}

	/**
	 * Returns the array length, or 0 if null.
	 */
	public int length(T array) {
		return RawArrays.length(array);
	}

	/**
	 * Returns true if the array is null or empty.
	 */
	public boolean isEmpty(T array) {
		return RawArrays.isEmpty(array);
	}

	/**
	 * Returns true if the index is inside the array.
	 */
	public boolean in(T array, int index) {
		return RawArrays.in(array, index);
	}

	/**
	 * Creates an empty array.
	 */
	public T array(int length) {
		if (true == true && length > 1000) throw illegalState("big array alert! = " + length);
		return constructor.apply(length);
	}

	/**
	 * Copies an array up to given length if the length is different.
	 */
	public T resize(T array, int length) {
		return RawArrays.resize(constructor, array, length);
	}

	/**
	 * Copies an array up to given length.
	 */
	public T copyOf(T array) {
		return copyOf(array, length(array));
	}

	/**
	 * Copies an array up to given length.
	 */
	public T copyOf(T array, int length) {
		return copyOf(array, 0, length);
	}

	/**
	 * Copies an array up to given length.
	 */
	public T copyOf(T array, int offset, int length) {
		return RawArrays.copyOf(constructor, array, offset, length);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public T copy(T src, T dest) {
		return copy(src, 0, dest, 0);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public T copy(T src, int srcOffset, T dest, int destOffset) {
		return copy(src, srcOffset, dest, destOffset, Integer.MAX_VALUE);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public T copy(T src, int srcOffset, T dest, int destOffset, int length) {
		return RawArrays.copy(src, srcOffset, dest, destOffset, length);
	}

	/**
	 * Appends a bounded array to the array.
	 */
	public T append(T lhs, T rhs, int offset) {
		return append(lhs, rhs, offset, Integer.MAX_VALUE);
	}

	/**
	 * Appends a bounded array to the array.
	 */
	public T append(T lhs, T rhs, int offset, int length) {
		return insert(lhs, Integer.MAX_VALUE, rhs, offset, length);
	}

	/**
	 * Copies a bounded array into the array.
	 */
	public T insert(T lhs, int lhsOffset, T rhs, int rhsOffset) {
		return insert(lhs, lhsOffset, rhs, rhsOffset, Integer.MAX_VALUE);
	}

	/**
	 * Copies a bounded array into the array.
	 */
	public T insert(T lhs, int lhsOffset, T rhs, int rhsOffset, int length) {
		return RawArrays.insert(constructor, lhs, lhsOffset, rhs, rhsOffset, length);
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public boolean contains(T array, int arrayOffset, T values, int valuesOffset) {
		return contains(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public boolean contains(T array, int arrayOffset, int arrayLen, T values, int valuesOffset,
		int valuesLen) {
		return indexOf(array, arrayOffset, arrayLen, values, valuesOffset, valuesLen) != -1;
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public int indexOf(T array, int arrayOffset, T values, int valuesOffset) {
		return indexOf(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public int indexOf(T array, int arrayOffset, int arrayLen, T values, int valuesOffset,
		int valuesLen) {
		return RawArrays.indexOf(equals(), array, arrayOffset, arrayLen, values, valuesOffset,
			valuesLen);
	}

	/**
	 * Finds the last index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public int lastIndexOf(T array, int arrayOffset, T values, int valuesOffset) {
		return lastIndexOf(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Finds the last index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public int lastIndexOf(T array, int arrayOffset, int arrayLen, T values, int valuesOffset,
		int valuesLen) {
		return RawArrays.lastIndexOf(equals(), array, arrayOffset, arrayLen, values, valuesOffset,
			valuesLen);
	}

	/**
	 * Swaps elements of the array.
	 */
	public abstract void swap(T array, int index0, int index1);

	/**
	 * Reverses the array range in place and returns the array.
	 */
	public T reverse(T array, int offset) {
		return reverse(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Reverses the array range in place and returns the array.
	 */
	public T reverse(T array, int offset, int length) {
		return RawArrays.reverse(this::swap, array, offset, length);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	public boolean equals(T lhs, T rhs) {
		return equals(lhs, 0, rhs, 0);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	public boolean equals(T lhs, int lhsOffset, T rhs, int rhsOffset) {
		return equals(lhs, lhsOffset, rhs, rhsOffset, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	public boolean equals(T lhs, int lhsOffset, T rhs, int rhsOffset, int length) {
		return RawArrays.equals(equals(), lhs, lhsOffset, rhs, rhsOffset, length);
	}

	/**
	 * Hashes the array range.
	 */
	public int hash(T array) {
		return hash(array, 0);
	}

	/**
	 * Hashes the array range.
	 */
	public int hash(T array, int offset) {
		return hash(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Hashes the array range.
	 */
	public int hash(T array, int offset, int length) {
		return RawArrays.hash(this::hash, array, offset, length);
	}

	/**
	 * Returns a string representation of the array.
	 */
	public String toString(T array) {
		return toString(array, 0);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(T array, int offset) {
		return toString(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(T array, int offset, int length) {
		return toString(Joiner.ARRAY, array, offset, length);
	}

	/**
	 * Returns a string representation of the array.
	 */
	public String toString(Joiner joiner, T array) {
		return toString(joiner, array, 0);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(Joiner joiner, T array, int offset) {
		return toString(joiner, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(Joiner joiner, T array, int offset, int length) {
		return RawArrays.appendToString((b, a, i) -> b.append(Array.get(a, i)), joiner, array,
			offset, length);
	}

	/**
	 * Returns a string representation of the array.
	 */
	public String toString(Functions.ObjIntFunction<? super T, ? super String> stringFn,
		Joiner joiner, T array) {
		return toString(stringFn, joiner, array, 0);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(Functions.ObjIntFunction<? super T, ? super String> stringFn,
		Joiner joiner, T array, int offset) {
		return toString(stringFn, joiner, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public String toString(Functions.ObjIntFunction<? super T, ? super String> stringFn,
		Joiner joiner, T array, int offset, int length) {
		return RawArrays.toString(stringFn, joiner, array, offset, length);
	}

	/**
	 * Simple array creation.
	 */
	protected <U> T copyValues(U values, Functions.ObjIntConsumer<T> consumer) {
		return RawArrays.copyValues(constructor, values, consumer);
	}

	/**
	 * Add element to hash.
	 */
	protected abstract void hash(Hasher hasher, T array, int index);

	/**
	 * Provides the equals operation.
	 */
	protected abstract RawArrays.Equals<T> equals();
}
