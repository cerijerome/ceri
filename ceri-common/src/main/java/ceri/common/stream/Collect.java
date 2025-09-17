package ceri.common.stream;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import ceri.common.array.ArrayUtil;
import ceri.common.array.DynamicArray;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.collection.Maps;
import ceri.common.collection.Sets;
import ceri.common.exception.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Stream collectors.
 */
public class Collect {
	private static final Functions.BiOperator<Object> NO_COMBINER =
		(_, _) -> unsupported("Combining is not supported");
	private static final Collector<?, ?, Object[]> ARRAY = array(Object[]::new);
	private static final Collector<?, List<Object>, List<Object>> LIST =
		of(Lists::of, Collection::add, Immutable::wrap);
	private static final Collector<?, ?, List<?>> SORTED_LIST =
		Reflect.unchecked(sortedList(Comparator.naturalOrder()));
	private static final Collector<?, Set<Object>, Set<Object>> SET =
		of(Sets::of, Collection::add, Immutable::wrap);

	private Collect() {}

	/**
	 * Int stream collectors.
	 */
	public static class Ints {
		/** Collects elements into a primitive array. */
		public static final IntStream.Collector<?, int[]> array =
			new Composed<>(DynamicArray::ints, DynamicArray.OfInt::accept, DynamicArray::truncate);
		/** Collects elements into a sorted primitive array. */
		public static final IntStream.Collector<?, int[]> sortedArray = new Composed<>(
			DynamicArray::ints, DynamicArray.OfInt::accept, a -> ArrayUtil.ints.sort(a.truncate()));
		/** Collects chars into a string. */
		public static final IntStream.Collector<?, String> chars = new Composed<>(
			StringBuilder::new, (b, i) -> b.append((char) i), StringBuilder::toString);
		/** Collects code points into a string. */
		public static final IntStream.Collector<?, String> codePoints = new Composed<>(
			StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::toString);

		private Ints() {}

		/**
		 * A collector instance composed from its functions.
		 */
		record Composed<A, R>(Functions.Supplier<A> supplier,
			Functions.ObjIntConsumer<A> accumulator, Functions.Function<A, R> finisher)
			implements IntStream.Collector<A, R> {}
	}

	/**
	 * Long stream collectors.
	 */
	public static class Longs {
		/** Collects elements into a primitive array. */
		public static final LongStream.Collector<DynamicArray.OfLong, long[]> array =
			new Composed<>(DynamicArray::longs, DynamicArray.OfLong::accept,
				DynamicArray::truncate);
		/** Collects elements into a sorted primitive array. */
		public static final LongStream.Collector<DynamicArray.OfLong, long[]> sortedArray =
			new Composed<>(DynamicArray::longs, DynamicArray.OfLong::accept,
				a -> ArrayUtil.longs.sort(a.truncate()));

		private Longs() {}

		/**
		 * A collector instance composed from its functions.
		 */
		record Composed<A, R>(Functions.Supplier<A> supplier,
			Functions.ObjLongConsumer<A> accumulator, Functions.Function<A, R> finisher)
			implements LongStream.Collector<A, R> {}
	}

	/**
	 * Double stream collectors.
	 */
	public static class Doubles {
		/** Collects elements into a primitive array. */
		public static final DoubleStream.Collector<DynamicArray.OfDouble, double[]> array =
			new Composed<>(DynamicArray::doubles, DynamicArray.OfDouble::accept,
				DynamicArray::truncate);
		/** Collects elements into a sorted primitive array. */
		public static final DoubleStream.Collector<DynamicArray.OfDouble, double[]> sortedArray =
			new Composed<>(DynamicArray::doubles, DynamicArray.OfDouble::accept,
				a -> ArrayUtil.doubles.sort(a.truncate()));

		private Doubles() {}

		/**
		 * A collector instance composed from its functions.
		 */
		record Composed<A, R>(Functions.Supplier<A> supplier,
			Functions.ObjDoubleConsumer<A> accumulator, Functions.Function<A, R> finisher)
			implements DoubleStream.Collector<A, R> {}
	}

	/**
	 * A collector composed of method responses.
	 */
	private record Composed<T, A, R>(Functions.Supplier<A> supplier,
		Functions.BiOperator<A> combiner, Functions.BiConsumer<A, T> accumulator,
		Functions.Function<A, R> finisher, Set<Characteristics> characteristics)
		implements Collector<T, A, R> {}

	/**
	 * Composes a collector from functions.
	 */
	public static <T, A, R> Collector<T, A, R> of(Functions.Supplier<A> supplier,
		Functions.BiConsumer<A, T> accumulator, Functions.Function<A, R> finisher) {
		return new Composed<>(supplier, noCombiner(), accumulator, finisher, Set.of());
	}

	/**
	 * Composes a collector from functions, without a finisher.
	 */
	public static <T, A> Collector<T, A, A> of(Functions.Supplier<A> supplier,
		Functions.BiConsumer<A, T> accumulator) {
		return of(supplier, accumulator, t -> t);
	}

	/**
	 * A combiner that throws unsupported exception.
	 */
	public static <T> Functions.BiOperator<T> noCombiner() {
		return Reflect.unchecked(NO_COMBINER);
	}

	/**
	 * Collects elements into an object array.
	 */
	public static <T> Collector<T, ?, Object[]> array() {
		return Reflect.unchecked(ARRAY);
	}

	/**
	 * Collects elements into an array.
	 */
	public static <R, T extends R> Collector<T, ?, R[]>
		array(Functions.IntFunction<R[]> constructor) {
		return of(() -> DynamicArray.of(constructor), DynamicArray.OfType::accept,
			DynamicArray::truncate);
	}

	/**
	 * Collects elements into a sorted array.
	 */
	public static <R extends Comparable<? super R>, T extends R> Collector<T, ?, R[]>
		sortedArray(Functions.IntFunction<R[]> constructor) {
		return sortedArray(constructor, Comparator.naturalOrder());
	}

	/**
	 * Collects elements into a sorted array.
	 */
	public static <R, T extends R> Collector<T, ?, R[]>
		sortedArray(Functions.IntFunction<R[]> constructor, Comparator<? super R> comparator) {
		return of(() -> DynamicArray.of(constructor), DynamicArray.OfType::accept,
			a -> ArrayUtil.sort(a.truncate(), comparator));
	}

	/**
	 * Collects elements into a typed immutable collection.
	 */
	public static <R, T extends R, C extends Collection<R>> Collector<T, ?, C>
		collection(Immutable.Wrap<C> wrap) {
		return of(wrap::mutable, Collection::add, wrap::apply);
	}

	/**
	 * Collects elements into an immutable list.
	 */
	public static <T> Collector<T, ?, List<T>> list() {
		return Reflect.unchecked(LIST);
	}

	/**
	 * Collects elements into an immutable sorted list.
	 */
	public static <R extends Comparable<? super R>, T extends R> Collector<T, ?, List<R>>
		sortedList() {
		return Reflect.unchecked(SORTED_LIST);
	}

	/**
	 * Collects elements into an immutable sorted list.
	 */
	public static <R, T extends R> Collector<T, ?, List<R>>
		sortedList(Comparator<? super R> comparator) {
		return of(() -> Lists.<R>of(), Collection::add,
			list -> Immutable.wrap(Lists.sort(list, comparator)));
	}

	/**
	 * Collects elements into an immutable set.
	 */
	public static <T> Collector<T, ?, Set<T>> set() {
		return Reflect.unchecked(SET);
	}

	/**
	 * Collects elements into an immutable set.
	 */
	public static <T> Collector<T, ?, Set<T>> set(Functions.Supplier<? extends Set<T>> supplier) {
		return of(supplier, Collection::add, Immutable::wrap);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key.
	 */
	public static <T, K> Collector<T, ?, Map<K, T>>
		map(Functions.Function<? super T, ? extends K> keyMapper) {
		return map(keyMapper, t -> t);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key and value.
	 */
	public static <T, K, V> Collector<T, ?, Map<K, V>> map(
		Functions.Function<? super T, ? extends K> keyMapper,
		Functions.Function<? super T, ? extends V> valueMapper) {
		return map(Maps::of, keyMapper, valueMapper);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key and value.
	 */
	public static <T, K, V> Collector<T, ?, Map<K, V>> map(
		Functions.Supplier<? extends Map<K, V>> supplier,
		Functions.Function<? super T, ? extends K> keyMapper,
		Functions.Function<? super T, ? extends V> valueMapper) {
		return map(Maps.Put.def, supplier, keyMapper, valueMapper);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key and value.
	 */
	public static <T, K, V> Collector<T, ?, Map<K, V>> map(Maps.Put put,
		Functions.Supplier<? extends Map<K, V>> supplier,
		Functions.Function<? super T, ? extends K> keyMapper,
		Functions.Function<? super T, ? extends V> valueMapper) {
		return of(supplier,
			(m, t) -> Maps.Put.put(put, m, keyMapper.apply(t), valueMapper.apply(t)),
			m -> Immutable.wrap(m));
	}

	/**
	 * Collects elements into a typed immutable map by mapping each element to a key.
	 */
	public static <T, K, M extends Map<K, T>> Collector<T, ?, M> map(Immutable.Wrap<M> wrap,
		Functions.Function<? super T, ? extends K> keyMapper) {
		return map(wrap, keyMapper, t -> t);
	}

	/**
	 * Collects elements into a typed immutable map by mapping each element to a key and value.
	 */
	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> map(Immutable.Wrap<M> wrap,
		Functions.Function<? super T, ? extends K> keyMapper,
		Functions.Function<? super T, ? extends V> valueMapper) {
		return map(Maps.Put.def, wrap, keyMapper, valueMapper);
	}

	/**
	 * Collects elements into a typed immutable map by mapping each element to a key and value.
	 */
	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> map(Maps.Put put,
		Immutable.Wrap<M> wrap, Functions.Function<? super T, ? extends K> keyMapper,
		Functions.Function<? super T, ? extends V> valueMapper) {
		return of(wrap::mutable,
			(m, t) -> Maps.Put.put(put, m, keyMapper.apply(t), valueMapper.apply(t)), wrap::apply);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key and and adding the
	 * value to a set.
	 */
	public static <T, K> Collector<T, ?, Map<K, Set<T>>>
		mapSet(Functions.Function<T, K> keyMapper) {
		return mapSet(Maps::of, Sets::of, keyMapper);
	}

	/**
	 * Collects elements into an immutable map by mapping each element to a key and and adding the
	 * value to a set.
	 */
	public static <T, K, M extends Map<K, Set<T>>, S extends Set<T>> Collector<T, ?, Map<K, Set<T>>>
		mapSet(Functions.Supplier<M> mapSupplier, Functions.Supplier<S> setSupplier,
			Functions.Function<T, K> keyMapper) {
		return of(mapSupplier,
			(m, t) -> m.computeIfAbsent(keyMapper.apply(t), _ -> setSupplier.get()).add(t),
			Collections::unmodifiableMap);
	}

	private static Object unsupported(String message) {
		throw Exceptions.unsupportedOp(message);
	}
}
