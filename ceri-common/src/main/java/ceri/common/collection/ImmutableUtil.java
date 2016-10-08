package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods for creating immutable objects.
 */
public class ImmutableUtil {

	private ImmutableUtil() {}

	/**
	 * Creates an immutable iterable wrapper that returns an immutable iterator.
	 */
	public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
		return () -> ImmutableUtil.iterator(iterable.iterator());
	}

	/**
	 * Creates an immutable iterator wrapper.
	 */
	public static <T> Iterator<T> iterator(final Iterator<T> iterator) {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Iterator is immutable.");
			}
		};
	}

	/**
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		if (set.isEmpty()) return Collections.emptySet();
		return Collections.unmodifiableSet(new LinkedHashSet<>(set));
	}

	/**
	 * Copies a map of objects into an immutable LinkedHashMap.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map) {
		if (map.isEmpty()) return Collections.emptyMap();
		return Collections.unmodifiableMap(new LinkedHashMap<>(map));
	}

	/**
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> list) {
		if (list.isEmpty()) return Collections.emptyList();
		return Collections.unmodifiableList(new ArrayList<>(list));
	}

	/**
	 * Copies a stream of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> collectAsSet(Stream<? extends T> stream) {
		return Collections.<T>unmodifiableSet(stream.collect(Collectors
			.toCollection(LinkedHashSet::new)));
	}

	/**
	 * Copies a stream of objects into an immutable ArrayList.
	 */
	public static <T> List<T> collectAsList(Stream<? extends T> stream) {
		return Collections.unmodifiableList(stream.collect(Collectors.toList()));
	}

	/**
	 * Copies an array of objects into an immutable ArrayList.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		if (array.length == 0) return Collections.emptyList();
		List<T> list = new ArrayList<>();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Copies an array of objects into an immutable HashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		if (array.length == 0) return Collections.emptySet();
		Set<T> set = new LinkedHashSet<>();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
	}

	@SafeVarargs
	public static <F, T> List<T> convertAsList(Function<? super F, ? extends T> fn, F... fs) {
		return convertAsList(fn, Arrays.asList(fs));
	}

	public static <F, T> List<T> convertAsList(Function<? super F, ? extends T> fn, Iterable<F> fs) {
		List<T> ts = new ArrayList<>();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableList(ts);
	}

	@SafeVarargs
	public static <F, T> Set<T> convertAsSet(Function<? super F, ? extends T> fn, F... fs) {
		return convertAsSet(fn, Arrays.asList(fs));
	}

	public static <F, T> Set<T> convertAsSet(Function<? super F, ? extends T> fn, Iterable<F> fs) {
		Set<T> ts = new LinkedHashSet<>();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableSet(ts);
	}

	@SafeVarargs
	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn, T... ts) {
		return convertAsMap(fn, Arrays.asList(ts));
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Collection<T> ts) {
		return Collections.unmodifiableMap(ts.stream().collect(Collectors.toMap( //
			fn, Function.identity(), StreamUtil.mergeError(), LinkedHashMap::new)));
	}

	public static <K, T extends Enum<T>> Map<K, T> enumMap(Function<T, K> fn, Class<T> cls) {
		return convertAsMap(fn, EnumSet.allOf(cls));
	}

	public static <T extends Enum<T>> Set<T> enumSet(T one) {
		return Collections.unmodifiableSet(EnumSet.of(one));
	}

	@SafeVarargs
	public static <T extends Enum<T>> Set<T> enumSet(T first, T... rest) {
		return Collections.unmodifiableSet(EnumSet.of(first, rest));
	}

}
