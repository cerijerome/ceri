package ceri.common.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.common.text.Joiner;

/**
 * Utility methods to test and manipulate arrays.
 * @see ceri.common.collection.CollectionUtil
 */
public class ArrayUtil {
	public static final TypedArray.Type<Object> objs = TypedArray.type(Object[]::new);
	public static final PrimitiveArray.OfBool bools = new PrimitiveArray.OfBool();
	public static final PrimitiveArray.OfChar chars = new PrimitiveArray.OfChar();
	public static final PrimitiveArray.OfByte bytes = new PrimitiveArray.OfByte();
	public static final PrimitiveArray.OfShort shorts = new PrimitiveArray.OfShort();
	public static final PrimitiveArray.OfInt ints = new PrimitiveArray.OfInt();
	public static final PrimitiveArray.OfLong longs = new PrimitiveArray.OfLong();
	public static final PrimitiveArray.OfFloat floats = new PrimitiveArray.OfFloat();
	public static final PrimitiveArray.OfDouble doubles = new PrimitiveArray.OfDouble();

	private ArrayUtil() {}

	/**
	 * Empty array constants.
	 */
	public static class Empty {
		public static final String[] strings = new String[0];
		public static final Object[] objects = new Object[0];
		public static final Class<?>[] classes = new Class<?>[0];

		private Empty() {}
	}

	public static void main(String[] args) {
		boolean[] array = RawArrays.empty(boolean.class);
		System.out.println(Arrays.toString(array));
	}

	/**
	 * Returns true if index is within array.
	 */
	public static boolean isValidIndex(int arrayLength, int index) {
		return (index >= 0 && index < arrayLength);
	}

	/**
	 * Returns true if parameters are able to slice an array.
	 */
	public static boolean isValidSlice(int arrayLength, int offset, int length) {
		if (offset < 0 || offset > arrayLength) return false;
		if (length < 0 || offset + length > arrayLength) return false;
		return true;
	}

	/**
	 * Returns true if parameters are able to slice an array by range.
	 */
	public static boolean isValidRange(int arrayLength, int start, int end) {
		if (start < 0 || start > arrayLength) return false;
		if (end < start || end > arrayLength) return false;
		return true;
	}

	/**
	 * Returns true if the slice/range is the same as the whole.
	 */
	public static boolean isFullSlice(int arrayLength, int offset, int length) {
		return offset == 0 && length == arrayLength;
	}

	/**
	 * Returns the typed array component class, or null. Primitives not supported.
	 */
	public static <T> Class<T[]> arrayType(Class<T> cls) {
		return RawArrays.arrayType(cls);
	}

	/**
	 * Returns the typed component class of an array type, or null.
	 */
	public static <T> Class<T> componentType(Class<T[]> cls) {
		return RawArrays.componentType(cls);
	}

	/**
	 * Returns true if the given object is a non-null array.
	 */
	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}

	/**
	 * Creates a typed array. Primitives are not supported.
	 */
	public static <T> T[] array(Class<T> type, int size) {
		if (type == null) return null;
		if (!type.isPrimitive()) return RawArrays.array(type, size);
		throw new IllegalArgumentException("Primitives not supported: " + type);
	}

	/**
	 * Convenience method to create an array.
	 */
	@SafeVarargs
	public static <T> T[] array(T... ts) {
		return ts;
	}

	/**
	 * Creates an empty typed array. Primitives are not supported.
	 */
	public static <T> T[] empty(Class<T> type) {
		return array(type, 0);
	}

	/**
	 * Returns the array length or 0 if null.
	 */
	@SafeVarargs
	public static <T> int length(T... array) {
		return RawArrays.length(array);
	}

	/**
	 * Returns true if the array is null or empty.
	 */
	public static <T> boolean isEmpty(T[] array) {
		return RawArrays.isEmpty(array);
	}

	// ---------------------------------------------------------------------------

	// contains(T[], T...)
	// contains(T[], offset, T[] offset)
	// contains(T[], offset, length, T[] offset, length)

	/**
	 * Returns true if the array contains the element.
	 */
	public static <T> boolean contains(T[] array, T t) {
		for (var item : array)
			if (Objects.equals(item, t)) return true;
		return false;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Returns true if the index is inside the array.
	 */
	public static <T> boolean in(T[] array, int index) {
		return RawArrays.in(array, index);
	}

	/**
	 * Returns the indexed value, or null if out of range.
	 */
	public static <T> T at(T[] array, int i) {
		return at(array, i, null);
	}

	/**
	 * Returns the indexed value, or default if out of range.
	 */
	public static <T> T at(T[] array, int i, T def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns the last element, or null if empty.
	 */
	public static <T> T last(T[] array) {
		return last(array, null);
	}

	/**
	 * Returns the last element, or default if empty.
	 */
	public static <T> T last(T[] array, T def) {
		return at(array, length(array) - 1, def);
	}

	/**
	 * Copies an array to given length if the length is different.
	 */
	public static <T> T[] resize(Functions.IntFunction<T[]> constructor, T[] array, int length) {
		return RawArrays.resize(constructor, array, length);
	}

	/**
	 * Copies an array up to given length.
	 */
	public static <T> T[] copyOf(Functions.IntFunction<T[]> constructor, T[] array) {
		return copyOf(constructor, array, length(array));
	}

	/**
	 * Copies an array up to given length.
	 */
	public static <T> T[] copyOf(Functions.IntFunction<T[]> constructor, T[] array, int length) {
		return copyOf(constructor, array, 0, length);
	}

	/**
	 * Copies an array up to given length.
	 */
	public static <T> T[] copyOf(Functions.IntFunction<T[]> constructor, T[] array, int offset,
		int length) {
		return RawArrays.copyOf(constructor, array, offset, length);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public static <T> T[] copy(T[] src, T[] dest) {
		return copy(src, 0, dest, 0);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public static <T> T[] copy(T[] src, int srcOffset, T[] dest, int destOffset) {
		return copy(src, srcOffset, dest, destOffset, Integer.MAX_VALUE);
	}

	/**
	 * Copies bounded source to bounded destination, returning the destination array.
	 */
	public static <T> T[] copy(T[] src, int srcOffset, T[] dest, int destOffset, int length) {
		return RawArrays.copy(src, srcOffset, dest, destOffset, length);
	}

	/**
	 * Appends an array to the array.
	 */
	@SafeVarargs
	public static <T> T[] append(Functions.IntFunction<T[]> constructor, T[] array, T... values) {
		return append(constructor, array, values, 0);
	}

	/**
	 * Appends a bounded array to the array.
	 */
	public static <T> T[] append(Functions.IntFunction<T[]> constructor, T[] lhs, T[] rhs,
		int offset) {
		return append(constructor, lhs, rhs, offset, Integer.MAX_VALUE);
	}

	/**
	 * Appends a bounded array to the array.
	 */
	public static <T> T[] append(Functions.IntFunction<T[]> constructor, T[] lhs, T[] rhs,
		int offset, int length) {
		return insert(constructor, lhs, Integer.MAX_VALUE, rhs, offset, length);
	}

	/**
	 * Copies values into the array.
	 */
	@SafeVarargs
	public static <T> T[] insert(Functions.IntFunction<T[]> constructor, T[] array, int offset,
		T... values) {
		return insert(constructor, array, offset, values, 0);
	}

	/**
	 * Copies a bounded array into the array.
	 */
	public static <T> T[] insert(Functions.IntFunction<T[]> constructor, T[] lhs, int lhsOffset,
		T[] rhs, int rhsOffset) {
		return insert(constructor, lhs, lhsOffset, rhs, rhsOffset, Integer.MAX_VALUE);
	}

	/**
	 * Copies a bounded array into the array.
	 */
	public static <T> T[] insert(Functions.IntFunction<T[]> constructor, T[] lhs, int lhsOffset,
		T[] rhs, int rhsOffset, int length) {
		return RawArrays.insert(constructor, lhs, lhsOffset, rhs, rhsOffset, length);
	}

	/**
	 * Returns true if the values are found within the array.
	 */
	@SafeVarargs
	public static <T> boolean contains(T[] array, T... values) {
		return contains(array, 0, values, 0);
	}

	/**
	 * Returns true if the values are found within the array. Offsets and lengths are bounded.
	 */
	public static <T> boolean contains(T[] array, int arrayOffset, T[] values, int valuesOffset) {
		return contains(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the values are found within the array. Offsets and lengths are bounded.
	 */
	public static <T> boolean contains(T[] array, int arrayOffset, int arrayLen, T[] values,
		int valuesOffset, int valuesLen) {
		return indexOf(array, arrayOffset, arrayLen, values, valuesOffset, valuesLen) != -1;
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	@SafeVarargs
	public static <T> int indexOf(T[] array, T... values) {
		return indexOf(array, 0, values, 0);
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public static <T> int indexOf(T[] array, int arrayOffset, T[] values, int valuesOffset) {
		return indexOf(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Finds the first index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public static <T> int indexOf(T[] array, int arrayOffset, int arrayLen, T[] values,
		int valuesOffset, int valuesLen) {
		return RawArrays.indexOf(Arrays::equals, array, arrayOffset, arrayLen, values, valuesOffset,
			valuesLen);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	@SafeVarargs
	public static <T> int lastIndexOf(T[] array, T... values) {
		return lastIndexOf(array, 0, values, 0);
	}

	/**
	 * Finds the last index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public static <T> int lastIndexOf(T[] array, int arrayOffset, T[] values, int valuesOffset) {
		return lastIndexOf(array, arrayOffset, Integer.MAX_VALUE, values, valuesOffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Finds the last index of values within the array. Offsets and lengths are bounded. Returns -1
	 * if not found.
	 */
	public static <T> int lastIndexOf(T[] array, int arrayOffset, int arrayLen, T[] values,
		int valuesOffset, int valuesLen) {
		return RawArrays.lastIndexOf(Arrays::equals, array, arrayOffset, arrayLen, values,
			valuesOffset, valuesLen);
	}

	/**
	 * Fill array range with value.
	 */
	public static <T> T[] fill(T[] array, T value) {
		return fill(array, 0, value);
	}

	/**
	 * Fill bounded array range with value.
	 */
	public static <T> T[] fill(T[] array, int offset, T value) {
		return fill(array, offset, Integer.MAX_VALUE, value);
	}

	/**
	 * Fill bounded array range with value.
	 */
	public static <T> T[] fill(T[] array, int offset, int length, T value) {
		return RawArrays.acceptSlice(array, offset, length,
			(o, l) -> Arrays.fill(array, o, o + l, value));
	}

	/**
	 * Swaps two array elements.
	 */
	public static <T> void swap(T[] array, int index0, int index1) {
		var a0 = array[index0];
		array[index0] = array[index1];
		array[index1] = a0;
	}

	/**
	 * Reverses the array in place and returns the array.
	 */
	@SafeVarargs
	public static <T> T[] reverse(T... array) {
		return reverse(array, 0);
	}

	/**
	 * Reverses the array range in place and returns the array.
	 */
	public static <T> T[] reverse(T[] array, int offset) {
		return reverse(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Reverses the array range in place and returns the array.
	 */
	public static <T> T[] reverse(T[] array, int offset, int length) {
		return RawArrays.reverse(ArrayUtil::swap, array, offset, length);
	}

	/**
	 * Iterates over array range.
	 */
	public static <E extends Exception, T> T[] forEach(T[] array, Excepts.Consumer<E, T> consumer)
		throws E {
		return forEach(array, 0, consumer);
	}

	/**
	 * Iterates over array range.
	 */
	public static <E extends Exception, T> T[] forEach(T[] array, int offset,
		Excepts.Consumer<E, T> consumer) throws E {
		return forEach(array, offset, Integer.MAX_VALUE, consumer);
	}

	/**
	 * Iterates over array range.
	 */
	public static <E extends Exception, T> T[] forEach(T[] array, int offset, int length,
		Excepts.Consumer<E, T> consumer) throws E {
		return RawArrays.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
	}

	/**
	 * Iterates over array range with index.
	 */
	public static <E extends Exception, T> T[] forEachIndexed(T[] array,
		Excepts.ObjIntConsumer<E, T> consumer) throws E {
		return forEachIndexed(array, 0, consumer);
	}

	/**
	 * Iterates over array range with index.
	 */
	public static <E extends Exception, T> T[] forEachIndexed(T[] array, int offset,
		Excepts.ObjIntConsumer<E, T> consumer) throws E {
		return forEachIndexed(array, offset, Integer.MAX_VALUE, consumer);
	}

	/**
	 * Iterates over array range with index.
	 */
	public static <E extends Exception, T> T[] forEachIndexed(T[] array, int offset, int length,
		Excepts.ObjIntConsumer<E, T> consumer) throws E {
		return RawArrays.acceptIndexes(array, offset, length, i -> consumer.accept(array[i], i));
	}

	/**
	 * Passes a bounded range to the function and returns the result.
	 */
	public static <E extends Exception, R> R applySlice(int arrayLen, int offset, int length,
		Excepts.IntBiFunction<E, R> function) throws E {
		offset = MathUtil.limit(offset, 0, arrayLen);
		length = MathUtil.limit(length, 0, arrayLen - offset);
		return function.apply(offset, length);
	}

	/**
	 * Passes a bounded range to the consumer.
	 */
	public static <E extends Exception> void acceptSlice(int arrayLen, int offset, int length,
		Excepts.IntBiConsumer<E> consumer) throws E {
		offset = MathUtil.limit(offset, 0, arrayLen);
		length = MathUtil.limit(length, 0, arrayLen - offset);
		consumer.accept(offset, length);
	}

	/**
	 * Passes bounded range indexes to the consumer.
	 */
	public static <E extends Exception> void acceptIndexes(int arrayLen, int offset, int length,
		Excepts.IntConsumer<E> consumer) throws E {
		offset = MathUtil.limit(offset, 0, arrayLen);
		length = MathUtil.limit(length, 0, arrayLen - offset);
		for (int i = 0; i < length; i++)
			consumer.accept(i);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	@SafeVarargs
	public static <T> boolean equals(T[] lhs, T... rhs) {
		return equals(lhs, 0, rhs, 0);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	public static <T> boolean equals(T[] lhs, int lhsOffset, T[] rhs, int rhsOffset) {
		return equals(lhs, lhsOffset, rhs, rhsOffset, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the array ranges are equals. Offsets and lengths are bounded.
	 */
	public static <T> boolean equals(T[] lhs, int lhsOffset, T[] rhs, int rhsOffset, int length) {
		return RawArrays.equals(Arrays::equals, lhs, lhsOffset, rhs, rhsOffset, length);
	}

	/**
	 * Hashes the array range.
	 */
	@SafeVarargs
	public static <T> int hash(T... array) {
		return hash(array, 0);
	}

	/**
	 * Hashes the array range.
	 */
	public static <T> int hash(T[] array, int offset) {
		return hash(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Hashes the array range.
	 */
	public static <T> int hash(T[] array, int offset, int length) {
		return RawArrays.hash((h, a, i) -> h.hash(a[i]), array, offset, length);
	}

	/**
	 * Returns a string representation of the array.
	 */
	@SafeVarargs
	public static <T> String toString(T... array) {
		return toString(array, 0);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public static <T> String toString(T[] array, int offset) {
		return toString(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public static <T> String toString(T[] array, int offset, int length) {
		return toString(Joiner.ARRAY, array, offset, length);
	}

	/**
	 * Returns a string representation of the array.
	 */
	@SafeVarargs
	public static <T> String toString(Joiner joiner, T... array) {
		return toString(joiner, array, 0);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public static <T> String toString(Joiner joiner, T[] array, int offset) {
		return toString(joiner, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a string representation of the array range.
	 */
	public static <T> String toString(Joiner joiner, T[] array, int offset, int length) {
		return RawArrays.appendToString((b, a, i) -> b.append(Array.get(a, i)), joiner, array,
			offset, length);
	}

	/**
	 * Returns a hex string representation of the array, or regular string if not numeric.
	 */
	@SafeVarargs
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T... array) {
		return toString(stringFn, joiner, array, 0);
	}

	/**
	 * Returns a hex string representation of the array range, or regular string if not numeric.
	 */
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T[] array, int offset) {
		return toString(stringFn, joiner, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a hex string representation of the array range, or regular string if not numeric.
	 */
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T[] array, int offset, int length) {
		return RawArrays.toString((a, i) -> stringFn.apply(a[i]), joiner, array, offset, length);
	}
}
