package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static java.lang.Double.parseDouble;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.FunctionTestUtil;

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
	public void testCloseableApply() throws IOException {
		ExceptionFunction<IOException, Integer, Integer> iFn = FunctionTestUtil.function();
		ExceptionFunction<IOException, Stream<Integer>, Integer> fn =
			s -> iFn.apply(s.findFirst().get());
		StreamUtil.closeableApply(Stream.of(2, 3, 4), fn);
		assertThrown(() -> StreamUtil.closeableApply(Stream.of(1, 2, 3), fn));
	}

	@Test
	public void testCloseableApplyAsInt() throws IOException {
		ExceptionIntUnaryOperator<IOException> intOp = FunctionTestUtil.intUnaryOperator();
		ExceptionToIntFunction<IOException, Stream<Integer>> toIntFn =
			s -> intOp.applyAsInt(s.findFirst().get());
		StreamUtil.closeableApplyAsInt(Stream.of(2, 3, 4), toIntFn);
		assertThrown(() -> StreamUtil.closeableApplyAsInt(Stream.of(1, 2, 3), toIntFn));
	}

	@Test
	public void testCloseableAccept() throws IOException {
		ExceptionConsumer<IOException, Integer> consumer = FunctionTestUtil.consumer();
		ExceptionConsumer<IOException, Stream<Integer>> streamConsumer =
			s -> consumer.accept(s.findFirst().get());
		StreamUtil.closeableAccept(Stream.of(2, 3, 4), streamConsumer);
		assertThrown(() -> StreamUtil.closeableAccept(Stream.of(1, 2, 3), streamConsumer));
	}

	@Test
	public void testCloseableForEach() throws IOException {
		StreamUtil.closeableForEach(Stream.of(2, 3, 4), FunctionTestUtil.consumer());
		assertThrown(
			() -> StreamUtil.closeableForEach(Stream.of(2, 1), FunctionTestUtil.consumer()));
	}

	@Test
	public void testUnitRange() {
		assertArray(StreamUtil.unitRange(5).toArray(), 0.0, 0.25, 0.5, 0.75, 1.0);
	}

	@Test
	public void testToInt() {
		assertArray(StreamUtil.toInt(List.of(1.2, 2.4, 3.6, 4.8).stream()).toArray(), 1, 2, 3, 4);
	}

	@Test
	public void testBitwiseOperators() {
		assertEquals(StreamUtil.bitwiseOr(IntStream.of(1, 2, 5)), 7);
		assertEquals(StreamUtil.bitwiseAnd(IntStream.of(15, 7, 14)), 6);
		assertEquals(StreamUtil.bitwiseXor(IntStream.of(1, 2, 5)), 6);
	}

	@Test
	public void testCastAny() {
		Stream<Number> stream = StreamUtil.castAny(Stream.of("1", 1, 0.1, null, 2), Number.class);
		assertStream(stream, 1, 0.1, 2);
	}

	@Test
	public void testRange() {
		assertStream(StreamUtil.range(3, i -> (char) ('A' + i)), Indexed.of('A', 0),
			Indexed.of('B', 1), Indexed.of('C', 2));
		assertStream(StreamUtil.range(1, 3, i -> (char) ('A' + i)), Indexed.of('B', 1),
			Indexed.of('C', 2));
	}

	@Test
	public void testIndexed() {
		assertStream(StreamUtil.indexed(List.of("A", "B", "C")), Indexed.of("A", 0),
			Indexed.of("B", 1), Indexed.of("C", 2));
		assertStream(StreamUtil.indexed("A", "B", "C"), Indexed.of("A", 0), Indexed.of("B", 1),
			Indexed.of("C", 2));
	}

	@Test
	public void testIndexedMap() {
		assertStream(StreamUtil.map(StreamUtil.indexed("A", "B", "C"), (s, i) -> s + i), "A0", "B1",
			"C2");
		assertStream(StreamUtil.indexedMap(List.of("A", "B", "C"), (s, i) -> s + i), "A0", "B1",
			"C2");
	}

	@Test
	public void testIndexedForEach() {
		List<String> capture = new ArrayList<>();
		StreamUtil.indexedForEach(List.of("a", "b", "c"), (s, i) -> capture.add(i + ":" + s));
		assertIterable(capture, "0:a", "1:b", "2:c");
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
	public void testCollect() {
		assertIterable(StreamUtil.collect(Stream.of(1, 2, 3), ArrayList::new, List::add), 1, 2, 3);
	}

	@Test
	public void testWrap() {
		try (WrappedStream<IOException, String> w = StreamUtil.wrap(Stream.of("", "a", "x"))) {
			assertThrown(IOException.class, () -> w.mapToInt(s -> {
				if ("x".equals(s)) throw new IOException();
				return s.length();
			}).forEach(x -> {}));
		}
	}

	@Test
	public void testToString() {
		assertNull(StreamUtil.toString(null, "-"));
		assertNull(StreamUtil.toString(null, "(", ":", ")"));
		assertEquals(StreamUtil.toString(Stream.of(1, null, 2), "-"), "1-null-2");
		assertEquals(StreamUtil.toString(Stream.of(1, null, 2), "(", "::", ")"), "(1::null::2)");
	}

	@Test
	public void testAppend() {
		assertStream(StreamUtil.append(Stream.of(), 1, 2), 1, 2);
		assertStream(StreamUtil.append(Stream.of(1, 2, 3)), 1, 2, 3);
		assertStream(StreamUtil.append(Stream.of(1, 2, 3), 4, 5), 1, 2, 3, 4, 5);
	}

	@Test
	public void testPrepend() {
		assertStream(StreamUtil.prepend(Stream.of(), 1, 2), 1, 2);
		assertStream(StreamUtil.prepend(Stream.of(1, 2, 3)), 1, 2, 3);
		assertStream(StreamUtil.prepend(Stream.of(1, 2, 3), 4, 5), 4, 5, 1, 2, 3);
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
		assertCollection(StreamUtil.joinToSet(stream), "1", "2", "3");
	}

	@Test
	public void testJoinToList() {
		Stream<Collection<String>> stream = Stream.of(List.of("1", "2"), List.of("2", "3"));
		assertIterable(StreamUtil.joinToList(stream), "1", "2", "2", "3");
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
	public void testStream() {
		Map<String, Integer> map0 = MapPopulator.of("abc", 1, "DE", 1, "f", 0).map;
		assertArray(StreamUtil.stream(map0, String::charAt).toArray(), 'b', 'E', 'f');
		Map<Integer, String> map1 = MapPopulator.of(1, "1", 3, "3", 2, "2").map;
		Object[] array =
			StreamUtil.stream(map1, (i, s) -> parseDouble(s + "." + (i * i))).toArray();
		assertArray(array, 1.1, 3.9, 2.4);
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
		assertIterable(StreamUtil.toList(StreamUtil.stream(Abc.class)), Abc.A, Abc.B, Abc.C);
	}

	@Test
	public void testStreamEnumeration() {
		Properties props = new Properties();
		props.put("A", 1);
		props.put("B", 2);
		props.put("C", 3);
		assertCollection(StreamUtil.stream(props.elements()).toList(), 1, 2, 3);
	}

	@Test
	public void testToSet() {
		Set<Integer> set = StreamUtil.toSet(Stream.of(1, 3, 2));
		assertCollection(set, 1, 3, 2);
		set = StreamUtil.toSet(Stream.of(1, 3, 2), TreeSet::new);
		assertIterable(set, 1, 2, 3);
	}

	@Test
	public void testMergeFirst() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map = stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeFirst()));
		assertIterable(map.values(), "1", "2", "3");
	}

	@Test
	public void testMergeSecond() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map = stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeSecond()));
		assertIterable(map.values(), "01", "2", "3");
	}

	@Test
	public void testMergeError() {
		Stream<String> stream = Stream.of("1", "2", "3", "1");
		assertThrown(() -> stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeError())));
	}

	@SuppressWarnings("resource")
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
		assertCollection(map.keySet(), 11, 12, 13);
		assertCollection(map.values(), "1", "2", "3");
	}

	@Test
	public void testToMapKeys() {
		Stream<String> stream = Stream.of("1", "2", "3", "4");
		Map<Integer, String> map = StreamUtil.toMap(stream, Integer::parseInt);
		assertIterable(map.keySet(), 1, 2, 3, 4);
		assertEquals(map.get(1), "1");
		assertEquals(map.get(2), "2");
		assertEquals(map.get(3), "3");
		assertEquals(map.get(4), "4");
	}

}
