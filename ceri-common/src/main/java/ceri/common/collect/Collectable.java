package ceri.common.collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable.Wrap;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Collection type support.
 */
public class Collectable {
	private Collectable() {}

	/**
	 * Collection filters.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Predicate that returns true if a collection contains the value.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Collection<T>> has(T value) {
			return ts -> ts != null && ts.contains(value);
		}

		/**
		 * Predicate that returns true if a collection contains all the values.
		 */
		@SafeVarargs
		public static <E extends Exception, T> Excepts.Predicate<E, Collection<T>>
			hasAll(T... values) {
			if (values == null) return Filters.yes();
			return hasAll(Sets.ofAll(values));
		}

		/**
		 * Predicate that returns true if a collection contains all the values.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Collection<T>>
			hasAll(Collection<? extends T> values) {
			if (values == null) return Filters.yes();
			return ts -> ts != null && ts.containsAll(values);
		}
	}

	/**
	 * Utility for building collections.
	 */
	public static class Builder<T, C extends Collection<T>, B extends Builder<T, C, B>> {
		private final C collection;

		public static <T, C extends Collection<T>> Collect<T, C> of(C collection) {
			return new Collect<>(collection);
		}

		public static class Collect<T, C extends Collection<T>>
			extends Builder<T, C, Collect<T, C>> {
			private Collect(C collection) {
				super(collection);
			}
		}

		protected Builder(C collection) {
			this.collection = collection;
		}

		/**
		 * Adds the values to the collection.
		 */
		@SafeVarargs
		public final B add(T value, T... values) {
			get().add(value);
			return add(values);
		}

		/**
		 * Adds the values to the collection.
		 */
		public final B add(T[] values) {
			if (values != null) Collections.addAll(get(), values);
			return typedThis();
		}

		/**
		 * Adds the values to the collection.
		 */
		public final B add(Iterable<? extends T> iterable) {
			Collectable.add(get(), iterable);
			return typedThis();
		}

		/**
		 * Apply the populator function to the collection.
		 */
		public <E extends Exception> B apply(Excepts.Consumer<E, ? super C> populator) throws E {
			if (populator != null) populator.accept(get());
			return typedThis();
		}

		/**
		 * Returns the underlying collection.
		 */
		public C get() {
			return collection;
		}

		/**
		 * Returns the collection wrapped as unmodifiable.
		 */
		public Collection<T> wrap() {
			return Wrap.<T>collect().apply(get());
		}

		private B typedThis() {
			return Reflect.unchecked(this);
		}
	}

	/**
	 * Create a builder using the supplier.
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> Builder.Collect<T, C>
		build(Functions.Supplier<C> supplier, T value, T... values) {
		return Builder.of(supplier.get()).add(value, values);
	}

	// access

	/**
	 * Returns true if the collection is null or has no elements.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Returns true if the collection is non-null and has elements.
	 */
	public static boolean nonEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * Returns the size of the map, or 0 if null.
	 */
	public static int size(Collection<?> collection) {
		return collection == null ? 0 : collection.size();
	}

	// add

	/**
	 * Adds a value and returns the collection if the value is non-null.
	 */
	public static <T, C extends Collection<T>> C safeAdd(C dest, T value) {
		return value == null ? dest : addTo(dest, value);
	}

	/**
	 * Adds a value and returns the collection.
	 */
	public static <T, C extends Collection<T>> C addTo(C dest, T value) {
		if (dest != null) dest.add(value);
		return dest;
	}

	/**
	 * Adds values to the collection.
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C addAll(C dest, T... values) {
		return add(dest, values, 0);
	}

	/**
	 * Adds the array region to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, T[] src, int offset) {
		return add(dest, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Adds the array region to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, T[] src, int offset, int length) {
		if (dest == null || src == null) return dest;
		RawArray.acceptSlice(src, offset, length,
			(o, l) -> dest.addAll(Arrays.asList(src).subList(o, o + l)));
		return dest;
	}

	/**
	 * Adds iterable values to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, Iterable<? extends T> src) {
		if (dest == null || src == null) return dest;
		for (var value : src)
			dest.add(value);
		return dest;
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAddAll(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... src) throws E {
		return adaptAdd(dest, mapper, src, 0);
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] src, int offset)
		throws E {
		return adaptAdd(dest, mapper, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] src, int offset,
		int length) throws E {
		if (dest == null || src == null || mapper == null) return dest;
		RawArray.acceptSlice(src, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				dest.add(mapper.apply(src[o + i]));
		});
		return dest;
	}

	/**
	 * Adds transformed iterable values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> src)
		throws E {
		if (dest == null || src == null || mapper == null) return dest;
		for (var value : src)
			dest.add(mapper.apply(value));
		return dest;
	}

	/**
	 * Adds transformed map entries to the collection.
	 */
	public static <E extends Exception, K, V, T, C extends Collection<T>> C convertAdd(C dest,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> src)
		throws E {
		if (dest == null || src == null || unmapper == null) return dest;
		for (var e : src.entrySet())
			dest.add(unmapper.apply(e.getKey(), e.getValue()));
		return dest;
	}

	// remove

	/**
	 * Removes all the given elements.
	 */
	@SafeVarargs
	public static <T, C extends Collection<? super T>> C removeAll(C collection, T... ts) {
		if (!isEmpty(collection) && !Array.isEmpty(ts)) collection.removeAll(Arrays.asList(ts));
		return collection;
	}
}
