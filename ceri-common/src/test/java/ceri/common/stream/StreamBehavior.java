package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNoSuchElement;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.fail;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collector;
import org.junit.Test;
import ceri.common.comparator.Comparators;
import ceri.common.text.Joiner;

public class StreamBehavior {

	@Test
	public void testEmpty() throws Exception {
		assertStream(Stream.empty());
		assertSame(Stream.empty(), Stream.empty());
		assertSame(Stream.empty().filter(_ -> fail()), Stream.empty());
		assertSame(Stream.empty().map(_ -> fail()), Stream.empty());
		assertEquals(Stream.empty().next(), null);
		assertEquals(Stream.empty().isEmpty(), true);
		assertEquals(Stream.empty().count(), 0L);
	}

	@Test
	public void testOf() throws Exception {
		assertEquals(Stream.of((String[]) null).isEmpty(), true);
		assertEquals(Stream.of().isEmpty(), true);
		assertStream(Stream.of(1, 2, 3), 1, 2, 3);
	}

	@Test
	public void testFromIterable() throws Exception {
		assertEquals(Stream.from((List<?>) null).isEmpty(), true);
		assertEquals(Stream.from(List.of()).isEmpty(), true);
		assertStream(Stream.from(List.of(1, 2, 3)), 1, 2, 3);
	}

	@Test
	public void testFromIterator() throws Exception {
		assertEquals(Stream.from((Iterator<?>) null).isEmpty(), true);
		assertEquals(Stream.from(List.of().iterator()).isEmpty(), true);
		assertStream(Stream.from(List.of(1, 2, 3).iterator()), 1, 2, 3);
	}

	@Test
	public void testFromStream() throws Exception {
		assertEquals(Stream.from((java.util.stream.Stream<?>) null).isEmpty(), true);
		assertEquals(Stream.from(List.of().stream()).isEmpty(), true);
		assertStream(Stream.from(List.of(1, 2, 3).stream()), 1, 2, 3);
	}

	@Test
	public void testFromSpliterator() throws Exception {
		assertEquals(Stream.from((Spliterator<?>) null).isEmpty(), true);
		assertEquals(Stream.from(List.of().spliterator()).isEmpty(), true);
		assertStream(Stream.from(List.of(1, 2, 3).spliterator()), 1, 2, 3);
	}

	@Test
	public void shouldFilterElements() throws Exception {
		assertStream(Stream.empty().filter(Objects::nonNull));
		assertStream(testStream().filter(null), -1, 0, null, 1);
		assertStream(testStream().filter(Objects::nonNull), -1, 0, 1);
	}

	@Test
	public void shouldFilterInstances() throws Exception {
		assertStream(Stream.empty().instances(Object.class));
		assertStream(testStream().instances(Number.class), -1, 0, 1);
		assertStream(testStream().instances(Integer.class), -1, 0, 1);
		assertStream(testStream().instances(Long.class));
	}

	@Test
	public void shouldFilterNonNullElements() throws Exception {
		assertStream(Stream.empty().nonNull());
		assertStream(testStream().nonNull(), -1, 0, 1);
	}

	@Test
	public void shouldMatchAny() throws Exception {
		assertEquals(Stream.empty().anyMatch(null), false);
		assertEquals(Stream.empty().anyMatch(Objects::nonNull), false);
		assertEquals(testStream().anyMatch(null), true);
		assertEquals(testStream().anyMatch(i -> i != null), true);
		assertEquals(testStream().anyMatch(i -> i != null && i > 1), false);
	}

	@Test
	public void shouldMatchAll() throws Exception {
		assertEquals(Stream.empty().allMatch(null), true);
		assertEquals(Stream.empty().allMatch(Objects::nonNull), true);
		assertEquals(testStream().allMatch(null), true);
		assertEquals(testStream().allMatch(i -> i != null), false);
		assertEquals(testStream().allMatch(i -> i == null || i <= 1), true);
	}

	@Test
	public void shouldMatchNone() throws Exception {
		assertEquals(Stream.empty().noneMatch(null), true);
		assertEquals(Stream.empty().noneMatch(Objects::nonNull), true);
		assertEquals(testStream().noneMatch(null), false);
		assertEquals(testStream().noneMatch(i -> i != null), false);
		assertEquals(testStream().noneMatch(i -> i != null && i > 1), true);
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertStream(Stream.empty().map(null));
		assertStream(Stream.empty().map(String::valueOf));
		assertStream(testStream().map(null));
		assertStream(testStream().map(String::valueOf), "-1", "0", "null", "1");
	}

	@Test
	public void shouldMapElementsToInt() throws Exception {
		assertStream(Stream.empty().mapToInt(null));
		assertStream(Stream.empty().mapToInt(_ -> 0));
		assertStream(testStream().mapToInt(null));
		assertStream(testStream().mapToInt(i -> i == null ? 0 : i + 1), 0, 1, 0, 2);
	}

	@Test
	public void shouldMapElementsToLong() throws Exception {
		assertStream(Stream.empty().mapToLong(null));
		assertStream(Stream.empty().mapToLong(_ -> 0L));
		assertStream(testStream().mapToLong(null));
		assertStream(testStream().mapToLong(l -> l == null ? 0L : l + 1L), 0L, 1L, 0L, 2L);
	}

	@Test
	public void shouldMapElementsToDouble() throws Exception {
		assertStream(Stream.empty().mapToDouble(null));
		assertStream(Stream.empty().mapToDouble(_ -> .0));
		assertStream(testStream().mapToDouble(null));
		assertStream(testStream().mapToDouble(d -> d == null ? .0 : d + .1), -.9, .1, .0, 1.1);
	}

	@Test
	public void shouldExpand() throws Exception {
		assertStream(Stream.empty().expand(null));
		assertStream(Stream.empty().expand(_ -> Set.of(1)));
		assertStream(testStream().expand(null));
		assertStream(testStream().expand(i -> i == null ? null : List.of(i - 1, i + 1)), -2, 0,
			-1, 1, 0, 2);
	}

	@Test
	public void shouldFlatMap() throws Exception {
		assertStream(Stream.empty().flatMap(null));
		assertStream(Stream.empty().flatMap(_ -> Stream.of(1)));
		assertStream(testStream().flatMap(null));
		assertStream(testStream().flatMap(i -> i == null ? null : Stream.of(i - 1, i + 1)), -2, 0,
			-1, 1, 0, 2);
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertStream(Stream.empty().limit(3));
		assertStream(testStream().limit(0));
		assertStream(testStream().limit(3), -1, 0, null);
		assertStream(testStream().limit(5), -1, 0, null, 1);
	}

	@Test
	public void shouldProvideDistinctElements() throws Exception {
		assertStream(Stream.empty().distinct());
		assertStream(Stream.of(1, 0, null, 0, -1, null).distinct(), 1, 0, null, -1);
	}

	@Test
	public void shouldProvideSortedElements() throws Exception {
		assertStream(Stream.empty().sorted((_, _) -> 0));
		assertStream(testStream().sorted(Comparators.nullsFirst()), null, -1, 0, 1);
	}

	@Test
	public void shouldProvideNextElement() throws Exception {
		assertEquals(Stream.empty().next(), null);
		assertEquals(Stream.empty().next("1"), "1");
		var stream = testStream();
		assertEquals(stream.next(3), -1);
		assertEquals(stream.next(3), 0);
		assertEquals(stream.next(3), null);
		assertEquals(stream.next(3), 1);
		assertEquals(stream.next(3), 3);
		assertEquals(stream.next(), null);
	}

	@Test
	public void shouldDetermineIfEmpty() throws Exception {
		assertEquals(Stream.empty().isEmpty(), true);
		var stream = Stream.of(1);
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
		assertNoSuchElement(Stream.empty().iterable().iterator()::next);
		var iter = testStream().iterable().iterator();
		assertEquals(iter.hasNext(), true);
		assertEquals(iter.next(), -1);
		assertEquals(iter.next(), 0);
		assertEquals(iter.next(), null);
		assertEquals(iter.next(), 1);
		assertEquals(iter.hasNext(), false);
		assertNoSuchElement(iter::next);
	}

	@Test
	public void shouldProvideAnElementArray() throws Exception {
		assertArray(Stream.empty().toArray());
		assertArray(Stream.empty().toArray(Object[]::new));
		assertArray(testStream().toArray(), -1, 0, null, 1);
		assertEquals(testStream().toArray(null), null);
		assertArray(testStream().toArray(Integer[]::new), -1, 0, null, 1);
	}

	@Test
	public void shouldCollectToSet() throws Exception {
		assertUnordered(Stream.empty().toSet());
		assertUnordered(testStream().toSet(), -1, 0, null, 1);
		assertUnordered(Stream.of(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
	}

	@Test
	public void shouldCollectToList() throws Exception {
		assertOrdered(Stream.empty().toList());
		assertOrdered(testStream().toList(), -1, 0, null, 1);
		assertOrdered(Stream.of(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
	}

	@Test
	public void shouldCollectToMap() throws Exception {
		assertMap(Stream.empty().toMap(t -> t));
		assertMap(testStream().toMap(i -> i), -1, -1, 0, 0, null, null, 1, 1);
		assertMap(testStream().toMap(i -> i, _ -> 0), -1, 0, 0, 0, null, 0, 1, 0);
	}

	@Test
	public void shouldAddToMap() throws Exception {
		assertEquals(testStream().collectMap(i -> i, null), null);
		assertMap(testStream().collectMap(null, new HashMap<>()));
		assertMap(testStream().collectMap(i -> i, null, new HashMap<>()));
		assertMap(testStream().collectMap(i -> i, new HashMap<>()), -1, -1, 0, 0, null, null, 1, 1);
	}

	@Test
	public void shouldAddToCollection() throws Exception {
		assertUnordered(Stream.empty().collect(new HashSet<>()));
		assertEquals(testStream().collect((Collection<Object>) null), null);
		assertUnordered(testStream().collect(new HashSet<>()), -1, 0, null, 1);
	}

	@Test
	public void shouldCollectWithCollector() throws Exception {
		assertEquals(Stream.empty().collect(Joiner.OR), "");
		assertEquals(testStream().collect((Collector<Object, ?, ?>) null), null);
		assertEquals(testStream().collect(Joiner.OR), "-1|0|null|1");
	}

	@Test
	public void shouldCollectWithAccumulator() throws Exception {
		assertEquals(testStream().collect(null, (_, _) -> {}, _ -> ""), null);
		assertEquals(testStream().collect(StringBuilder::new, (_, _) -> {}, null), null);
		assertEquals(testStream().collect(StringBuilder::new, null, _ -> ""), "");
		assertEquals(testStream().collect(() -> null, (_, _) -> {}, _ -> ""), "");
		assertEquals(
			testStream().collect(StringBuilder::new, StringBuilder::append, b -> "[" + b + "]"),
			"[-10null1]");
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

	private static Stream<RuntimeException, Integer> testStream() {
		return Stream.of(-1, 0, null, 1);
	}

}
