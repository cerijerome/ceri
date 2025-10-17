package ceri.common.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import ceri.common.function.Compares;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;

public class Sets {
	private Sets() {}

	/**
	 * Utility for building sets.
	 */
	public static class Builder<T, S extends Set<T>>
		extends Collectable.Builder<T, S, Builder<T, S>> {

		/**
		 * Create a builder using the list.
		 */
		public static <T, S extends Set<T>> Builder<T, S> of(S set) {
			return new Builder<>(set);
		}

		private Builder(S set) {
			super(set);
		}

		@Override
		public Set<T> wrap() {
			return Immutable.wrap(get());
		}
	}

	/**
	 * Create a builder.
	 */
	@SafeVarargs
	public static <T> Builder<T, Set<T>> build(T value, T... values) {
		return build(Sets::of, value, values);
	}

	/**
	 * Create a builder using the supplier.
	 */
	@SafeVarargs
	public static <T, S extends Set<T>> Builder<T, S> build(Functions.Supplier<S> supplier, T value,
		T... values) {
		return Builder.of(supplier.get()).add(value, values);
	}

	/**
	 * Creates an empty mutable linked hash set.
	 */
	public static <T> LinkedHashSet<T> link() {
		return new LinkedHashSet<>();
	}

	/**
	 * Creates a mutable linked hash set from the collection.
	 */
	public static <T> LinkedHashSet<T> link(Collection<? extends T> collection) {
		return collection == null ? link() : new LinkedHashSet<>(collection);
	}

	/**
	 * Creates an empty mutable tree set.
	 */
	public static <T extends Comparable<? super T>> TreeSet<T> tree() {
		return new TreeSet<>(Compares.nullsFirst());
	}

	/**
	 * Creates a mutable tree set from the collection.
	 */
	public static <T extends Comparable<? super T>> TreeSet<T>
		tree(Collection<? extends T> collection) {
		var set = Sets.<T>tree();
		if (collection != null) set.addAll(collection);
		return set;
	}

	/**
	 * Creates an empty mutable identity hash set.
	 */
	public static <T> Set<T> id() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	/**
	 * Creates an empty mutable set.
	 */
	public static <T> Set<T> of() {
		return new HashSet<>();
	}

	/**
	 * Creates a mutable set from values.
	 */
	@SafeVarargs
	public static <T> Set<T> ofAll(T... values) {
		return of(values, 0);
	}

	/**
	 * Creates a mutable set from array values.
	 */
	public static <T> Set<T> of(T[] array, int offset) {
		return of(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable set from array values.
	 */
	public static <T> Set<T> of(T[] array, int offset, int length) {
		return Collectable.add(of(), array, offset, length);
	}

	/**
	 * Creates a mutable set from iterable values.
	 */
	public static <T> Set<T> of(Iterable<? extends T> values) {
		return Collectable.add(of(), values);
	}

	/**
	 * Creates a mutable set from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Set<U> adaptAll(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adapt(mapper, values, 0);
	}

	/**
	 * Creates a mutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U>
		adapt(Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
			throws E {
		return adapt(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return Collectable.adaptAdd(of(), mapper, array, offset, length);
	}

	/**
	 * Creates a mutable set from transformed iterable values.
	 */
	public static <E extends Exception, T, U> Set<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return Collectable.adaptAdd(of(), mapper, values);
	}

	/**
	 * Creates a mutable set from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> Set<T> convert(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return Collectable.convertAdd(of(), unmapper, map);
	}
}
