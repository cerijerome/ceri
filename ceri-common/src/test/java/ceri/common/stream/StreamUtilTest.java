package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.collection.MapPopulator;
import ceri.common.collection.Mutables;
import ceri.common.function.FunctionTestUtil.E;
import ceri.common.test.Captor;

public class StreamUtilTest {
	private enum Abc {
		A,
		B,
		C;
	}

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
	public void testIterable() {
		String[] array = { "a", "b", "c" };
		int i = 0;
		for (String s : StreamUtil.iterable(Stream.of(array)))
			assertEquals(s, array[i++]);
	}

	@Test
	public void testIntIterable() {
		int[] array = { 1, -1, 0 };
		int i = 0;
		for (int n : StreamUtil.iterable(IntStream.of(array)))
			assertEquals(n, array[i++]);
	}

	@Test
	public void testLongIterable() {
		long[] array = { 1, -1, 0 };
		int i = 0;
		for (long n : StreamUtil.iterable(LongStream.of(array)))
			assertEquals(n, array[i++]);
	}

	@Test
	public void testCollect() {
		assertOrdered(StreamUtil.collect(Stream.of(1, 2, 3), ArrayList::new, List::add), 1, 2, 3);
	}

	@Test
	public void testToString() {
		assertNull(StreamUtil.toString(null, "-"));
		assertNull(StreamUtil.toString(null, "(", ":", ")"));
		assertEquals(StreamUtil.toString(Stream.of(1, null, 2), "-"), "1-null-2");
		assertEquals(StreamUtil.toString(Stream.of(1, null, 2), "(", "::", ")"), "(1::null::2)");
	}

	@Test
	public void testIsEmpty() {
		assertTrue(StreamUtil.isEmpty(Stream.of()));
		assertFalse(StreamUtil.isEmpty(Stream.of("test")));
	}

	@Test
	public void testFindFirstNonNull() {
		Stream<String> stream = Stream.of(null, null, "abc", "de", "f");
		assertEquals(StreamUtil.findFirstNonNull(stream, s -> s.length() < 3), "de");
	}

	@Test
	public void testFirstNonNull() {
		Stream<String> stream = Stream.of(null, null, "abc", "def");
		assertEquals(StreamUtil.firstNonNull(stream), "abc");
	}

	@Test
	public void testMaxAndMin() {
		assertEquals(StreamUtil.min(Stream.of("abc", "", "ABC")), "");
		assertEquals(StreamUtil.max(Stream.of("abc", "", "ABC")), "abc");
		assertThrown(() -> StreamUtil.min(Stream.of("abc", null, "ABC")));
		assertEquals(StreamUtil.max(Stream.of("abc", null, "ABC")), "abc");
	}

	@Test
	public void testFirstOf() {
		Stream<Number> stream = Stream.of((byte) 1, 2.1, 3L, (short) 4, 5);
		assertEquals(StreamUtil.firstOf(stream, Short.class), (short) 4);
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
	public void testToEntryMap() {
		assertMap(StreamUtil.toEntryMap(Map.of(1, "1", 2, "2").entrySet().stream()), 1, "1", 2,
			"2");
	}

	@Test
	public void testEntryCollector() {
		assertMap(Map.of(1, "1", 2, "2").entrySet().stream().collect(StreamUtil.entryCollector()),
			1, "1", 2, "2");
	}

	@Test
	public void testStreamMap() {
		Map<String, Integer> map0 = MapPopulator.of("abc", 1, "DE", 1, "f", 0).map;
		assertArray(StreamUtil.stream(map0, String::charAt).toArray(), 'b', 'E', 'f');
		Map<Integer, String> map1 = MapPopulator.of(1, "1", 3, "3", 2, "2").map;
		Object[] array =
			StreamUtil.stream(map1, (i, s) -> parseDouble(s + "." + (i * i))).toArray();
		assertArray(array, 1.1, 3.9, 2.4);
	}

	@Test
	public void testIntStreamFromIteratorFunctions() {
		var list = Mutables.asList(5, 2, 4, 1);
		var stream = StreamUtil.intStream(() -> !list.isEmpty(), () -> list.remove(0));
		assertStream(stream, 5, 2, 4, 1);
	}

	@Test
	public void testLongStreamFromIteratorFunctions() {
		var list = Mutables.asList(5L, 2L, 4L, 1L);
		var stream = StreamUtil.longStream(() -> !list.isEmpty(), () -> list.remove(0));
		assertStream(stream, 5, 2, 4, 1);
	}

	@Test
	public void testStreamIterator() {
		assertStream(StreamUtil.stream(List.of(1, 2, 3).iterator()), 1, 2, 3);
	}

	@Test
	public void testStreamArray() {
		assertStream(StreamUtil.stream(new Integer[] { 1, 2, 3 }, 1), 2, 3);
	}

	@Test
	public void testStreamEnums() {
		assertOrdered(StreamUtil.toList(StreamUtil.stream(Abc.class)), Abc.A, Abc.B, Abc.C);
	}

	@Test
	public void testStreamEnumeration() {
		Properties props = new Properties();
		props.put("A", 1);
		props.put("B", 2);
		props.put("C", 3);
		assertUnordered(StreamUtil.stream(props.elements()).toList(), 1, 2, 3);
	}

	@Test
	public void testToSet() {
		Set<Integer> set = StreamUtil.toSet(Stream.of(1, 3, 2));
		assertUnordered(set, 1, 3, 2);
		set = StreamUtil.toSet(Stream.of(1, 3, 2), TreeSet::new);
		assertOrdered(set, 1, 2, 3);
	}

	@Test
	public void testToIdentitySet() {
		String a0 = new String("a");
		String a1 = new String("a");
		String b0 = new String("b");
		Set<String> set = StreamUtil.toIdentitySet(Stream.of(a0, b0, a1));
		assertUnordered(set, a0, b0, a1);
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

	@Test
	public void testToMapOfCollections() {
		Stream<String> stream = Stream.of("1", "22", "333", "44", "5");
		Map<Integer, Set<String>> map =
			StreamUtil.toMapOfCollections(stream, t -> t.length(), LinkedHashSet::new);
		assertMap(map, 1, Set.of("1", "5"), 2, Set.of("22", "44"), 3, Set.of("333"));
	}

	@Test
	public void testToMapOfSets() {
		Stream<String> stream = Stream.of("1", "22", "333", "44", "5");
		var map = StreamUtil.toMapOfSets(stream, t -> t.length());
		assertMap(map, 1, Set.of("1", "5"), 2, Set.of("22", "44"), 3, Set.of("333"));
	}

	@Test
	public void testToMapOfLists() {
		Stream<String> stream = Stream.of("1", "22", "333", "44", "5");
		var map = StreamUtil.toMapOfLists(stream, t -> t.length());
		assertMap(map, 1, List.of("1", "5"), 2, List.of("22", "44"), 3, List.of("333"));
	}

}
