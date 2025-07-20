package ceri.common.array;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.common.text.Joiner;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.Hasher;

/**
 * Operations on untyped arrays; calls with non-arrays have undefined behavior.
 */
public class RawArrays {
	private RawArrays() {}

	/**
	 * Array equality function; matches Arrays.equals().
	 */
	@FunctionalInterface
	public interface Equals<T> {
		boolean equals(T lhs, int lhsFrom, int lhsTo, T rhs, int rhsFrom, int rhsTo);
	}

	/**
	 * Returns true if the given object is a non-null array.
	 */
	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}

	/**
	 * Returns the array component class, or null.
	 */
	public static <T> Class<T> arrayType(Class<?> cls) {
		return cls == null ? null : BasicUtil.unchecked(empty(cls).getClass());
	}

	/**
	 * Returns the component class of an array type, or null.
	 */
	public static <T> Class<T> componentType(Class<?> cls) {
		return cls == null ? null : BasicUtil.unchecked(cls.getComponentType());
	}

	/**
	 * Creates an array of given size.
	 */
	public static <T> T array(Class<?> type, int size) {
		return type == null ? null : BasicUtil.unchecked(Array.newInstance(type, size));
	}

	/**
	 * Creates an array of given size.
	 */
	public static <T> T empty(Class<?> type) {
		return array(type, 0);
	}

	/**
	 * Returns the array length, or 0 if null.
	 */
	public static int length(Object array) {
		return array == null ? 0 : Array.getLength(array);
	}

	/**
	 * Returns true if the array is null or empty.
	 */
	public static boolean isEmpty(Object array) {
		return length(array) == 0;
	}

	/**
	 * Returns true if the index is inside the array.
	 */
	public static boolean in(Object array, int index) {
		if (array == null || index < 0) return false;
		return index < Array.getLength(array);
	}

	/**
	 * Copies from source to destination, returning the destination array. Bounds are limited.
	 */
	public static <T> T copy(T src, T dest) {
		return copy(src, 0, dest, 0);
	}

	/**
	 * Copies from source to destination, returning the destination array. Bounds are limited.
	 */
	public static <T> T copy(T src, int srcOffset, T dest, int destOffset) {
		return copy(src, srcOffset, dest, destOffset, Integer.MAX_VALUE);
	}

	/**
	 * Copies from source to destination, returning the destination array. Bounds are limited.
	 */
	public static <T> T copy(T src, int srcOffset, T dest, int destOffset, int length) {
		if (src == null || dest == null) return dest;
		int srcLen = Array.getLength(src);
		srcOffset = MathUtil.limit(srcOffset, 0, srcLen);
		int destLen = Array.getLength(dest);
		destOffset = MathUtil.limit(destOffset, 0, destLen);
		length = MathUtil.min(length, srcLen, destLen);
		if (length > 0) System.arraycopy(src, srcOffset, dest, destOffset, length);
		return dest;
	}

	/**
	 * Copies an array up to given length.
	 */
	public static <T> T copyOf(Functions.IntFunction<T> constructor, T array, int length) {
		return copyOf(constructor, array, 0, length);
	}

	/**
	 * Copies an array up to given length.
	 */
	public static <T> T copyOf(Functions.IntFunction<T> constructor, T array, int offset,
		int length) {
		if (constructor == null) return null;
		return copy(array, offset, constructor.apply(Math.max(0, length)), 0);
	}

	/**
	 * Copies an array up to given length if the length is different.
	 */
	public static <T> T resize(Functions.IntFunction<T> constructor, T array, int length) {
		length = Math.max(0, length);
		if (array == null) return constructor.apply(length);
		int arrayLen = Array.getLength(array);
		if (arrayLen == length) return array;
		return copyOf(constructor, array, length);
	}

	/**
	 * Copies values from one array type to another using Array.get/set().
	 */
	public static Object arrayCopy(Object src, int srcOffset, Object dest, int destOffset,
		int length) {
		if (src == null || dest == null) return dest;
		int srcLen = Array.getLength(src);
		srcOffset = MathUtil.limit(srcOffset, 0, srcLen);
		int destLen = Array.getLength(dest);
		destOffset = MathUtil.limit(destOffset, 0, destLen);
		length = MathUtil.min(length, srcLen, destLen);
		while (length-- > 0)
			Array.set(src, srcOffset++, Array.get(dest, destOffset++));
		return dest;
	}

	/**
	 * Passes a validated range to the consumer.
	 */
	public static <E extends Exception, T, R> R applySlice(T array, int offset, int length,
		Excepts.IntBiFunction<E, R> function) throws E {
		if (array == null) return null;
		return ArrayUtil.applySlice(Array.getLength(array), offset, length, function);
	}

	/**
	 * Passes a validated range to the consumer.
	 */
	public static <E extends Exception, T> T acceptSlice(T array, int offset, int length,
		Excepts.IntBiConsumer<E> consumer) throws E {
		if (array == null) return array;
		ArrayUtil.acceptSlice(Array.getLength(array), offset, length, consumer);
		return array;
	}

	/**
	 * Iterates over array range.
	 */
	public static <E extends Exception, T> T acceptIndexes(T array, int offset, int length,
		Excepts.IntConsumer<E> consumer) throws E {
		return acceptSlice(array, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				consumer.accept(o + i);
		});
	}

	/**
	 * Insert a bounded array range into an array.
	 */
	public static <T> T insert(Functions.IntFunction<T> constructor, T lhs, int lhsOffset, T rhs,
		int rhsOffset, int length) {
		if (constructor == null || lhs == null || rhs == null) return lhs;
		int lhsLen = Array.getLength(lhs);
		lhsOffset = MathUtil.limit(lhsOffset, 0, lhsLen);
		int rhsLen = Array.getLength(rhs);
		rhsOffset = MathUtil.limit(rhsOffset, 0, rhsLen);
		length = MathUtil.limit(length, 0, rhsLen - rhsOffset);
		var inserted = insert(constructor, lhs, lhsOffset, length);
		if (length > 0) System.arraycopy(rhs, rhsOffset, inserted, lhsOffset, length);
		return inserted;
	}

	/**
	 * Insert an empty range into an array.
	 */
	public static <T> T insert(Functions.IntFunction<T> constructor, T array, int offset,
		int length) {
		if (constructor == null || array == null || length <= 0) return array;
		int arrayLen = Array.getLength(array);
		offset = MathUtil.limit(offset, 0, arrayLen);
		var inserted = constructor.apply(arrayLen + length);
		if (offset > 0) System.arraycopy(array, 0, inserted, 0, offset);
		if (offset < arrayLen)
			System.arraycopy(array, offset, inserted, offset + length, arrayLen - offset);
		return inserted;
	}

	/**
	 * Copy values using a consumer for each index.
	 */
	public static <T, U> T copyValues(Functions.IntFunction<T> constructor, U values,
		Functions.ObjIntConsumer<T> indexConsumer) {
		if (constructor == null || values == null || indexConsumer == null) return null;
		int length = Array.getLength(values);
		var array = constructor.apply(length);
		for (int i = 0; i < length; i++)
			indexConsumer.accept(array, i);
		return array;
	}

	/**
	 * Finds the first index of a bounded array using an equality function. Returns -1 if not found.
	 */
	public static <T> int indexOf(Equals<T> equalsFn, T array, int arrayOffset, int arrayLen,
		T values, int valuesOffset, int valuesLen) {
		if (array == null || values == null) return -1;
		int actualArrayLen = Array.getLength(array);
		arrayOffset = MathUtil.limit(arrayOffset, 0, actualArrayLen);
		arrayLen = MathUtil.limit(arrayLen, 0, actualArrayLen - arrayOffset);
		int actualValuesLen = Array.getLength(values);
		valuesOffset = MathUtil.limit(valuesOffset, 0, actualValuesLen);
		valuesLen = MathUtil.limit(valuesLen, 0, actualValuesLen - valuesOffset);
		for (int i = arrayOffset; i <= arrayLen - valuesLen; i++)
			if (equalsFn.equals(array, i, i + valuesLen, values, valuesOffset,
				valuesOffset + valuesLen)) return i;
		return -1;
	}

	/**
	 * Finds the last index of a bounded array using an equality function. Returns -1 if not found.
	 */
	public static <T> int lastIndexOf(Equals<T> equalsFn, T array, int arrayOffset, int arrayLen,
		T values, int valuesOffset, int valuesLen) {
		if (array == null || values == null) return -1;
		int actualArrayLen = Array.getLength(array);
		arrayOffset = MathUtil.limit(arrayOffset, 0, actualArrayLen);
		arrayLen = MathUtil.limit(arrayLen, 0, actualArrayLen - arrayOffset);
		int actualValuesLen = Array.getLength(values);
		valuesOffset = MathUtil.limit(valuesOffset, 0, actualValuesLen);
		valuesLen = MathUtil.limit(valuesLen, 0, actualValuesLen - valuesOffset);
		for (int i = arrayLen - valuesLen; i >= arrayOffset; i--)
			if (equalsFn.equals(array, i, i + valuesLen, values, valuesOffset,
				valuesOffset + valuesLen)) return i;
		return -1;
	}

	/**
	 * Copies a bounded array into a new boxed array.
	 */
	public static <C> C[] boxed(Functions.IntFunction<C[]> boxedConstructor, Object array,
		int offset, int length) {
		if (array == null || boxedConstructor == null) return null;
		return applySlice(array, offset, length, (o, l) -> {
			C[] boxed = boxedConstructor.apply(l);
			for (int i = 0; i < l; i++)
				boxed[i] = BasicUtil.unchecked(Array.get(array, o + i));
			return boxed;
		});
	}

	/**
	 * Copies a bounded boxed array into a new array.
	 */
	public static <T, C> T unboxed(Functions.IntFunction<T> constructor, C[] array, int offset,
		int length) {
		if (array == null || constructor == null) return null;
		return applySlice(array, offset, length, (o, l) -> {
			T unboxed = constructor.apply(l);
			for (int i = 0; i < l; i++)
				Array.set(unboxed, i, array[o + i]);
			return unboxed;
		});
	}

	/**
	 * Copies a collection into a new array.
	 */
	public static <T, C> T unboxed(Functions.IntFunction<T> constructor, Collection<C> collection) {
		if (collection == null || constructor == null) return null;
		T unboxed = constructor.apply(collection.size());
		int i = 0;
		for (var c : collection)
			Array.set(unboxed, i++, c);
		return unboxed;
	}

	/**
	 * Copies a bounded list range into a new array.
	 */
	public static <T, C> T unboxed(Functions.IntFunction<T> constructor, List<C> list, int offset,
		int length) {
		if (list == null || constructor == null) return null;
		return ArrayUtil.applySlice(list.size(), offset, length, (o, l) -> {
			T unboxed = constructor.apply(l);
			for (int i = 0; i < l; i++)
				Array.set(unboxed, i, list.get(o + i));
			return unboxed;
		});
	}

	/**
	 * Reverses a bounded array range with an index swap function.
	 */
	public static <T> T reverse(Functions.ObjBiIntConsumer<T> swapper, T array, int offset,
		int length) {
		return acceptSlice(array, offset, length, (o, l) -> {
			for (int i = 0; i < l / 2; i++)
				swapper.accept(array, o + i, o + l - i - 1);
		});
	}

	/**
	 * Returns true if the bounded array ranges are equals, using an equality function.
	 */
	public static <T> boolean equals(Equals<T> equalsFn, T lhs, int lhsOffset, T rhs,
		int rhsOffset, int length) {
		if (lhs == null && rhs == null) return true;
		if (lhs == null || rhs == null) return false;
		int lhsLen = Array.getLength(lhs);
		lhsOffset = MathUtil.limit(lhsOffset, 0, lhsLen);
		int rhsLen = Array.getLength(rhs);
		rhsOffset = MathUtil.limit(rhsOffset, 0, rhsLen);
		return equalsFn.equals(lhs, lhsOffset, Math.min(lhsOffset + length, lhsLen), rhs, rhsOffset,
			Math.min(rhsOffset + length, rhsLen));
	}

	/**
	 * Calculates the hash of a bounded array range, using an index hashing function.
	 */
	public static <T> int hash(Functions.BiObjIntConsumer<Hasher, T> hashFn, T array, int offset,
		int length) {
		if (array == null) return 0;
		var hasher = Hasher.of();
		acceptIndexes(array, offset, length, i -> hashFn.accept(hasher, array, i));
		return hasher.code();
	}

	/**
	 * Joins bounded array elements into a string using a builder.
	 */
	public static <T> String appendToString(
		Functions.BiObjIntConsumer<StringBuilder, ? super T> appendFn, Joiner joiner, T array,
		int offset, int length) {
		if (array == null || joiner == null) return StringUtil.NULL;
		return applySlice(array, offset, length, (o, l) -> {
			return joiner.joinIndex((b, i) -> appendFn.accept(b, array, i), o, l);
		});
	}

	/**
	 * Joins bounded array elements into a string.
	 */
	public static <T> String toString(Functions.ObjIntFunction<? super T, ? super String> stringFn,
		Joiner joiner, T array, int offset, int length) {
		if (array == null || joiner == null) return StringUtil.NULL;
		return applySlice(array, offset, length, (o, l) -> {
			return joiner.joinIndex((b, i) -> b.append(stringFn.apply(array, i)), o, l);
		});
	}
}
