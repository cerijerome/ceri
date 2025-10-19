package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collector;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.function.Compares;
import ceri.common.function.Functions;
import ceri.common.text.Joiner;

public class StreamBehavior {
	private static final Stream<RuntimeException, Integer> empty = Stream.empty();
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = Immutable.list();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Iterator<Integer> nullIterator = null;
	private static final Spliterator<Integer> nullSpliterator = null;
	private static final java.util.stream.Stream<Integer> nullStream = null;
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Predicate<Integer> no = _ -> false;
	private static final Functions.Predicate<Integer> yes = _ -> true;
	private static final Functions.Predicate<Integer> pred = i -> i != null && i >= 0;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.ToIntFunction<Integer> intFn = i -> i == null ? 0 : -i;
	private static final Functions.ToLongFunction<Integer> longFn = i -> i == null ? 0 : -i;
	private static final Functions.ToDoubleFunction<Integer> doubleFn = i -> i == null ? 0 : -i;
	private static final Functions.Function<Integer, Iterable<Integer>> expandFn =
		i -> i == null ? null : Lists.ofAll(-i, null, i);
	private static final Functions.Function<Integer, Stream<RuntimeException, Integer>> flatFn =
		i -> i == null ? null : Streams.of(-i, null, i);

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static Stream<RuntimeException, Integer> testStream() {
		return Stream.ofAll(-1, null, 1, 0);
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
		assertStream(testStream().filter(null), -1, null, 1, 0);
		assertStream(testStream().filter(no));
		assertStream(testStream().filter(yes), -1, null, 1, 0);
		assertStream(testStream().filter(pred), 1, 0);
	}

	@Test
	public void shouldFilterInstances() throws Exception {
		assertStream(empty.instances(null));
		assertStream(empty.instances(Object.class));
		assertStream(testStream().instances(null));
		assertStream(testStream().instances(Object.class), -1, 1, 0);
		assertStream(testStream().instances(Number.class), -1, 1, 0);
		assertStream(testStream().instances(Integer.class), -1, 1, 0);
		assertStream(testStream().instances(Long.class));
	}

	@Test
	public void shouldFilterNulls() throws Exception {
		assertStream(empty.nonNull());
		assertStream(testStream().nonNull(), -1, 1, 0);
	}

	@Test
	public void shouldMatchAny() throws Exception {
		assertEquals(empty.anyMatch(null), false);
		assertEquals(empty.anyMatch(no), false);
		assertEquals(empty.anyMatch(yes), false);
		assertEquals(empty.anyMatch(pred), false);
		assertEquals(testStream().anyMatch(null), true);
		assertEquals(testStream().anyMatch(no), false);
		assertEquals(testStream().anyMatch(yes), true);
		assertEquals(testStream().anyMatch(pred), true);
	}

	@Test
	public void shouldMatchAll() throws Exception {
		assertEquals(empty.allMatch(null), true);
		assertEquals(empty.allMatch(no), true);
		assertEquals(empty.allMatch(yes), true);
		assertEquals(empty.allMatch(pred), true);
		assertEquals(testStream().allMatch(null), true);
		assertEquals(testStream().allMatch(no), false);
		assertEquals(testStream().allMatch(yes), true);
		assertEquals(testStream().allMatch(pred), false);
	}

	@Test
	public void shouldMatchNone() throws Exception {
		assertEquals(empty.noneMatch(null), true);
		assertEquals(empty.noneMatch(no), true);
		assertEquals(empty.noneMatch(yes), true);
		assertEquals(empty.noneMatch(pred), true);
		assertEquals(testStream().noneMatch(null), false);
		assertEquals(testStream().noneMatch(no), true);
		assertEquals(testStream().noneMatch(yes), false);
		assertEquals(testStream().noneMatch(pred), false);
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(empty.map(null));
		assertStream(empty.map(fn));
		assertStream(testStream().map(null));
		assertStream(testStream().map(fn), "-1", "null", "1", "0");
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		assertStream(empty.mapToInt(null));
		assertStream(empty.mapToInt(intFn));
		assertStream(testStream().mapToInt(null));
		assertStream(testStream().mapToInt(intFn), 1, 0, -1, 0);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		assertStream(empty.mapToLong(null));
		assertStream(empty.mapToLong(longFn));
		assertStream(testStream().mapToLong(null));
		assertStream(testStream().mapToLong(longFn), 1L, 0L, -1L, 0L);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		assertStream(empty.mapToDouble(null));
		assertStream(empty.mapToDouble(doubleFn));
		assertStream(testStream().mapToDouble(null));
		assertStream(testStream().mapToDouble(doubleFn), 1.0, 0.0, -1.0, 0.0);
	}

	@Test
	public void shouldExpandElements() throws Exception {
		assertStream(empty.expand(null));
		assertStream(empty.expand(expandFn));
		assertStream(testStream().expand(null));
		assertStream(testStream().expand(expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		assertStream(empty.flatMap(null));
		assertStream(empty.flatMap(flatFn));
		assertStream(testStream().flatMap(null));
		assertStream(testStream().flatMap(flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldWrapExceptions() {
		assertStream(empty.runtime());
		assertStream(testStream().runtime(), -1, null, 1, 0);
		assertRte(() -> ioStream().runtime().toArray());
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(empty.limit(3));
		assertStream(testStream().limit(0));
		assertStream(testStream().limit(2), -1, null);
		assertStream(testStream().limit(5), -1, null, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		assertStream(Stream.empty().distinct());
		assertStream(Stream.ofAll(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		assertStream(Stream.empty().sorted((_, _) -> 0));
		assertStream(testStream().sorted(Compares.of()), null, -1, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		assertEquals(Stream.empty().next(), null);
		assertEquals(Stream.empty().next(3), 3);
		var stream = testStream();
		assertEquals(stream.next(3), -1);
		assertEquals(stream.next(), null);
		assertEquals(stream.next(), 1);
		assertEquals(stream.next(3), 0);
		assertEquals(stream.next(), null);
		assertEquals(stream.next(3), 3);
	}

	@Test
	public void shouldSkipElements() {
		assertStream(testStream().skip(2), 1, 0);
		assertStream(testStream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		assertEquals(Stream.empty().isEmpty(), true);
		var stream = Stream.ofAll(1);
		assertEquals(stream.isEmpty(), false);
		assertEquals(stream.isEmpty(), true);
		assertEquals(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		assertEquals(Stream.empty().count(), 0L);
		assertEquals(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() {
		assertOrdered(Stream.empty().iterable());
		assertOrdered(testStream().iterable(), -1, null, 1, 0);
	}

	@Test
	public void shouldAddToCollection() throws Exception {
		assertUnordered(Stream.empty().add(Sets.of()));
		assertEquals(testStream().add(nullList), null);
		assertUnordered(testStream().add(Sets.of()), -1, 0, null, 1);
	}

	@Test
	public void shouldPutInMap() throws Exception {
		assertEquals(testStream().put(null, i -> i), null);
		assertMap(testStream().put(Maps.of(), null));
		assertMap(testStream().put(Maps.of(), i -> i), -1, -1, null, null, 1, 1, 0, 0);
		assertEquals(testStream().put(null, i -> i, fn), null);
		assertMap(testStream().put(Maps.of(), null, fn));
		assertMap(testStream().put(Maps.of(), i -> i, null));
		assertMap(testStream().put(Maps.of(), i -> i, fn), -1, "-1", null, "null", 1, "1", 0, "0");
		assertMap(testStream().put(null, Maps.of(), i -> i, fn));
		assertEquals(testStream().put(Maps.Put.def, null, i -> i, fn), null);
		assertMap(testStream().put(Maps.Put.def, Maps.of(), null, fn));
		assertMap(testStream().put(Maps.Put.def, Maps.of(), i -> i, null));
		assertMap(testStream().put(Maps.Put.first, Maps.of(), i -> i, fn), -1, "-1", null, "null",
			1, "1", 0, "0");
	}

	@Test
	public void shouldCollectToArray() throws Exception {
		assertArray(Stream.empty().toArray());
		assertArray(Stream.empty().toArray(Object[]::new));
		assertArray(testStream().toArray(), -1, null, 1, 0);
		assertEquals(testStream().toArray(null), null);
		assertArray(testStream().toArray(Integer[]::new), -1, null, 1, 0);
	}

	@Test
	public void shouldCollectToSet() throws Exception {
		assertUnordered(Stream.empty().toSet());
		assertUnordered(testStream().toSet(), -1, 0, null, 1);
		assertUnordered(Stream.ofAll(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
	}

	@Test
	public void shouldCollectToList() throws Exception {
		assertOrdered(Stream.empty().toList());
		assertOrdered(testStream().toList(), -1, null, 1, 0);
		assertOrdered(Stream.ofAll(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
	}

	@Test
	public void shouldCollectToMap() throws Exception {
		assertMap(Stream.empty().toMap(t -> t));
		assertMap(testStream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
		assertMap(testStream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
	}

	@Test
	public void shouldCollectWithCollector() throws Exception {
		assertEquals(Stream.empty().collect(Joiner.OR), "");
		assertEquals(testStream().collect((Collector<Integer, ?, ?>) null), null);
		assertEquals(testStream().collect(Joiner.OR), "-1|null|1|0");
	}

	@Test
	public void shouldCollectWithAccumulator() throws Exception {
		assertEquals(testStream().collect(null, (_, _) -> {}, _ -> ""), null);
		assertEquals(testStream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		assertEquals(testStream().collect(StringBuilder::new, null, _ -> ""), "");
		assertEquals(testStream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		assertEquals(
			testStream().collect(StringBuilder::new, StringBuilder::append, b -> "[" + b + "]"),
			"[-1null10]");
	}

	@Test
	public void shouldReduceElements() throws Exception {
		assertEquals(Stream.empty().reduce((_, _) -> 1), null);
		assertEquals(Stream.empty().reduce((_, _) -> 1, 3), 3);
		assertEquals(testStream().reduce(null), null);
		assertEquals(testStream().reduce(null, 1), 1);
		assertEquals(testStream().reduce((i, _) -> i), -1);
		assertEquals(testStream().reduce((i, _) -> i, 3), -1);
	}
}
