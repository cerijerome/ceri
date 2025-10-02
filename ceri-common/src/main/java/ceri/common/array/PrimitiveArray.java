package ceri.common.array;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Functions.ObjBiIntConsumer;
import ceri.common.text.Format;
import ceri.common.text.Joiner;
import ceri.common.util.Hasher;

/**
 * Provides typed primitive array support.
 */
public abstract class PrimitiveArray<T, C> extends TypedArray<T> {

	/**
	 * Typed primitive array support.
	 */
	public static class OfBool extends PrimitiveArray<boolean[], Boolean> {
		public final boolean[] empty = new boolean[0];
		public final TypedArray.Type<Boolean> box = TypedArray.type(Boolean[]::new);

		OfBool() {
			super(boolean[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public boolean[] of(boolean... values) {
			return values;
		}

		/**
		 * Converts an unboxed array to a boxed array.
		 */
		public Boolean[] boxed(boolean... values) {
			return boxed(values, 0);
		}

		@Override
		public Boolean[] boxed(boolean[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Boolean> list(boolean... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public boolean at(boolean[] array, int index, boolean def) {
			return in(array, index) ? array[index] : def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public boolean last(boolean[] array, boolean def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public boolean[] append(boolean[] array, boolean... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public boolean[] insert(boolean[] array, int offset, boolean... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(boolean[] array, boolean... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(boolean[] array, boolean... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(boolean[] array, boolean... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public boolean[] fill(boolean[] array, boolean value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public boolean[] fill(boolean[] array, int offset, boolean value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public boolean[] fill(boolean[] array, int offset, int length, boolean value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, value));
		}

		@Override
		public void swap(boolean[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public boolean[] reverse(boolean... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> boolean[] forEach(boolean[] array,
			Excepts.BoolConsumer<E> consumer) throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> boolean[] forEach(boolean[] array, int offset,
			Excepts.BoolConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> boolean[] forEach(boolean[] array, int offset, int length,
			Excepts.BoolConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(boolean[] array, boolean... values) {
			return super.equals(array, 0, values, 0);
		}

		@Override
		protected ObjBiIntConsumer<boolean[]> sorter() {
			return OfBool::sortArray;
		}

		@Override
		protected void hash(Hasher hasher, boolean[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<boolean[]> equals() {
			return Arrays::equals;
		}

		private static void sortArray(boolean[] array, int from, int to) {
			int n = from;
			for (int i = from; i < to; i++)
				if (!array[i]) n++;
			for (int i = from; i < to; i++)
				array[i] = i >= n;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfChar extends PrimitiveArray<char[], Character>
		implements TypedArray.Integral<char[]> {
		public final char[] empty = new char[0];
		public final TypedArray.Type.Integral<Character> box =
			TypedArray.integral(Character[]::new, Format.HEX::apply);

		OfChar() {
			super(char[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public char[] of(char... values) {
			return values;
		}

		/**
		 * Simple array creation.
		 */
		public char[] of(int... values) {
			return copyValues(values, (a, v, i) -> a[i] = (char) v[i]);
		}

		/**
		 * Converts an unboxed array to a boxed array.
		 */
		public Character[] boxed(char... values) {
			return boxed(values, 0);
		}

		@Override
		public Character[] boxed(char[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Character> list(char... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public char at(char[] array, int index, int def) {
			return in(array, index) ? array[index] : (char) def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public char last(char[] array, int def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public char[] append(char[] array, char... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public char[] insert(char[] array, int offset, char... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(char[] array, char... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(char[] array, char... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(char[] array, char... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public char[] fill(char[] array, int value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public char[] fill(char[] array, int offset, int value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public char[] fill(char[] array, int offset, int length, int value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, (char) value));
		}

		@Override
		public void swap(char[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public char[] reverse(char... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> char[] forEach(char[] array, Excepts.IntConsumer<E> consumer)
			throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> char[] forEach(char[] array, int offset,
			Excepts.IntConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> char[] forEach(char[] array, int offset, int length,
			Excepts.IntConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		@Override
		public String toHex(Joiner joiner, char[] array, int offset, int length) {
			return toString((a, i) -> Format.HEX.apply(a[i]), joiner, array, offset, length);
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(char[] array, char... values) {
			return super.equals(array, 0, values, 0);
		}

		/**
		 * Returns true if the arrays are equivalent.
		 */
		public boolean equals(char[] array, int... values) {
			return equivalent(array, values, i -> array[i] == (char) values[i]);
		}

		@Override
		protected ObjBiIntConsumer<char[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, char[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<char[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfByte extends PrimitiveArray<byte[], Byte>
		implements TypedArray.Integral<byte[]> {
		public final byte[] empty = new byte[0];
		public final TypedArray.Type.Integral<Byte> box =
			TypedArray.integral(Byte[]::new, Format.HEX::ubyte);

		OfByte() {
			super(byte[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public byte[] of(byte... values) {
			return values;
		}

		/**
		 * Simple array creation.
		 */
		public byte[] of(int... values) {
			return copyValues(values, (a, v, i) -> a[i] = (byte) v[i]);
		}

		/**
		 * Converts unboxed values to a boxed array.
		 */
		public Byte[] boxed(byte... values) {
			return boxed(values, 0);
		}

		/**
		 * Converts unboxed values to a boxed array.
		 */
		public Byte[] boxed(int... values) {
			return box.copyValues(values, (a, v, i) -> a[i] = (byte) v[i]);
		}

		@Override
		public Byte[] boxed(byte[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Byte> list(byte... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public byte at(byte[] array, int index, int def) {
			return in(array, index) ? array[index] : (byte) def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public byte last(byte[] array, int def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public byte[] append(byte[] array, byte... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public byte[] insert(byte[] array, int offset, byte... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(byte[] array, byte... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(byte[] array, byte... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(byte[] array, byte... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public byte[] fill(byte[] array, int value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public byte[] fill(byte[] array, int offset, int value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public byte[] fill(byte[] array, int offset, int length, int value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, (byte) value));
		}

		@Override
		public void swap(byte[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public byte[] reverse(byte... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> byte[] forEach(byte[] array, Excepts.IntConsumer<E> consumer)
			throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> byte[] forEach(byte[] array, int offset,
			Excepts.IntConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> byte[] forEach(byte[] array, int offset, int length,
			Excepts.IntConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(byte[] array, byte... values) {
			return super.equals(array, 0, values, 0);
		}

		/**
		 * Returns true if the arrays are equivalent.
		 */
		public boolean equals(byte[] array, int... values) {
			return equivalent(array, values, i -> array[i] == (byte) values[i]);
		}

		@Override
		public String toHex(Joiner joiner, byte[] array, int offset, int length) {
			return toString((a, i) -> Format.HEX.ubyte(a[i]), joiner, array, offset, length);
		}

		@Override
		protected ObjBiIntConsumer<byte[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, byte[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<byte[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfShort extends PrimitiveArray<short[], Short>
		implements TypedArray.Integral<short[]> {
		public final short[] empty = new short[0];
		public final TypedArray.Type.Integral<Short> box =
			TypedArray.integral(Short[]::new, Format.HEX::ushort);

		OfShort() {
			super(short[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public short[] of(short... values) {
			return values;
		}

		/**
		 * Simple array creation.
		 */
		public short[] of(int... values) {
			return copyValues(values, (a, v, i) -> a[i] = (short) v[i]);
		}

		/**
		 * Converts unboxed values to a boxed array.
		 */
		public Short[] boxed(short... values) {
			return boxed(values, 0);
		}

		/**
		 * Converts unboxed values to a boxed array.
		 */
		public Short[] boxed(int... values) {
			return box.copyValues(values, (a, v, i) -> a[i] = (short) v[i]);
		}

		@Override
		public Short[] boxed(short[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Short> list(short... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public short at(short[] array, int index, int def) {
			return in(array, index) ? array[index] : (short) def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public short last(short[] array, int def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public short[] append(short[] array, short... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public short[] insert(short[] array, int offset, short... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(short[] array, short... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(short[] array, short... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(short[] array, short... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public short[] fill(short[] array, int value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public short[] fill(short[] array, int offset, int value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public short[] fill(short[] array, int offset, int length, int value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, (short) value));
		}

		@Override
		public void swap(short[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public short[] reverse(short... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> short[] forEach(short[] array, Excepts.IntConsumer<E> consumer)
			throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> short[] forEach(short[] array, int offset,
			Excepts.IntConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> short[] forEach(short[] array, int offset, int length,
			Excepts.IntConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(short[] array, short... values) {
			return super.equals(array, 0, values, 0);
		}

		/**
		 * Returns true if the arrays are equivalent.
		 */
		public boolean equals(short[] array, int... values) {
			return equivalent(array, values, i -> array[i] == (short) values[i]);
		}

		@Override
		public String toHex(Joiner joiner, short[] array, int offset, int length) {
			return toString((a, i) -> Format.HEX.ushort(a[i]), joiner, array, offset, length);
		}

		@Override
		protected ObjBiIntConsumer<short[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, short[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<short[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfInt extends PrimitiveArray<int[], Integer>
		implements TypedArray.Integral<int[]> {
		public final int[] empty = new int[0];
		public final TypedArray.Type.Integral<Integer> box =
			TypedArray.integral(Integer[]::new, Format.HEX::uint);

		OfInt() {
			super(int[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public int[] of(int... values) {
			return values;
		}

		/**
		 * Creates an array of sequential values.
		 */
		public int[] range(int offset, int length) {
			if (length <= 0) return empty;
			var array = array(length);
			for (int i = 0; i < length; i++)
				array[i] = offset + i;
			return array;
		}

		/**
		 * Converts unboxed values to a boxed array.
		 */
		public Integer[] boxed(int... values) {
			return boxed(values, 0);
		}

		@Override
		public Integer[] boxed(int[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Integer> list(int... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public int at(int[] array, int index, int def) {
			return in(array, index) ? array[index] : def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public int last(int[] array, int def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public int[] append(int[] array, int... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public int[] insert(int[] array, int offset, int... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(int[] array, int... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(int[] array, int... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(int[] array, int... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public int[] fill(int[] array, int value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public int[] fill(int[] array, int offset, int value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public int[] fill(int[] array, int offset, int length, int value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, value));
		}

		@Override
		public void swap(int[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public int[] reverse(int... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> int[] forEach(int[] array, Excepts.IntConsumer<E> consumer)
			throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> int[] forEach(int[] array, int offset,
			Excepts.IntConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> int[] forEach(int[] array, int offset, int length,
			Excepts.IntConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(int[] array, int... values) {
			return super.equals(array, 0, values, 0);
		}

		@Override
		public String toHex(Joiner joiner, int[] array, int offset, int length) {
			return toString((a, i) -> Format.HEX.uint(a[i]), joiner, array, offset, length);
		}

		@Override
		protected ObjBiIntConsumer<int[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, int[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<int[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfLong extends PrimitiveArray<long[], Long>
		implements TypedArray.Integral<long[]> {
		public final long[] empty = new long[0];
		public final TypedArray.Type.Integral<Long> box =
			TypedArray.integral(Long[]::new, Format.HEX::apply);

		OfLong() {
			super(long[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public long[] of(long... values) {
			return values;
		}

		/**
		 * Creates an array of sequential values.
		 */
		public long[] range(long offset, int length) {
			if (length <= 0) return empty;
			var array = array(length);
			for (int i = 0; i < length; i++)
				array[i] = offset + i;
			return array;
		}

		/**
		 * Converts an unboxed array to a boxed array.
		 */
		public Long[] boxed(long... values) {
			return boxed(values, 0);
		}

		@Override
		public Long[] boxed(long[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Long> list(long... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public long at(long[] array, int index, long def) {
			return in(array, index) ? array[index] : def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public long last(long[] array, long def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public long[] append(long[] array, long... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public long[] insert(long[] array, int offset, long... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(long[] array, long... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(long[] array, long... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(long[] array, long... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public long[] fill(long[] array, long value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public long[] fill(long[] array, int offset, long value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public long[] fill(long[] array, int offset, int length, long value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, value));
		}

		@Override
		public void swap(long[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public long[] reverse(long... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> long[] forEach(long[] array, Excepts.LongConsumer<E> consumer)
			throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> long[] forEach(long[] array, int offset,
			Excepts.LongConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> long[] forEach(long[] array, int offset, int length,
			Excepts.LongConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(long[] array, long... values) {
			return super.equals(array, 0, values, 0);
		}

		@Override
		public String toHex(Joiner joiner, long[] array, int offset, int length) {
			return toString((a, i) -> Format.HEX.apply(a[i]), joiner, array, offset, length);
		}

		@Override
		protected ObjBiIntConsumer<long[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, long[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<long[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfFloat extends PrimitiveArray<float[], Float> {
		public final float[] empty = new float[0];
		public final TypedArray.Type<Float> box = TypedArray.type(Float[]::new);

		OfFloat() {
			super(float[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public float[] of(float... values) {
			return values;
		}

		/**
		 * Simple array creation.
		 */
		public float[] of(double... values) {
			return copyValues(values, (a, v, i) -> a[i] = (float) v[i]);
		}

		/**
		 * Converts an unboxed array to a boxed array.
		 */
		public Float[] boxed(float... values) {
			return boxed(values, 0);
		}

		/**
		 * Converts an unboxed values to a boxed array.
		 */
		public Float[] boxed(double... values) {
			return box.copyValues(values, (a, v, i) -> a[i] = (float) v[i]);
		}

		@Override
		public Float[] boxed(float[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Float> list(float... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public float at(float[] array, int index, double def) {
			return in(array, index) ? array[index] : (float) def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public float last(float[] array, double def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public float[] append(float[] array, float... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public float[] insert(float[] array, int offset, float... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(float[] array, float... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(float[] array, float... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(float[] array, float... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public float[] fill(float[] array, double value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public float[] fill(float[] array, int offset, double value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public float[] fill(float[] array, int offset, int length, double value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, (float) value));
		}

		@Override
		public void swap(float[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public float[] reverse(float... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> float[] forEach(float[] array,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> float[] forEach(float[] array, int offset,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> float[] forEach(float[] array, int offset, int length,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(float[] array, float... values) {
			return super.equals(array, 0, values, 0);
		}

		/**
		 * Returns true if the arrays are equivalent.
		 */
		public boolean equals(float[] array, double... values) {
			return equivalent(array, values, i -> array[i] == (float) values[i]);
		}

		@Override
		protected ObjBiIntConsumer<float[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, float[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<float[]> equals() {
			return Arrays::equals;
		}
	}

	/**
	 * Typed primitive array support.
	 */
	public static class OfDouble extends PrimitiveArray<double[], Double> {
		public final double[] empty = new double[0];
		public final TypedArray.Type<Double> box = TypedArray.type(Double[]::new);

		OfDouble() {
			super(double[]::new);
		}

		/**
		 * Simple array creation.
		 */
		public double[] of(double... values) {
			return values;
		}

		/**
		 * Converts an unboxed array to a boxed array.
		 */
		public Double[] boxed(double... values) {
			return boxed(values, 0);
		}

		@Override
		public Double[] boxed(double[] array, int offset, int length) {
			return RawArray.boxed(box::array, array, offset, length);
		}

		/**
		 * Converts an unboxed array to a list back by a boxed array.
		 */
		public List<Double> list(double... values) {
			return list(values, 0);
		}

		/**
		 * Returns the element at index, or default if out of range.
		 */
		public double at(double[] array, int index, double def) {
			return in(array, index) ? array[index] : def;
		}

		/**
		 * Returns the last element, or default if empty.
		 */
		public double last(double[] array, double def) {
			return at(array, length(array) - 1, def);
		}

		/**
		 * Appends the bounded array to the array.
		 */
		public double[] append(double[] array, double... values) {
			return append(array, values, 0);
		}

		/**
		 * Copies values into an array.
		 */
		public double[] insert(double[] array, int offset, double... values) {
			return insert(array, offset, values, 0);
		}

		/**
		 * Returns true if the values are found within the array.
		 */
		public boolean contains(double[] array, double... values) {
			return contains(array, 0, values, 0);
		}

		/**
		 * Finds the first index of values within the array. Returns -1 if not found.
		 */
		public int indexOf(double[] array, double... values) {
			return indexOf(array, 0, values, 0);
		}

		/**
		 * Finds the last index of values within the array. Returns -1 if not found.
		 */
		public int lastIndexOf(double[] array, double... values) {
			return lastIndexOf(array, 0, values, 0);
		}

		/**
		 * Fill array with value.
		 */
		public double[] fill(double[] array, double value) {
			return fill(array, 0, value);
		}

		/**
		 * Fill array range with value.
		 */
		public double[] fill(double[] array, int offset, double value) {
			return fill(array, offset, Integer.MAX_VALUE, value);
		}

		/**
		 * Fill array range with value.
		 */
		public double[] fill(double[] array, int offset, int length, double value) {
			return RawArray.acceptSlice(array, offset, length,
				(o, l) -> Arrays.fill(array, o, o + l, value));
		}

		@Override
		public void swap(double[] array, int index0, int index1) {
			var a0 = array[index0];
			array[index0] = array[index1];
			array[index1] = a0;
		}

		/**
		 * Reverses the array in place and returns the array.
		 */
		public double[] reverse(double... array) {
			return reverse(array, 0);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> double[] forEach(double[] array,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return forEach(array, 0, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> double[] forEach(double[] array, int offset,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return forEach(array, offset, Integer.MAX_VALUE, consumer);
		}

		/**
		 * Iterates over array range.
		 */
		public <E extends Exception> double[] forEach(double[] array, int offset, int length,
			Excepts.DoubleConsumer<E> consumer) throws E {
			return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
		}

		/**
		 * Returns true if the arrays are equal.
		 */
		public boolean equals(double[] array, double... values) {
			return super.equals(array, 0, values, 0);
		}

		@Override
		protected ObjBiIntConsumer<double[]> sorter() {
			return Arrays::sort;
		}

		@Override
		protected void hash(Hasher hasher, double[] array, int index) {
			hasher.hash(array[index]);
		}

		@Override
		protected RawArray.Equals<double[]> equals() {
			return Arrays::equals;
		}
	}

	private PrimitiveArray(Functions.IntFunction<T> constructor) {
		super(constructor);
	}

	/**
	 * Converts an unboxed array to a boxed array.
	 */
	public C[] boxed(T array, int offset) {
		return boxed(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Converts an unboxed array to a boxed array.
	 */
	public abstract C[] boxed(T array, int offset, int length);

	/**
	 * Converts an unboxed array to a list back by a boxed array.
	 */
	public List<C> list(T array, int offset) {
		return list(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Converts an unboxed array to a list back by a boxed array.
	 */
	public List<C> list(T array, int offset, int length) {
		return Arrays.asList(boxed(array, offset, length));
	}

	/**
	 * Converts a boxed array to an unboxed array.
	 */
	public T unboxed(C[] array) {
		return unboxed(array, 0);
	}

	/**
	 * Converts a boxed array to an unboxed array.
	 */
	public T unboxed(C[] array, int offset) {
		return unboxed(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Converts a boxed array to an unboxed array.
	 */
	public T unboxed(C[] array, int offset, int length) {
		return RawArray.unboxed(this::array, array, offset, length);
	}

	/**
	 * Converts a boxed collection to an unboxed array.
	 */
	public T unboxed(Collection<C> collection) {
		return RawArray.unboxed(this::array, collection);
	}

	/**
	 * Converts a boxed collection to an unboxed array.
	 */
	public T unboxed(List<C> list, int offset) {
		return unboxed(list, offset, Integer.MAX_VALUE);
	}

	/**
	 * Converts a boxed collection to an unboxed array.
	 */
	public T unboxed(List<C> list, int offset, int length) {
		return RawArray.unboxed(this::array, list, offset, length);
	}

	/**
	 * Iterates over array range with boxed values.
	 */
	public <E extends Exception> T forEachBox(T array, Excepts.Consumer<E, C> consumer) throws E {
		return forEachBox(array, 0, consumer);
	}

	/**
	 * Iterates over array range with boxed values.
	 */
	public <E extends Exception> T forEachBox(T array, int offset, Excepts.Consumer<E, C> consumer)
		throws E {
		return forEachBox(array, offset, Integer.MAX_VALUE, consumer);
	}

	/**
	 * Iterates over array range with boxed values.
	 */
	public <E extends Exception> T forEachBox(T array, int offset, int length,
		Excepts.Consumer<E, C> consumer) throws E {
		return RawArray.acceptIndexes(array, offset, length,
			i -> consumer.accept(RawArray.get(array, i)));
	}

	/**
	 * Iterates over array range with boxed values and index.
	 */
	public <E extends Exception> T forEachBoxIndexed(T array, Excepts.ObjIntConsumer<E, C> consumer)
		throws E {
		return forEachBoxIndexed(array, 0, consumer);
	}

	/**
	 * Iterates over array range with boxed values and index.
	 */
	public <E extends Exception> T forEachBoxIndexed(T array, int offset,
		Excepts.ObjIntConsumer<E, C> consumer) throws E {
		return forEachBoxIndexed(array, offset, Integer.MAX_VALUE, consumer);
	}

	/**
	 * Iterates over array range with boxed values and index.
	 */
	public <E extends Exception> T forEachBoxIndexed(T array, int offset, int length,
		Excepts.ObjIntConsumer<E, C> consumer) throws E {
		return RawArray.acceptIndexes(array, offset, length,
			i -> consumer.accept(RawArray.get(array, i), i));
	}

	/**
	 * Sorts the array range in place.
	 */
	public T sort(T array) {
		return sort(array, 0);
	}

	/**
	 * Sorts the array range in place.
	 */
	public T sort(T array, int offset) {
		return sort(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Sorts the array range in place.
	 */
	public T sort(T array, int offset, int length) {
		return RawArray.acceptSlice(array, offset, length,
			(o, l) -> sorter().accept(array, o, o + l));
	}

	/**
	 * Array range sort function.
	 */
	protected abstract Functions.ObjBiIntConsumer<T> sorter();

	// support

	private static <T, U> boolean equivalent(T lhs, U rhs, Functions.IntPredicate equivalence) {
		if (lhs == null && rhs == null) return true;
		if (lhs == null || rhs == null) return false;
		int lhsLen = RawArray.length(lhs);
		int rhsLen = RawArray.length(rhs);
		if (lhsLen != rhsLen) return false;
		for (int i = 0; i < lhsLen; i++)
			if (!equivalence.test(i)) return false;
		return true;
	}
}
