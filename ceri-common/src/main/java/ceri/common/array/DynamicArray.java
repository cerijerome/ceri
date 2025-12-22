package ceri.common.array;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.data.LongArray;
import ceri.common.data.LongProvider;
import ceri.common.function.Functions;

/**
 * Utility to create arrays without initially knowing the required size. Not thread-safe.
 */
public abstract class DynamicArray<T> {
	/** Default growth function. */
	public static final Functions.IntBiOperator GROW_DEF = growX2(8);
	public static final Functions.IntBiOperator GROW_EXACT = (_, s) -> s; // expands only to size
	private final Functions.IntBiOperator growth;
	public final TypedArray<T> typed;
	private T array;
	private int index = 0;

	/**
	 * A growth function that starts with min, then doubles its size.
	 */
	public static Functions.IntBiOperator growX2(int min) {
		return (i, _) -> (i < min ? min : i << 1);
	}

	/**
	 * A growth function that starts with min, then increases by step size.
	 */
	public static Functions.IntBiOperator growByStep(int min, int step) {
		return (i, _) -> (i < min ? min : i + step);
	}

	/**
	 * A growth function that starts with min, and only increases to the exact size required.
	 */
	public static Functions.IntBiOperator growExact(int min) {
		return (_, s) -> Math.max(min, s);
	}

	/**
	 * Create an object array builder with default growth.
	 */
	public static OfType<Object> of() {
		return of(GROW_DEF);
	}

	/**
	 * Create an object array builder with given growth.
	 */
	public static OfType<Object> of(Functions.IntBiOperator growth) {
		return of(Object.class, growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static <T> OfType<T> of(Class<T> component) {
		return of(component, GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static <T> OfType<T> of(Class<T> component, Functions.IntBiOperator growth) {
		return new OfType<>(component, growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfBool bools() {
		return bools(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfBool bools(Functions.IntBiOperator growth) {
		return new OfBool(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfChar chars() {
		return chars(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfChar chars(Functions.IntBiOperator growth) {
		return new OfChar(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfByte bytes() {
		return bytes(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfByte bytes(Functions.IntBiOperator growth) {
		return new OfByte(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfShort shorts() {
		return shorts(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfShort shorts(Functions.IntBiOperator growth) {
		return new OfShort(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfInt ints() {
		return ints(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfInt ints(Functions.IntBiOperator growth) {
		return new OfInt(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfLong longs() {
		return longs(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfLong longs(Functions.IntBiOperator growth) {
		return new OfLong(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfFloat floats() {
		return floats(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfFloat floats(Functions.IntBiOperator growth) {
		return new OfFloat(growth);
	}

	/**
	 * Create a type array builder with default growth.
	 */
	public static OfDouble doubles() {
		return doubles(GROW_DEF);
	}

	/**
	 * Create a type array builder with given growth.
	 */
	public static OfDouble doubles(Functions.IntBiOperator growth) {
		return new OfDouble(growth);
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfType<T> extends DynamicArray<T[]> implements Functions.Consumer<T> {
		private OfType(Class<T> component, Functions.IntBiOperator growth) {
			super(TypedArray.type(component), growth);
		}

		@Override
		public void accept(T value) {
			super.accept((a, i) -> a[i] = value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public T get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		@SafeVarargs
		public final int set(int index, T... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		@SafeVarargs
		public final int append(T... values) {
			return append(values, 0);
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfBool extends DynamicArray<boolean[]> implements Functions.BoolConsumer {
		private OfBool(Functions.IntBiOperator growth) {
			super(Array.bools, growth);
		}

		@Override
		public void accept(boolean value) {
			super.accept((a, i) -> a[i] = value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public boolean get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, boolean... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(boolean... values) {
			return append(values, 0);
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfChar extends DynamicArray<char[]> implements Functions.IntConsumer {
		private OfChar(Functions.IntBiOperator growth) {
			super(Array.chars, growth);
		}

		@Override
		public void accept(int value) {
			super.accept((a, i) -> a[i] = (char) value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public char get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, char... values) {
			return set(index, values, 0);
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, CharSequence s) {
			return set(index, s, 0);
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, CharSequence s, int offset) {
			return set(index, s, offset, Integer.MAX_VALUE);
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, CharSequence s, int offset, int length) {
			return super.set(index, s.length(), offset, length, (a, i, j) -> a[i] = s.charAt(j));
		}

		/**
		 * Appends chars and returns the new index.
		 */
		public int append(CharSequence s) {
			return append(s, 0);
		}

		/**
		 * Appends chars and returns the new index.
		 */
		public int append(CharSequence s, int offset) {
			return append(s, offset, Integer.MAX_VALUE);
		}

		/**
		 * Appends chars and returns the new index.
		 */
		public int append(CharSequence s, int offset, int length) {
			return set(index(), s, offset, length);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(char... values) {
			return append(values, 0);
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public CharBuffer buffer() {
			return CharBuffer.wrap(array(), 0, index());
		}

		@Override
		public String toString() {
			return String.valueOf(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfByte extends DynamicArray<byte[]> implements Functions.IntConsumer {
		private OfByte(Functions.IntBiOperator growth) {
			super(Array.bytes, growth);
		}

		@Override
		public void accept(int value) {
			super.accept((a, i) -> a[i] = (byte) value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public byte get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, byte... values) {
			return set(index, values, 0);
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, int... values) {
			return super.set(index, values, (a, i, j) -> a[i] = (byte) values[j]);
		}

		/**
		 * v * Appends the values and returns the new index.
		 */
		public int append(byte... values) {
			return append(values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(int... values) {
			return set(index(), values);
		}

		/**
		 * Returns an immutable wrapper.
		 */
		public ByteProvider wrap() {
			return ByteArray.Immutable.wrap(array(), 0, index());
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public ByteBuffer buffer() {
			return ByteBuffer.wrap(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfShort extends DynamicArray<short[]> implements Functions.IntConsumer {
		private OfShort(Functions.IntBiOperator growth) {
			super(Array.shorts, growth);
		}

		@Override
		public void accept(int value) {
			super.accept((a, i) -> a[i] = (short) value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public short get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, short... values) {
			return set(index, values, 0);
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, int... values) {
			return super.set(index, values, (a, i, j) -> a[i] = (short) values[j]);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(short... values) {
			return append(values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(int... values) {
			return set(index(), values);
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public ShortBuffer buffer() {
			return ShortBuffer.wrap(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfInt extends DynamicArray<int[]> implements Functions.IntConsumer {
		private OfInt(Functions.IntBiOperator growth) {
			super(Array.ints, growth);
		}

		@Override
		public void accept(int value) {
			super.accept((a, i) -> a[i] = value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public int get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, int... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(int... values) {
			return append(values, 0);
		}

		/**
		 * Returns an immutable wrapper.
		 */
		public IntProvider wrap() {
			return IntArray.Immutable.wrap(array(), 0, index());
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public IntBuffer buffer() {
			return IntBuffer.wrap(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfLong extends DynamicArray<long[]> implements Functions.LongConsumer {
		private OfLong(Functions.IntBiOperator growth) {
			super(Array.longs, growth);
		}

		@Override
		public void accept(long value) {
			super.accept((a, i) -> a[i] = value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public long get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, long... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(long... values) {
			return append(values, 0);
		}

		/**
		 * Returns an immutable wrapper.
		 */
		public LongProvider wrap() {
			return LongArray.Immutable.wrap(array(), 0, index());
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public LongBuffer buffer() {
			return LongBuffer.wrap(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfFloat extends DynamicArray<float[]> implements Functions.DoubleConsumer {
		private OfFloat(Functions.IntBiOperator growth) {
			super(Array.floats, growth);
		}

		@Override
		public void accept(double value) {
			super.accept((a, i) -> a[i] = (float) value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public float get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, float... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(float... values) {
			return append(values, 0);
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public FloatBuffer buffer() {
			return FloatBuffer.wrap(array(), 0, index());
		}
	}

	/**
	 * For building typed arrays.
	 */
	public static class OfDouble extends DynamicArray<double[]>
		implements Functions.DoubleConsumer {
		private OfDouble(Functions.IntBiOperator growth) {
			super(Array.doubles, growth);
		}

		@Override
		public void accept(double value) {
			super.accept((a, i) -> a[i] = value);
		}

		/**
		 * Get the value at index, expanding the array if needed.
		 */
		public double get(int index) {
			index = ensureIndex(index);
			return array()[index];
		}

		/**
		 * Sets the values at the index and returns the new index.
		 */
		public int set(int index, double... values) {
			return set(index, values, 0);
		}

		/**
		 * Appends the values and returns the new index.
		 */
		public int append(double... values) {
			return append(values, 0);
		}

		/**
		 * Returns a buffer wrapper.
		 */
		public DoubleBuffer buffer() {
			return DoubleBuffer.wrap(array(), 0, index());
		}
	}

	private DynamicArray(TypedArray<T> typed, Functions.IntBiOperator growth) {
		this.typed = typed;
		this.growth = growth;
		this.array = typed.resize(null, growth.applyAsInt(0, 0));
	}

	/**
	 * Returns the current array index.
	 */
	public int index() {
		return index;
	}

	/**
	 * Updates the current array index, expanding the array if needed.
	 */
	public int index(int index) {
		if (index < 0) index = Math.max(0, this.index + index);
		ensureSize(index);
		this.index = index;
		return index;
	}

	/**
	 * Returns the actual array, including padding.
	 */
	public T array() {
		return array;
	}

	/**
	 * Truncates the array to exact size.
	 */
	public T truncate() {
		array = typed.resize(array, index);
		return array;
	}

	/**
	 * Sets the values at the index and returns the new index.
	 */
	public int set(int index, T array, int offset) {
		return set(index, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Sets the values at the index and returns the new index.
	 */
	public int set(int index, T array, int offset, int length) {
		int i = resolveIndex(index);
		RawArray.acceptSlice(array, offset, length, (o, l) -> {
			ensureSize(i + l);
			typed.copy(array, o, this.array, i, l);
			this.index = Math.max(this.index, i + l);
		});
		return this.index;
	}

	/**
	 * Appends the values and returns the new index.
	 */
	public int append(T array, int offset) {
		return append(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Appends the values and returns the new index.
	 */
	public int append(T array, int offset, int length) {
		return set(index, array, offset, length);
	}

	private int resolveIndex(int index) {
		return index >= 0 ? index : Math.max(0, index() + index);
	}

	public int ensureIndex(int index) {
		index = resolveIndex(index);
		ensureSize(index + 1);
		this.index = Math.max(this.index, index + 1);
		return index;
	}

	private void ensureSize(int size) {
		int len = RawArray.length(array);
		while (len < size)
			len = growth.applyAsInt(len, size);
		array = typed.resize(array, len);
	}

	private <U> int set(int index, U array, Functions.ObjBiIntConsumer<T> consumer) {
		return set(index, RawArray.length(array), 0, Integer.MAX_VALUE, consumer);
	}

	private int set(int index, int arrayLen, int offset, int length,
		Functions.ObjBiIntConsumer<T> consumer) {
		int i = resolveIndex(index);
		Array.acceptSlice(arrayLen, offset, length, (o, l) -> {
			ensureSize(i + l);
			for (int j = 0; j < l; j++)
				consumer.accept(this.array, i + j, o + j);
			this.index = Math.max(this.index, i + l);
		});
		return this.index;
	}

	private void accept(Functions.ObjIntConsumer<T> consumer) {
		ensureSize(index + 1);
		consumer.accept(array, index++);
	}
}
