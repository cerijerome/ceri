package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertList;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static java.lang.Double.parseDouble;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class StreamUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StreamUtil.class);
	}

	@Test
	public void testFirstNonNull() {
		assertThat(StreamUtil.firstNonNull(Stream.of(null, null, "abc", "def")), is("abc"));
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
	public void testStream() {
		Map<String, Integer> map0 = MapPopulator.of("abc", 1, "DE", 1, "f", 0).map;
		assertArray(StreamUtil.stream(map0, (s, i) -> s.charAt(i)).toArray(), 'b', 'E', 'f');
		Map<Integer, String> map1 = MapPopulator.of(1, "1", 3, "3", 2, "2").map;
		Object[] array =
			StreamUtil.stream(map1, (i, s) -> parseDouble(s + "." + (i * i))).toArray();
		assertArray(array, 1.1, 3.9, 2.4);
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
