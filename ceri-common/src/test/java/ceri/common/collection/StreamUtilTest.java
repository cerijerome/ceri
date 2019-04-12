package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertList;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertStream;
import static java.lang.Double.parseDouble;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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

public class StreamUtilTest {
	private static enum Abc {
		A,
		B,
		C;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StreamUtil.class);
	}

	@Test
	public void testUnitRange() {
		assertThat(StreamUtil.unitRange(5).toArray(),
			is(new double[] { 0.0, 0.25, 0.5, 0.75, 1.0 }));
	}

	@Test
	public void testToInt() {
		assertThat(StreamUtil.toInt(List.of(1.2, 2.4, 3.6, 4.8).stream()).toArray(),
			is(new int[] { 1, 2, 3, 4 }));
	}

	@Test
	public void testBitwiseOperators() {
		assertThat(StreamUtil.bitwiseOr(IntStream.of(1, 2, 5)), is(7));
		assertThat(StreamUtil.bitwiseAnd(IntStream.of(15, 7, 14)), is(6));
		assertThat(StreamUtil.bitwiseXor(IntStream.of(1, 2, 5)), is(6));
	}

	@Test
	public void testCastAny() {
		Stream<Number> stream = StreamUtil.castAny(Stream.of("1", 1, 0.1, null, 2), Number.class);
		assertStream(stream, 1, 0.1, 2);
	}

	@Test
	public void testToString() {
		assertNull(StreamUtil.toString(null, "-"));
		assertNull(StreamUtil.toString(null, "(", ":", ")"));
		assertThat(StreamUtil.toString(Stream.of(1, null, 2), "-"), is("1-null-2"));
		assertThat(StreamUtil.toString(Stream.of(1, null, 2), "(", "::", ")"), is("(1::null::2)"));
	}

	@Test
	public void testIsEmpty() {
		assertThat(StreamUtil.isEmpty(Stream.of()), is(true));
		assertThat(StreamUtil.isEmpty(Stream.of("test")), is(false));
	}

	@Test
	public void testFindFirstNonNull() {
		Stream<String> stream = Stream.of(null, null, "abc", "de", "f");
		assertThat(StreamUtil.findFirstNonNull(stream, s -> s.length() < 3), is("de"));
	}

	@Test
	public void testFirstNonNull() {
		Stream<String> stream = Stream.of(null, null, "abc", "def");
		assertThat(StreamUtil.firstNonNull(stream), is("abc"));
	}

	@Test
	public void testMaxAndMin() {
		assertThat(StreamUtil.min(Stream.of("abc", "", "ABC")), is(""));
		assertThat(StreamUtil.max(Stream.of("abc", "", "ABC")), is("abc"));
		assertException(() -> StreamUtil.min(Stream.of("abc", null, "ABC")));
		assertThat(StreamUtil.max(Stream.of("abc", null, "ABC")), is("abc"));
	}

	@Test
	public void testFirst() {
		Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).filter(i -> i % 2 == 0);
		assertThat(StreamUtil.first(stream), is(2));
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
		assertThat(StreamUtil.toEntryMap(Map.of(1, "1", 2, "2").entrySet().stream()),
			is(Map.of(1, "1", 2, "2")));
	}

	@Test
	public void testEntryCollector() {
		assertThat(Map.of(1, "1", 2, "2").entrySet().stream().collect(StreamUtil.entryCollector()),
			is(Map.of(1, "1", 2, "2")));
	}

	@Test
	public void testStream() {
		Map<String, Integer> map0 = MapPopulator.of("abc", 1, "DE", 1, "f", 0).map;
		assertArray(StreamUtil.stream(map0, (s, i) -> s.charAt(i)).toArray(), 'b', 'E', 'f');
		Map<Integer, String> map1 = MapPopulator.of(1, "1", 3, "3", 2, "2").map;
		Object[] array =
			StreamUtil.stream(map1, (i, s) -> parseDouble(s + "." + (i * i))).toArray();
		assertArray(array, 1.1, 3.9, 2.4);
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
		assertException(() -> stream.collect(
			Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil.mergeError())));
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
		assertCollection(map.keySet(), 11, 12, 13);
		assertCollection(map.values(), "1", "2", "3");
	}

	@Test
	public void testToMapKeys() {
		Stream<String> stream = Stream.of("1", "2", "3", "4");
		Map<Integer, String> map = StreamUtil.toMap(stream, Integer::parseInt);
		assertIterable(map.keySet(), 1, 2, 3, 4);
		assertThat(map.get(1), is("1"));
		assertThat(map.get(2), is("2"));
		assertThat(map.get(3), is("3"));
		assertThat(map.get(4), is("4"));
	}

}
