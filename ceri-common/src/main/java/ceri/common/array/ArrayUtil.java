package ceri.common.array;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
import ceri.common.math.Maths;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;

/**
 * Utility methods to test and manipulate arrays.
 * @see ceri.common.collection.CollectionUtil
 */
public class ArrayUtil {
	private ArrayUtil() {}

	public static final PrimitiveArray.OfBool bools = new PrimitiveArray.OfBool();
	public static final PrimitiveArray.OfChar chars = new PrimitiveArray.OfChar();
	public static final PrimitiveArray.OfByte bytes = new PrimitiveArray.OfByte();
	public static final PrimitiveArray.OfShort shorts = new PrimitiveArray.OfShort();
	public static final PrimitiveArray.OfInt ints = new PrimitiveArray.OfInt();
	public static final PrimitiveArray.OfLong longs = new PrimitiveArray.OfLong();
	public static final PrimitiveArray.OfFloat floats = new PrimitiveArray.OfFloat();
	public static final PrimitiveArray.OfDouble doubles = new PrimitiveArray.OfDouble();

	/**
	 * Accepts bounded offsets and lengths.
	 */
	public interface BiSliceConsumer<E extends Exception> {
		void accept(int lOffset, int lLength, int rOffset, int rLength) throws E;
	}

	/**
	 * Applies bounded offsets and lengths.
	 */
	public interface BiSliceFunction<E extends Exception, T> {
		T apply(int lOffset, int lLength, int rOffset, int rLength) throws E;
	}

	/**
	 * Empty array constants.
	 */
	public static class Empty {
		private Empty() {}

		public static final Boolean[] bools = new Boolean[0];
		public static final Character[] chars = new Character[0];
		public static final Byte[] bytes = new Byte[0];
		public static final Short[] shorts = new Short[0];
		public static final Integer[] ints = new Integer[0];
		public static final Long[] longs = new Long[0];
		public static final Float[] floats = new Float[0];
		public static final Double[] doubles = new Double[0];
		public static final String[] strings = new String[0];
		public static final Object[] objects = new Object[0];
		public static final Class<?>[] classes = new Class<?>[0];
	}

	public static class Filter {
		private Filter() {}

		/**
		 * Returns true if any element and index matches the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, T[]>
			any(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				for (int i = 0; i < ts.length; i++)
					if (predicate.test(ts[i], i)) return true;
				return false;
			};
		}

		/**
		 * Returns true if all elements and indexes match the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, T[]>
			all(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				for (int i = 0; i < ts.length; i++)
					if (!predicate.test(ts[i], i)) return false;
				return true;
			};
		}
	}

	/**
	 * Convenience method to create an array.
	 */
	@SafeVarargs
	public static <T> T[] of(T... ts) {
		return ts;
	}

	/**
	 * Creates a typed array. Primitives are not supported.
	 */
	public static <T> T[] ofType(Class<T> type, int size) {
		if (type == null) return null;
		return RawArray.ofType(requireNonPrimitive(type), size);
	}

	/**
	 * Returns the array length or 0 if null.
	 */
	@SafeVarargs
	public static <T> int length(T... array) {
		return RawArray.length(array);
	}

	/**
	 * Returns true if the array is null or empty.
	 */
	public static <T> boolean isEmpty(T[] array) {
		return RawArray.isEmpty(array);
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
		return RawArray.arrayType(requireNonPrimitive(cls));
	}

	/**
	 * Returns the typed component class of an array type, or null.
	 */
	public static <T> Class<T> componentType(Class<T[]> cls) {
		return RawArray.componentType(cls);
	}

	/**
	 * Returns true if index is within array length.
	 */
	public static boolean in(int arrayLength, int index) {
		return (index >= 0 && index < arrayLength);
	}

	/**
	 * Returns true if the index is inside the array.
	 */
	public static <T> boolean in(T[] array, int index) {
		return RawArray.in(array, index);
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
		return RawArray.resize(constructor, array, length);
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
		return RawArray.copyOf(constructor, array, offset, length);
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
		return RawArray.copy(src, srcOffset, dest, destOffset, length);
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
		return RawArray.insert(constructor, rhs, rhsOffset, lhs, lhsOffset, length);
	}

	/**
	 * Returns true if the array contains the element.
	 */
	public static <T> boolean has(T[] array, T t) {
		if (array != null) for (var item : array)
			if (Objects.equals(item, t)) return true;
		return false;
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
		return RawArray.indexOf(Arrays::equals, array, arrayOffset, arrayLen, values, valuesOffset,
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
		return RawArray.lastIndexOf(Arrays::equals, array, arrayOffset, arrayLen, values,
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
		return RawArray.acceptSlice(array, offset, length,
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
		return RawArray.reverse(ArrayUtil::swap, array, offset, length);
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
		if (consumer == null) return array;
		return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i]));
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
		if (consumer == null) return array;
		return RawArray.acceptIndexes(array, offset, length, i -> consumer.accept(array[i], i));
	}

	/**
	 * Passes a bounded range to the function and returns the result.
	 */
	public static <E extends Exception, T> T applySlice(int arrayLen, int offset, int length,
		Excepts.IntBiFunction<E, T> function) throws E {
		if (function == null) return null;
		offset = Maths.limit(offset, 0, arrayLen);
		length = Maths.limit(length, 0, arrayLen - offset);
		return function.apply(offset, length);
	}

	/**
	 * Passes a bounded range to the consumer.
	 */
	public static <E extends Exception> void acceptSlice(int arrayLen, int offset, int length,
		Excepts.IntBiConsumer<E> consumer) throws E {
		if (consumer == null) return;
		offset = Maths.limit(offset, 0, arrayLen);
		length = Maths.limit(length, 0, arrayLen - offset);
		consumer.accept(offset, length);
	}

	/**
	 * Passes bounded range indexes to the consumer.
	 */
	public static <E extends Exception> void acceptIndexes(int arrayLen, int offset, int length,
		Excepts.IntConsumer<E> consumer) throws E {
		if (consumer == null) return;
		offset = Maths.limit(offset, 0, arrayLen);
		length = Maths.limit(length, 0, arrayLen - offset);
		for (int i = 0; i < length; i++)
			consumer.accept(offset + i);
	}

	/**
	 * Passes bounded ranges to the function and returns the result.
	 */
	public static <E extends Exception, T> T applyBiSlice(int lArrayLen, int lOffset, int lLength,
		int rArrayLen, int rOffset, int rLength, BiSliceFunction<E, T> function) throws E {
		if (function == null) return null;
		lOffset = Maths.limit(lOffset, 0, lArrayLen);
		lLength = Maths.limit(lLength, 0, lArrayLen - lOffset);
		rOffset = Maths.limit(rOffset, 0, rArrayLen);
		rLength = Maths.limit(rLength, 0, rArrayLen - rOffset);
		return function.apply(lOffset, lLength, rOffset, rLength);
	}

	/**
	 * Passes bounded ranges to the consumer.
	 */
	public static <E extends Exception> void acceptBiSlice(int lArrayLen, int lOffset, int lLength,
		int rArrayLen, int rOffset, int rLength, BiSliceConsumer<E> consumer) throws E {
		if (consumer == null) return;
		lOffset = Maths.limit(lOffset, 0, lArrayLen);
		lLength = Maths.limit(lLength, 0, lArrayLen - lOffset);
		rOffset = Maths.limit(rOffset, 0, rArrayLen);
		rLength = Maths.limit(rLength, 0, rArrayLen - rOffset);
		consumer.accept(lOffset, lLength, rOffset, rLength);
	}

	/**
	 * Sort the array in place and return the array.
	 */
	public static <T extends Comparable<? super T>> T[] sort(T[] array) {
		return sort(array, 0);
	}

	/**
	 * Sort the array range in place and return the array.
	 */
	public static <T extends Comparable<? super T>> T[] sort(T[] array, int offset) {
		return sort(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Sort the array range in place and return the array.
	 */
	public static <T extends Comparable<? super T>> T[] sort(T[] array, int offset, int length) {
		return sort(array, offset, length, Comparators.nullsFirst());
	}

	/**
	 * Sort the array in place and return the array.
	 */
	public static <T> T[] sort(T[] array, Comparator<? super T> comparator) {
		return sort(array, 0, comparator);
	}

	/**
	 * Sort the array range in place and return the array.
	 */
	public static <T> T[] sort(T[] array, int offset, Comparator<? super T> comparator) {
		return sort(array, offset, Integer.MAX_VALUE, comparator);
	}

	/**
	 * Sort the array range in place and return the array.
	 */
	public static <T> T[] sort(T[] array, int offset, int length,
		Comparator<? super T> comparator) {
		return RawArray.acceptSlice(array, offset, length, (o, l) -> {
			Arrays.sort(array, o, o + l, comparator);
		});
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
		return RawArray.equals(Arrays::equals, lhs, lhsOffset, rhs, rhsOffset, length);
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
		return RawArray.hash((h, a, i) -> h.hash(a[i]), array, offset, length);
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
		return RawArray.appendToString((b, a, i) -> b.append(RawArray.<Object>get(a, i)), joiner,
			array, offset, length);
	}

	/**
	 * Returns a custom string representation of the array.
	 */
	@SafeVarargs
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T... array) {
		return toString(stringFn, joiner, array, 0);
	}

	/**
	 * Returns a custom string representation of the array range.
	 */
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T[] array, int offset) {
		return toString(stringFn, joiner, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a custom string representation of the array range.
	 */
	public static <T> String toString(Functions.Function<T, String> stringFn, Joiner joiner,
		T[] array, int offset, int length) {
		if (stringFn == null) return Strings.NULL;
		return RawArray.toString((a, i) -> stringFn.apply(a[i]), joiner, array, offset, length);
	}

	private static <T> Class<T> requireNonPrimitive(Class<T> type) {
		if (type == null || !type.isPrimitive()) return type;
		throw new IllegalArgumentException("Primitives not supported: " + type);
	}
}
