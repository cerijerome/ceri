package ceri.common.stream;

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
import ceri.common.test.Assert;
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
		Assert.stream(empty);
		Assert.same(empty, Stream.empty());
	}

	@Test
	public void testOf() throws Exception {
		Assert.stream(Stream.ofAll(nullArray));
		Assert.stream(Stream.ofAll(emptyArray));
		Assert.stream(Stream.ofAll(array()), -1, null, 1);
		Assert.stream(Stream.of(nullArray, 1));
		Assert.stream(Stream.of(emptyArray, 1));
		Assert.stream(Stream.of(array(), 1), null, 1);
	}

	@Test
	public void testFrom() throws Exception {
		Assert.stream(Stream.from(nullList));
		Assert.stream(Stream.from(emptyList));
		Assert.stream(Stream.from(list), -1, null, 1);
		Assert.stream(Stream.from(nullIterator));
		Assert.stream(Stream.from(emptyList.iterator()));
		Assert.stream(Stream.from(list.iterator()), -1, null, 1);
		Assert.stream(Stream.from(nullStream));
		Assert.stream(Stream.from(emptyList.stream()));
		Assert.stream(Stream.from(list.stream()), -1, null, 1);
		Assert.stream(Stream.from(nullSpliterator));
		Assert.stream(Stream.from(emptyList.spliterator()));
		Assert.stream(Stream.from(list.spliterator()), -1, null, 1);
	}

	@Test
	public void shouldFilterElements() throws Exception {
		Assert.stream(empty.filter(null));
		Assert.stream(empty.filter(no));
		Assert.stream(empty.filter(yes));
		Assert.stream(empty.filter(pred));
		Assert.stream(testStream().filter(null), -1, null, 1, 0);
		Assert.stream(testStream().filter(no));
		Assert.stream(testStream().filter(yes), -1, null, 1, 0);
		Assert.stream(testStream().filter(pred), 1, 0);
	}

	@Test
	public void shouldFilterInstances() throws Exception {
		Assert.stream(empty.instances(null));
		Assert.stream(empty.instances(Object.class));
		Assert.stream(testStream().instances(null));
		Assert.stream(testStream().instances(Object.class), -1, 1, 0);
		Assert.stream(testStream().instances(Number.class), -1, 1, 0);
		Assert.stream(testStream().instances(Integer.class), -1, 1, 0);
		Assert.stream(testStream().instances(Long.class));
	}

	@Test
	public void shouldFilterNulls() throws Exception {
		Assert.stream(empty.nonNull());
		Assert.stream(testStream().nonNull(), -1, 1, 0);
	}

	@Test
	public void shouldMatchAny() throws Exception {
		Assert.equal(empty.anyMatch(null), false);
		Assert.equal(empty.anyMatch(no), false);
		Assert.equal(empty.anyMatch(yes), false);
		Assert.equal(empty.anyMatch(pred), false);
		Assert.equal(testStream().anyMatch(null), true);
		Assert.equal(testStream().anyMatch(no), false);
		Assert.equal(testStream().anyMatch(yes), true);
		Assert.equal(testStream().anyMatch(pred), true);
	}

	@Test
	public void shouldMatchAll() throws Exception {
		Assert.equal(empty.allMatch(null), true);
		Assert.equal(empty.allMatch(no), true);
		Assert.equal(empty.allMatch(yes), true);
		Assert.equal(empty.allMatch(pred), true);
		Assert.equal(testStream().allMatch(null), true);
		Assert.equal(testStream().allMatch(no), false);
		Assert.equal(testStream().allMatch(yes), true);
		Assert.equal(testStream().allMatch(pred), false);
	}

	@Test
	public void shouldMatchNone() throws Exception {
		Assert.equal(empty.noneMatch(null), true);
		Assert.equal(empty.noneMatch(no), true);
		Assert.equal(empty.noneMatch(yes), true);
		Assert.equal(empty.noneMatch(pred), true);
		Assert.equal(testStream().noneMatch(null), false);
		Assert.equal(testStream().noneMatch(no), true);
		Assert.equal(testStream().noneMatch(yes), false);
		Assert.equal(testStream().noneMatch(pred), false);
	}

	@Test
	public void shouldMapToString() throws Exception {
		Assert.stream(empty.string());
		Assert.stream(testStream().string(), "-1", "", "1", "0");
	}

	@Test
	public void shouldMapElements() throws Exception {
		Assert.stream(empty.map(null));
		Assert.stream(empty.map(fn));
		Assert.stream(testStream().map(null));
		Assert.stream(testStream().map(fn), "-1", "null", "1", "0");
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		Assert.stream(empty.mapToInt(null));
		Assert.stream(empty.mapToInt(intFn));
		Assert.stream(testStream().mapToInt(null));
		Assert.stream(testStream().mapToInt(intFn), 1, 0, -1, 0);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		Assert.stream(empty.mapToLong(null));
		Assert.stream(empty.mapToLong(longFn));
		Assert.stream(testStream().mapToLong(null));
		Assert.stream(testStream().mapToLong(longFn), 1L, 0L, -1L, 0L);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		Assert.stream(empty.mapToDouble(null));
		Assert.stream(empty.mapToDouble(doubleFn));
		Assert.stream(testStream().mapToDouble(null));
		Assert.stream(testStream().mapToDouble(doubleFn), 1.0, 0.0, -1.0, 0.0);
	}

	@Test
	public void shouldExpandElements() throws Exception {
		Assert.stream(empty.expand(null));
		Assert.stream(empty.expand(expandFn));
		Assert.stream(testStream().expand(null));
		Assert.stream(testStream().expand(expandFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldFlatMapElements() throws Exception {
		Assert.stream(empty.flatMap(null));
		Assert.stream(empty.flatMap(flatFn));
		Assert.stream(testStream().flatMap(null));
		Assert.stream(testStream().flatMap(flatFn), 1, null, -1, -1, null, 1, 0, null, 0);
	}

	@Test
	public void shouldWrapExceptions() {
		Assert.stream(empty.runtime());
		Assert.stream(testStream().runtime(), -1, null, 1, 0);
		Assert.runtime(() -> ioStream().runtime().toArray());
	}

	@Test
	public void shouldLimitElements() throws Exception {
		Assert.stream(empty.limit(3));
		Assert.stream(testStream().limit(0));
		Assert.stream(testStream().limit(2), -1, null);
		Assert.stream(testStream().limit(5), -1, null, 1, 0);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		Assert.stream(Stream.empty().distinct());
		Assert.stream(Stream.ofAll(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		Assert.stream(Stream.empty().sorted((_, _) -> 0));
		Assert.stream(testStream().sorted(Compares.of()), null, -1, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		Assert.equal(Stream.empty().next(), null);
		Assert.equal(Stream.empty().next(3), 3);
		var stream = testStream();
		Assert.equal(stream.next(3), -1);
		Assert.equal(stream.next(), null);
		Assert.equal(stream.next(), 1);
		Assert.equal(stream.next(3), 0);
		Assert.equal(stream.next(), null);
		Assert.equal(stream.next(3), 3);
	}

	@Test
	public void shouldSkipElements() {
		Assert.stream(testStream().skip(2), 1, 0);
		Assert.stream(testStream().skip(5));
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		Assert.equal(Stream.empty().isEmpty(), true);
		var stream = Stream.ofAll(1);
		Assert.equal(stream.isEmpty(), false);
		Assert.equal(stream.isEmpty(), true);
		Assert.equal(stream.isEmpty(), true);
	}

	@Test
	public void shouldDetermineCount() throws Exception {
		Assert.equal(Stream.empty().count(), 0L);
		Assert.equal(testStream().count(), 4L);
	}

	@Test
	public void shouldProvideIterator() {
		Assert.ordered(Stream.empty().iterable());
		Assert.ordered(testStream().iterable(), -1, null, 1, 0);
	}

	@Test
	public void shouldAddToCollection() throws Exception {
		Assert.unordered(Stream.empty().add(Sets.of()));
		Assert.equal(testStream().add(nullList), null);
		Assert.unordered(testStream().add(Sets.of()), -1, 0, null, 1);
	}

	@Test
	public void shouldPutInMap() throws Exception {
		Assert.equal(testStream().put(null, i -> i), null);
		Assert.map(testStream().put(Maps.of(), null));
		Assert.map(testStream().put(Maps.of(), i -> i), -1, -1, null, null, 1, 1, 0, 0);
		Assert.equal(testStream().put(null, i -> i, fn), null);
		Assert.map(testStream().put(Maps.of(), null, fn));
		Assert.map(testStream().put(Maps.of(), i -> i, null));
		Assert.map(testStream().put(Maps.of(), i -> i, fn), -1, "-1", null, "null", 1, "1", 0, "0");
		Assert.map(testStream().put(null, Maps.of(), i -> i, fn));
		Assert.equal(testStream().put(Maps.Put.def, null, i -> i, fn), null);
		Assert.map(testStream().put(Maps.Put.def, Maps.of(), null, fn));
		Assert.map(testStream().put(Maps.Put.def, Maps.of(), i -> i, null));
		Assert.map(testStream().put(Maps.Put.first, Maps.of(), i -> i, fn), -1, "-1", null, "null",
			1, "1", 0, "0");
	}

	@Test
	public void shouldCollectToArray() throws Exception {
		Assert.array(Stream.empty().toArray());
		Assert.array(Stream.empty().toArray(Object[]::new));
		Assert.array(testStream().toArray(), -1, null, 1, 0);
		Assert.equal(testStream().toArray(null), null);
		Assert.array(testStream().toArray(Integer[]::new), -1, null, 1, 0);
	}

	@Test
	public void shouldCollectToSet() throws Exception {
		Assert.unordered(Stream.empty().toSet());
		Assert.unordered(testStream().toSet(), -1, 0, null, 1);
		Assert.unordered(Stream.ofAll(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
	}

	@Test
	public void shouldCollectToList() throws Exception {
		Assert.ordered(Stream.empty().toList());
		Assert.ordered(testStream().toList(), -1, null, 1, 0);
		Assert.ordered(Stream.ofAll(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
	}

	@Test
	public void shouldCollectToMap() throws Exception {
		Assert.map(Stream.empty().toMap(t -> t));
		Assert.map(testStream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
		Assert.map(testStream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
	}

	@Test
	public void shouldCollectWithCollector() throws Exception {
		Assert.equal(Stream.empty().collect(Joiner.OR), "");
		Assert.equal(testStream().collect((Collector<Integer, ?, ?>) null), null);
		Assert.equal(testStream().collect(Joiner.OR), "-1|null|1|0");
	}

	@Test
	public void shouldCollectWithAccumulator() throws Exception {
		Assert.equal(testStream().collect(null, (_, _) -> {}, _ -> ""), null);
		Assert.equal(testStream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		Assert.equal(testStream().collect(StringBuilder::new, null, _ -> ""), "");
		Assert.equal(testStream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		Assert.equal(
			testStream().collect(StringBuilder::new, StringBuilder::append, b -> "[" + b + "]"),
			"[-1null10]");
	}

	@Test
	public void shouldReduceElements() throws Exception {
		Assert.equal(Stream.empty().reduce((_, _) -> 1), null);
		Assert.equal(Stream.empty().reduce((_, _) -> 1, 3), 3);
		Assert.equal(testStream().reduce(null), null);
		Assert.equal(testStream().reduce(null, 1), 1);
		Assert.equal(testStream().reduce((i, _) -> i), -1);
		Assert.equal(testStream().reduce((i, _) -> i, 3), -1);
	}
}
