package ceri.common.array;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import ceri.common.collection.Iterables;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.common.util.Hasher;

/**
 * Operations on untyped arrays; calls with non-arrays have undefined behavior.
 */
public class RawArray {
	private RawArray() {}

	/**
	 * Represents a ranged view onto an array.
	 */
	public record Sub<T>(T array, int offset, int length) {
		/**
		 * Create an instance with validated range.
		 */
		public static <T> Sub<T> of(T array, int offset, int length) {
			int arrayLen = RawArray.length(array);
			offset = Maths.limit(offset, 0, arrayLen);
			length = Maths.limit(length, 0, arrayLen - offset);
			return new Sub<>(array, offset, length);
		}

		/**
		 * The range end index.
		 */
		public int to() {
			return offset() + length();
		}
	}

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
		return cls == null ? null : Reflect.unchecked(ofType(cls, 0).getClass());
	}

	/**
	 * Returns the component class of an array type, or null.
	 */
	public static <T> Class<T> componentType(Class<?> cls) {
		return cls == null ? null : Reflect.unchecked(cls.getComponentType());
	}

	/**
	 * Creates an array of given size.
	 */
	public static <T> T ofType(Class<?> type, int size) {
		return type == null ? null :
			Reflect.unchecked(java.lang.reflect.Array.newInstance(type, size));
	}

	/**
	 * Returns the array length, or 0 if null.
	 */
	public static int length(Object array) {
		return array == null ? 0 : java.lang.reflect.Array.getLength(array);
	}

	/**
	 * Returns true if the array is null or empty.
	 */
	public static boolean isEmpty(Object array) {
		return length(array) == 0;
	}

	/**
	 * Sets the value at index.
	 */
	public static void set(Object array, int index, Object value) {
		java.lang.reflect.Array.set(array, index, value);
	}

	/**
	 * Gets the value at index.
	 */
	public static <T> T get(Object array, int index) {
		return Reflect.unchecked(java.lang.reflect.Array.get(array, index));
	}

	/**
	 * Returns true if the index is inside the array.
	 */
	public static boolean in(Object array, int index) {
		if (array == null || index < 0) return false;
		return index < length(array);
	}

	/**
	 * Returns an iterable type for the array.
	 */
	public static Iterable<Object> iterable(Object array, int offset, int length) {
		return Iterables.of(iterator(array, offset, length));
	}

	/**
	 * Returns an iterator for the array.
	 */
	public static Iterator<Object> iterator(Object array, int offset, int length) {
		return applySlice(array, offset, length, (o, l) -> new Iterator<Object>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return Maths.within(i, 0, l - 1);
			}

			@Override
			public Object next() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(array, o + i++);
			}
		});
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
		acceptBiSlice(src, srcOffset, length, dest, destOffset, length, (lo, ll, ro, rl) -> {
			int n = Math.min(ll, rl);
			if (n > 0) System.arraycopy(src, lo, dest, ro, n);
		});
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
		int arrayLen = length(array);
		if (arrayLen == length) return array;
		return copyOf(constructor, array, length);
	}

	/**
	 * Copies values from one array type to another using Array.get/set().
	 */
	public static Object arrayCopy(Object src, int srcOffset, Object dest, int destOffset,
		int length) {
		if (src == null || dest == null) return dest;
		acceptBiSlice(src, srcOffset, length, dest, destOffset, length, (lo, ll, ro, rl) -> {
			int n = Math.min(ll, rl);
			while (n-- > 0)
				set(dest, ro++, get(src, lo++));
		});
		return dest;
	}

	/**
	 * Passes a bounded range to the consumer.
	 */
	public static <E extends Exception, T, R> R applySlice(T array, int offset, int length,
		Excepts.IntBiFunction<E, R> function) throws E {
		if (array == null) return null;
		return ArrayUtil.applySlice(length(array), offset, length, function);
	}

	/**
	 * Passes a bounded range to the consumer.
	 */
	public static <E extends Exception, T> T acceptSlice(T array, int offset, int length,
		Excepts.IntBiConsumer<E> consumer) throws E {
		if (array != null) ArrayUtil.acceptSlice(length(array), offset, length, consumer);
		return array;
	}

	/**
	 * Iterates over array range.
	 */
	public static <E extends Exception, T> T acceptIndexes(T array, int offset, int length,
		Excepts.IntConsumer<E> consumer) throws E {
		if (consumer == null) return array;
		return acceptSlice(array, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				consumer.accept(o + i);
		});
	}

	/**
	 * Passes bounded ranges to the function and returns the result.
	 */
	public static <E extends Exception, T, R> R applyBiSlice(T lArray, int lOffset, int lLength,
		T rArray, int rOffset, int rLength, ArrayUtil.BiSliceFunction<E, R> function) throws E {
		if (function == null) return null;
		return ArrayUtil.applyBiSlice(length(lArray), lOffset, lLength, length(rArray), rOffset,
			rLength, function);
	}

	/**
	 * Passes bounded ranges to the consumer.
	 */
	public static <E extends Exception, T> void acceptBiSlice(T lArray, int lOffset, int lLength,
		T rArray, int rOffset, int rLength, ArrayUtil.BiSliceConsumer<E> consumer) throws E {
		if (consumer == null) return;
		ArrayUtil.acceptBiSlice(length(lArray), lOffset, lLength, length(rArray), rOffset, rLength,
			consumer);
	}

	/**
	 * Insert a bounded array range into an array.
	 */
	public static <T> T insert(Functions.IntFunction<T> constructor, T src, int srcOffset, T dest,
		int destOffset, int length) {
		if (constructor == null || dest == null || src == null) return dest;
		return applyBiSlice(src, srcOffset, length, dest, destOffset, 0, (lo, ll, ro, _) -> {
			if (length == 0) return dest;
			var inserted = insert(constructor, dest, ro, ll);
			System.arraycopy(src, lo, inserted, ro, ll);
			return inserted;
		});
	}

	/**
	 * Insert an empty range into an array.
	 */
	public static <T> T insert(Functions.IntFunction<T> constructor, T array, int offset,
		int length) {
		if (constructor == null || array == null || length <= 0) return array;
		int arrayLen = length(array);
		offset = Maths.limit(offset, 0, arrayLen);
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
		int length = length(values);
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
		return applyBiSlice(array, arrayOffset, arrayLen, values, valuesOffset, valuesLen,
			(ao, al, vo, vl) -> {
				for (int i = 0; i <= al - vl; i++)
					if (equalsFn.equals(array, ao + i, ao + i + vl, values, vo, vo + vl)) return i;
				return -1;
			});
	}

	/**
	 * Finds the last index of a bounded array using an equality function. Returns -1 if not found.
	 */
	public static <T> int lastIndexOf(Equals<T> equalsFn, T array, int arrayOffset, int arrayLen,
		T values, int valuesOffset, int valuesLen) {
		if (array == null || values == null) return -1;
		return applyBiSlice(array, arrayOffset, arrayLen, values, valuesOffset, valuesLen,
			(ao, al, vo, vl) -> {
				for (int i = al - vl; i >= 0; i--)
					if (equalsFn.equals(array, ao + i, ao + i + vl, values, vo, vo + vl)) return i;
				return -1;
			});
	}

	/**
	 * Copies a bounded array into a new boxed array.
	 */
	public static Object[] boxed(Object array, int offset, int length) {
		return boxed(Object[]::new, array, offset, length);
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
				boxed[i] = get(array, o + i);
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
				set(unboxed, i, array[o + i]);
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
			set(unboxed, i++, c);
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
				set(unboxed, i, list.get(o + i));
			return unboxed;
		});
	}

	/**
	 * Reverses a bounded array range with an index swap function.
	 */
	public static <T> T reverse(Functions.ObjBiIntConsumer<T> swapper, T array, int offset,
		int length) {
		if (swapper == null) return array;
		return acceptSlice(array, offset, length, (o, l) -> {
			for (int i = 0; i < l / 2; i++)
				swapper.accept(array, o + i, o + l - i - 1);
		});
	}

	/**
	 * Returns true if the bounded array ranges are equals, using an equality function.
	 */
	public static <T> boolean equals(Equals<T> equalsFn, T lhs, int lhsOffset, T rhs, int rhsOffset,
		int length) {
		if (equalsFn == null) return false;
		if (lhs == null || rhs == null) return lhs == rhs;
		return applyBiSlice(lhs, lhsOffset, length, rhs, rhsOffset, length, (lo, ll, ro, rl) -> {
			if (ll != rl) return false;
			return equalsFn.equals(lhs, lo, lo + ll, rhs, ro, ro + rl);
		});
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
		if (array == null || joiner == null) return Strings.NULL;
		return applySlice(array, offset, length, (o, l) -> {
			return joiner.joinIndex((b, i) -> appendFn.accept(b, array, i), o, l);
		});
	}

	/**
	 * Joins bounded array elements into a string.
	 */
	public static <T> String toString(Functions.ObjIntFunction<? super T, ? super String> stringFn,
		Joiner joiner, T array, int offset, int length) {
		if (stringFn == null || array == null || joiner == null) return Strings.NULL;
		return applySlice(array, offset, length, (o, l) -> {
			return joiner.joinIndex((b, i) -> b.append(stringFn.apply(array, i)), o, l);
		});
	}
}
