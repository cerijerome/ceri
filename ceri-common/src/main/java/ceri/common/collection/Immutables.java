package ceri.common.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ceri.common.array.RawArrays;
import ceri.common.function.Excepts;

/**
 * Support for immutable types.
 */
public class Immutables {
	private Immutables() {}

	/**
	 * Wraps an array as an unmodifiable list.
	 */
	@SafeVarargs
	public static <T> List<T> wrapAsList(T... array) {
		if (array == null || array.length == 0) return List.of();
		return Collections.unmodifiableList(Arrays.asList(array));
	}

	/**
	 * Wraps an array region as an unmodifiable list.
	 */
	public static <T> List<T> wrapAsList(T[] array, int offset) {
		return wrapAsList(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Wraps an array region as an unmodifiable list.
	 */
	public static <T> List<T> wrapAsList(T[] array, int offset, int length) {
		if (array == null) return List.of();
		return RawArrays.applySlice(array, offset, length,
			(o, l) -> Collections.unmodifiableList(Arrays.asList(array).subList(o, o + l)));
	}

	// set creation

	/**
	 * Creates an immutable set from elements.
	 */
	public static <T> Set<T> set(Iterable<? extends T> iterable) {
		return Collections.unmodifiableSet(Iterables.set(iterable));
	}

	// map creation

	/**
	 * Creates an immutable map by mapping each element to a key.
	 */
	public static <E extends Exception, T, K> Map<K, T> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return Collections.unmodifiableMap(Iterables.map(keyMapper, iterable));
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return Collections.unmodifiableMap(Iterables.map(keyMapper, valueMapper, iterable));
	}

	/**
	 * Creates an immutable map by mapping each element to a key, if absent.
	 */
	public static <E extends Exception, T, K> Map<K, T> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return Collections.unmodifiableMap(Iterables.mapIfAbsent(keyMapper, iterable));
	}

	/**
	 * Creates an immutable map by mapping each element to a key and value, if absent.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return Collections.unmodifiableMap(Iterables.mapIfAbsent(keyMapper, valueMapper, iterable));
	}
}
