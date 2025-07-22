package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNoSuchElement;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.fail;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
	public void shouldFilterElements() throws Exception {
		assertStream(Stream.empty().filter(Objects::nonNull));
		assertStream(testStream().filter(null), -1, 0, null, 1);
		assertStream(testStream().filter(Objects::nonNull), -1, 0, 1);
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
		assertStream(testStream().sorted(Comparators.nullsFirstComparable()), null, -1, 0, 1);
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
	public void shouldDetermineMinElement() throws Exception {
		assertEquals(Stream.empty().min(null), null);
		assertEquals(Stream.empty().min(null, 3), 3);
		assertEquals(Stream.empty().min((_, _) -> 0), null);
		assertEquals(Stream.empty().min((_, _) -> 0, 3), 3);
		assertEquals(testStream().min(null), null);
		assertEquals(testStream().min(null, 3), 3);
		assertEquals(testStream().min(Comparators.nullsFirstComparable()), null);
		assertEquals(testStream().min(Comparators.nullsLastComparable()), -1);
	}

	@Test
	public void shouldDetermineMaxElement() throws Exception {
		assertEquals(Stream.empty().max(null), null);
		assertEquals(Stream.empty().max(null, 3), 3);
		assertEquals(Stream.empty().max((_, _) -> 0), null);
		assertEquals(Stream.empty().max((_, _) -> 0, 3), 3);
		assertEquals(testStream().max(null), null);
		assertEquals(testStream().max(null, 3), 3);
		assertEquals(testStream().max(Comparators.nullsFirstComparable()), 1);
		assertEquals(testStream().max(Comparators.nullsLastComparable()), null);
	}

	@Test
	public void shouldProvideIterator() {
		assertNoSuchElement(Stream.empty().iterator()::next);
		var iter = testStream().iterator();
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
		assertArray(Stream.empty().toArray(Object[]::new));
		assertEquals(testStream().toArray(null), null);
		assertArray(testStream().toArray(Integer[]::new), -1, 0, null, 1);
	}

	@Test
	public void shouldCollectToSet() throws Exception {
		assertCollection(Stream.empty().toSet());
		assertCollection(testStream().toSet(), -1, 0, null, 1);
		assertCollection(Stream.of(1, 0, null, 0, -1, null).toSet(), 1, 0, null, -1);
	}

	@Test
	public void shouldCollectToList() throws Exception {
		assertIterable(Stream.empty().toList());
		assertIterable(testStream().toList(), -1, 0, null, 1);
		assertIterable(Stream.of(1, 0, null, 0, -1, null).toList(), 1, 0, null, 0, -1, null);
	}

	@Test
	public void shouldCollectToSortedList() throws Exception {
		assertIterable(Stream.empty().toList((_, _) -> 0));
		assertIterable(testStream().toList(Comparators.nullsFirstComparable()), null, -1, 0, 1);
		assertIterable(
			Stream.of(1, 0, null, 0, -1, null).toList(Comparators.nullsFirstComparable()), null,
			null, -1, 0, 0, 1);
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
		assertCollection(Stream.empty().collect(new HashSet<>()));
		assertEquals(testStream().collect((Collection<Object>) null), null);
		assertCollection(testStream().collect(new HashSet<>()), -1, 0, null, 1);
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
		assertEquals(Stream.empty().reduce(3, (_, _) -> 1), 3);
		assertEquals(testStream().reduce(null), null);
		assertEquals(testStream().reduce(1, null), 1);
		assertEquals(testStream().reduce((i, _) -> i), -1);
		assertEquals(testStream().reduce(3, (i, _) -> i), 3);
	}

	private static Stream<RuntimeException, Integer> testStream() {
		return Stream.of(-1, 0, null, 1);
	}

}
