package ceri.common.collection;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertList;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
	public void testMergeFirst() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map =
			stream.collect(Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil
				.mergeFirst()));
		assertElements(map.values(), "1", "2", "3");
	}

	@Test
	public void testMergeSecond() {
		Stream<String> stream = Stream.of("1", "2", "3", "01");
		Map<Integer, String> map =
			stream.collect(Collectors.toMap(Integer::parseInt, Function.identity(), StreamUtil
				.mergeSecond()));
		assertElements(map.values(), "01", "2", "3");
	}

	@Test
	public void testMergeError() {
		Stream<String> stream = Stream.of("1", "2", "3", "1");
		assertException(() -> stream.collect(Collectors.toMap(Integer::parseInt, Function
			.identity(), StreamUtil.mergeError())));
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
	public void testToMap() {
		Stream<String> stream = Stream.of("1", "2", "3", "4");
		Map<Integer, String> map = StreamUtil.toMap(stream, Integer::parseInt);
		assertElements(map.keySet(), 1, 2, 3, 4);
		assertThat(map.get(1), is("1"));
		assertThat(map.get(2), is("2"));
		assertThat(map.get(3), is("3"));
		assertThat(map.get(4), is("4"));
	}

}
