package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertIterator;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNoSuchElement;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.fail;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.stream.Collector;
import org.junit.Test;
import ceri.common.collection.Immutable;
import ceri.common.collection.Maps;
import ceri.common.collection.Immutable.Wrap;
import ceri.common.collection.Lists;
import ceri.common.comparator.Comparators;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
import ceri.common.test.CallSync;
import ceri.common.text.Joiner;

public class StreamBehavior {
	private static final Stream<RuntimeException, Integer> empty = Stream.empty();
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = Immutable.list();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Iterator<Integer> nullIterator = null;
	private static final Spliterator<Integer> nullSpliterator = null;
	private static final java.util.stream.Stream<Integer> nullStream = null;
	private static final Set<Integer> nullSet = null;
	private static final SortedSet<Integer> nullSortSet = null;
	private static final NavigableSet<Integer> nullNavSet = null;
	private static final Set<Integer> set = Immutable.setOf(-1, null, 1);
	private static final Map<Integer, String> nullMap = null;
	private static final SortedMap<Integer, String> nullSortMap = null;
	private static final NavigableMap<Integer, String> nullNavMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map =
		Immutable.of(Wrap.seqMap(), -1, "A", null, "B", 1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Predicate<Integer> no = i -> false;
	private static final Functions.Predicate<Integer> yes = i -> true;
	private static final Functions.Predicate<Integer> pred = i -> i != null && i >= 0;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.ToIntFunction<Integer> intFn = i -> i == null ? 0 : -i;
	private static final Functions.ToLongFunction<Integer> longFn = i -> i == null ? 0 : -i;
	private static final Functions.ToDoubleFunction<Integer> doubleFn = i -> i == null ? 0 : -i;	
	private static final Functions.Function<Integer, Iterable<Integer>> expandFn =
		i -> i == null ? null : Lists.ofAll(-i, null, i);
	private static final Functions.Function<Integer, Stream<RuntimeException, Integer>> flatFn =
			i -> i == null ? null : Streams.of(-i, null, i);
	
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Functions.Predicate<Integer> nullPred = null;
	private static final Comparator<Integer> comp = Comparators.nullsLast();

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static <T> Functions.Supplier<T> nullFn() {
		return null;
	}

	private static Stream<RuntimeException, Integer> stream() {
		return Stream.ofAll(-1, null, 1);
	}

	private static Stream<IOException, Integer> ioStream() {
		return Stream.<IOException, Integer>ofAll(-1, null, 1).filter(i -> {
			if (i == null) throw new IOException();
			return true;
		});
	}

	@Test
	public void testEmpty() throws Exception {
		assertStream(empty);
		assertSame(empty, Stream.empty());
	}

	@Test
	public void testOf() throws Exception {
		assertStream(Stream.ofAll(nullArray));
		assertStream(Stream.ofAll(emptyArray));
		assertStream(Stream.ofAll(array()), -1, null, 1);
		assertStream(Stream.of(nullArray, 1));
		assertStream(Stream.of(emptyArray, 1));
		assertStream(Stream.of(array(), 1), null, 1);
	}

	@Test
	public void testFrom() throws Exception {
		assertStream(Stream.from(nullList));
		assertStream(Stream.from(emptyList));
		assertStream(Stream.from(list), -1, null, 1);
		assertStream(Stream.from(nullIterator));
		assertStream(Stream.from(emptyList.iterator()));
		assertStream(Stream.from(list.iterator()), -1, null, 1);
		assertStream(Stream.from(nullStream));
		assertStream(Stream.from(emptyList.stream()));
		assertStream(Stream.from(list.stream()), -1, null, 1);
		assertStream(Stream.from(nullSpliterator));
		assertStream(Stream.from(emptyList.spliterator()));
		assertStream(Stream.from(list.spliterator()), -1, null, 1);
	}

	@Test
	public void shouldFilterElements() throws Exception {
		assertStream(empty.filter(null));
		assertStream(empty.filter(no));
		assertStream(empty.filter(yes));
		assertStream(empty.filter(pred));
		assertStream(stream().filter(null), -1, null, 1);
		assertStream(stream().filter(no));
		assertStream(stream().filter(yes), -1, null, 1);
		assertStream(stream().filter(pred), 1);
	}

	@Test
	public void shouldFilterInstances() throws Exception {
		assertStream(empty.instances(null));
		assertStream(empty.instances(Object.class));
		assertStream(stream().instances(null));
		assertStream(stream().instances(Object.class), -1, 1);
		assertStream(stream().instances(Number.class), -1, 1);
		assertStream(stream().instances(Integer.class), -1, 1);
		assertStream(stream().instances(Long.class));
	}

	@Test
	public void shouldFilterNulls() throws Exception {
		assertStream(empty.nonNull());
		assertStream(stream().nonNull(), -1, 1);
	}

	@Test
	public void shouldMatchAny() throws Exception {
		assertEquals(empty.anyMatch(null), false);
		assertEquals(empty.anyMatch(no), false);
		assertEquals(empty.anyMatch(yes), false);
		assertEquals(empty.anyMatch(pred), false);
		assertEquals(stream().anyMatch(null), true);
		assertEquals(stream().anyMatch(no), false);
		assertEquals(stream().anyMatch(yes), true);
		assertEquals(stream().anyMatch(pred), true);
	}

	@Test
	public void shouldMatchAll() throws Exception {
		assertEquals(empty.allMatch(null), true);
		assertEquals(empty.allMatch(no), true);
		assertEquals(empty.allMatch(yes), true);
		assertEquals(empty.allMatch(pred), true);
		assertEquals(stream().allMatch(null), true);
		assertEquals(stream().allMatch(no), false);
		assertEquals(stream().allMatch(yes), true);
		assertEquals(stream().allMatch(pred), false);
	}

	@Test
	public void shouldMatchNone() throws Exception {
		assertEquals(empty.noneMatch(null), true);
		assertEquals(empty.noneMatch(no), true);
		assertEquals(empty.noneMatch(yes), true);
		assertEquals(empty.noneMatch(pred), true);
		assertEquals(stream().noneMatch(null), false);
		assertEquals(stream().noneMatch(no), true);
		assertEquals(stream().noneMatch(yes), false);
		assertEquals(stream().noneMatch(pred), false);
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(empty.map(null));
		assertStream(empty.map(fn));
		assertStream(stream().map(null));
		assertStream(stream().map(fn), "-1", "null", "1");
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		assertStream(empty.mapToInt(null));
		assertStream(empty.mapToInt(intFn));
		assertStream(stream().mapToInt(null));
		assertStream(stream().mapToInt(intFn), 1, 0, -1);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		assertStream(empty.mapToLong(null));
		assertStream(empty.mapToLong(longFn));
		assertStream(stream().mapToLong(null));
		assertStream(stream().mapToLong(longFn), 1L, 0L, -1L);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		assertStream(empty.mapToDouble(null));
		assertStream(empty.mapToDouble(doubleFn));
		assertStream(stream().mapToDouble(null));
		assertStream(stream().mapToDouble(doubleFn), 1.0, 0.0, -1.0);
	}

	@Test
	public void shouldExpandElements() throws Exception {
		assertStream(empty.expand(null));
		assertStream(empty.expand(expandFn));
		assertStream(stream().expand(null));
		assertStream(stream().expand(expandFn), 1, null, -1, -1, null, 1);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		assertStream(empty.flatMap(null));
		assertStream(empty.flatMap(flatFn));
		assertStream(stream().flatMap(null));
		assertStream(stream().flatMap(flatFn), 1, null, -1, -1, null, 1);
	}

	@Test
	public void shouldWrapExceptions() {
		assertStream(empty.runtime());
		assertStream(stream().runtime(), -1, null, 1);
		assertRte(() -> ioStream().runtime().toArray());
	}
	
	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(empty.limit(3));
		assertStream(stream().limit(0));
		assertStream(stream().limit(2), -1, null);
		assertStream(stream().limit(5), -1, null, 1);
	}

//	@Test
//	public void shouldProvideDistinctElements() throws Exception {
//		assertStream(Stream.empty().distinct());
//		assertStream(Stream.of(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
//	}
//
//	@Test
//	public void shouldProvideSortedElements() throws Exception {
//		assertStream(Stream.empty().sorted((_, _) -> 0));
//		assertStream(stream().sorted(Comparators.nullsFirst()), null, -1, 0, 1);
//	}
//
//	@Test
//	public void shouldProvideNextElement() throws Exception {
//		assertEquals(Stream.empty().next(), null);
//		assertEquals(Stream.empty().next("1"), "1");
//		var stream = stream();
//		assertEquals(stream.next(3), -1);
//		assertEquals(stream.next(3), 0);
//		assertEquals(stream.next(3), null);
//		assertEquals(stream.next(3), 1);
//		assertEquals(stream.next(3), 3);
//		assertEquals(stream.next(), null);
//	}
//
//	@Test
//	public void shouldDetermineIfEmpty() throws Exception {
//		assertEquals(Stream.empty().isEmpty(), true);
//		var stream = Stream.of(1);
//		assertEquals(stream.isEmpty(), false);
//		assertEquals(stream.isEmpty(), true);
//		assertEquals(stream.isEmpty(), true);
//	}
//
//	@Test
//	public void shouldDetermineCount() throws Exception {
//		assertEquals(Stream.empty().count(), 0L);
//		assertEquals(stream().count(), 4L);
//	}
//
//	@Test
//	public void shouldProvideIterator() {
//		assertIterable(Stream.empty().iterable());
//		assertIterable(stream().iterable(), -1, null, 1);
//	}
//
//	@Test
//	public void shouldAddToCollection() throws Exception {
//		assertUnordered(Stream.empty().add(new HashSet<>()));
//		assertEquals(stream().add((Collection<Object>) null), null);
//		assertUnordered(stream().add(new HashSet<>()), -1, 0, null, 1);
//	}
//
//	@Test
//	public void shouldPutInMap() throws Exception {
//		assertEquals(stream().put(null, i -> i), null);
//		assertMap(stream().put(Maps.of(), null));
//		assertMap(stream().put(Maps.of(), i -> i), -1, -1, null, null, 1, 1);
//		assertEquals(stream().put(null, i -> i, fn), null);
//		assertMap(stream().put(Maps.of(), null, fn));
//		assertMap(stream().put(Maps.of(), i -> i, null));
//		assertMap(stream().put(Maps.of(), i -> i, fn), -1, "-1", null, "null", 1, "1");
//		assertMap(stream().put(null, Maps.of(), i -> i, fn));
//		assertEquals(stream().put(Maps.Put.def, null, i -> i, fn), null);
//		assertMap(stream().put(Maps.Put.def, Maps.of(), null, fn));
//		assertMap(stream().put(Maps.Put.def, Maps.of(), i -> i, null));
//		assertMap(stream().put(Maps.Put.first, Maps.of(), i -> i, fn), -1, "-1", null, "null", 1,
//			"1");
//	}
//
//	@Test
//	public void shouldCollectToArray() throws Exception {
//		assertArray(Stream.empty().toArray());
//		assertArray(Stream.empty().toArray(Object[]::new));
//		assertArray(stream().toArray(), -1, 0, null, 1);
//		assertEquals(stream().toArray(null), null);
//		assertArray(stream().toArray(Integer[]::new), -1, 0, null, 1);
//	}
//
//	@Test
//	public void shouldCollectToSet() throws Exception {
//		assertUnordered(Stream.empty().toSet());
//		assertUnordered(stream().toSet(), -1, 0, null, 1);
//		assertUnordered(Stream.of(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
//	}
//
//	@Test
//	public void shouldCollectToList() throws Exception {
//		assertOrdered(Stream.empty().toList());
//		assertOrdered(stream().toList(), -1, 0, null, 1);
//		assertOrdered(Stream.of(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
//	}
//
//	@Test
//	public void shouldCollectToMap() throws Exception {
//		assertMap(Stream.empty().toMap(t -> t));
//		assertMap(stream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
//		assertMap(stream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
//	}
//
//	@Test
//	public void shouldCollectWithCollector() throws Exception {
//		assertEquals(Stream.empty().collect(Joiner.OR), "");
//		assertEquals(stream().collect((Collector<Object, ?, ?>) null), null);
//		assertEquals(stream().collect(Joiner.OR), "-1|0|null|1");
//	}
//
//	@Test
//	public void shouldCollectWithAccumulator() throws Exception {
//		assertEquals(stream().collect(null, (_, _) -> {}, _ -> ""), null);
//		assertEquals(stream().collect(StringBuilder::new, (_, _) -> {}, null), null);
//		assertEquals(stream().collect(StringBuilder::new, null, _ -> ""), "");
//		assertEquals(stream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
//		assertEquals(
//			stream().collect(StringBuilder::new, StringBuilder::append, b -> "[" + b + "]"),
//			"[-1null1]");
//	}
//
//	@Test
//	public void shouldReduceElements() throws Exception {
//		assertEquals(Stream.empty().reduce((_, _) -> 1), null);
//		assertEquals(Stream.empty().reduce((_, _) -> 1, 3), 3);
//		assertEquals(stream().reduce(null), null);
//		assertEquals(stream().reduce(null, 1), 1);
//		assertEquals(stream().reduce((i, _) -> i), -1);
//		assertEquals(stream().reduce((i, _) -> i, 3), -1);
//	}
}
