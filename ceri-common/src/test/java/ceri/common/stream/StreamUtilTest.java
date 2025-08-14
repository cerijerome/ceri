package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.Arrays;
import java.util.Map;
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
	public void testBadCombiner() {
		assertThrown(() -> StreamUtil.badCombiner().accept(null, null));
		assertThrown(() -> StreamUtil.badCombiner().accept(1, 2));
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
