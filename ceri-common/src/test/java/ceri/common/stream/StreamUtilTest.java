package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.E;
import ceri.common.test.Captor;

public class StreamUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StreamUtil.class);
	}

	@Test
	public void testBadCombiner() {
		assertThrown(() -> StreamUtil.badCombiner().accept(null, null));
		assertThrown(() -> StreamUtil.badCombiner().accept(1, 2));
	}

	@Test
	public void testForEachStream() {
		Captor.OfInt capturer = Captor.ofInt();
		StreamUtil.forEach(Stream.of(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
		StreamUtil.forEach(IntStream.of(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
		assertIoe(() -> StreamUtil.forEach(Stream.of(1, 2, 3), E.consumer));
		assertIoe(() -> StreamUtil.forEach(IntStream.of(1, 2, 3), E.consumer::accept));
		assertRte(() -> StreamUtil.forEach(Stream.of(2, 0, 3), E.consumer));
		assertRte(() -> StreamUtil.forEach(IntStream.of(2, 0, 3), E.consumer::accept));
	}

	@Test
	public void testCollect() {
		assertOrdered(StreamUtil.collect(Stream.of(1, 2, 3), ArrayList::new, List::add), 1, 2, 3);
	}

	@Test
	public void testIsEmpty() {
		assertTrue(StreamUtil.isEmpty(Stream.of()));
		assertFalse(StreamUtil.isEmpty(Stream.of("test")));
	}

	@Test
	public void testMaxAndMin() {
		assertEquals(StreamUtil.min(Stream.of("abc", "", "ABC")), "");
		assertEquals(StreamUtil.max(Stream.of("abc", "", "ABC")), "abc");
		assertThrown(() -> StreamUtil.min(Stream.of("abc", null, "ABC")));
		assertEquals(StreamUtil.max(Stream.of("abc", null, "ABC")), "abc");
	}

	@Test
	public void testFirst() {
		Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).filter(i -> i % 2 == 0);
		assertEquals(StreamUtil.first(stream), 2);
	}

	@Test
	public void testJoinToSet() {
		Stream<Collection<String>> stream = Stream.of(List.of("1", "2"), List.of("2", "3"));
		assertUnordered(StreamUtil.joinToSet(stream), "1", "2", "3");
	}

	@Test
	public void testJoinToList() {
		Stream<Collection<String>> stream = Stream.of(List.of("1", "2"), List.of("2", "3"));
		assertOrdered(StreamUtil.joinToList(stream), "1", "2", "2", "3");
	}

	@Test
	public void testToSet() {
		Set<Integer> set = StreamUtil.toSet(Stream.of(1, 3, 2));
		assertUnordered(set, 1, 3, 2);
		set = StreamUtil.toSet(Stream.of(1, 3, 2), TreeSet::new);
		assertOrdered(set, 1, 2, 3);
	}

	@Test
	public void testMergeFirst() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map = stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeFirst()));
		assertOrdered(map.values(), "1", "2", "3");
	}

	@Test
	public void testMergeSecond() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map = stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeSecond()));
		assertOrdered(map.values(), "01", "2", "3");
	}

	@Test
	public void testMergeError() {
		Stream<String> stream = Stream.of("1", "2", "3", "1");
		assertThrown(() -> stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeError())));
	}

	@Test
	public void testMerge() {
		assertEquals(StreamUtil.merge(true), StreamUtil.mergeFirst());
		assertEquals(StreamUtil.merge(false), StreamUtil.mergeSecond());
		assertEquals(StreamUtil.merge(null), StreamUtil.mergeError());
	}

	@Test
	public void testToList() {
		Stream<String> stream = Stream.of("A", "BB", null, "DDDD");
		assertList(StreamUtil.toList(stream), Arrays.asList("A", "BB", null, "DDDD"));
		stream = Stream.of();
		assertList(StreamUtil.toList(stream), Arrays.asList());
		stream = Stream.of((String) null);
		assertList(StreamUtil.toList(stream), Arrays.asList((String) null));
	}

	@Test
	public void testToMapKeysAndValues() {
		Stream<Integer> stream = Stream.of(1, 3, 2);
		Map<Integer, String> map = StreamUtil.toMap(stream, i -> i + 10, String::valueOf);
		assertUnordered(map.keySet(), 11, 12, 13);
		assertUnordered(map.values(), "1", "2", "3");
	}

	@Test
	public void testToMapKeys() {
		Stream<String> stream = Stream.of("1", "2", "3", "4");
		Map<Integer, String> map = StreamUtil.toMap(stream, Integer::parseInt);
		assertOrdered(map.keySet(), 1, 2, 3, 4);
		assertEquals(map.get(1), "1");
		assertEquals(map.get(2), "2");
		assertEquals(map.get(3), "3");
		assertEquals(map.get(4), "4");
	}

}
