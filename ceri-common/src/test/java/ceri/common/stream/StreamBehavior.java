package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertSame;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.test.TestUtil.Rte;

public class StreamBehavior {

	@Test
	public void shouldProvideEmptyStreamInstance() throws Exception {
		var empty = Stream.empty();
		assertEquals(empty.next(), null);
		assertSame(Stream.of((String[]) null), empty);
		assertSame(Stream.from(List.of()), empty);
		assertSame(Stream.from((Iterable<String>) null), empty);
		assertSame(Stream.from((Iterator<String>) null), empty);
		assertSame(Stream.of().filter(t -> t != null), empty);
		assertSame(Stream.of().map(t -> String.valueOf(t)), empty);
	}

	@Test
	public void shouldStreamIterables() throws Exception {
		assertIterable(Stream.from(List.of()).toList());
		assertIterable(Stream.from(List.of(-1, 0, 1)).toList(), -1, 0, 1);
	}

	@Test
	public void shouldFilterElements() throws Exception {
		assertIterable(Stream.of().filter(_ -> true).toList());
		assertIterable(Stream.of().filter(_ -> false).toSet());
		assertIterable(Stream.<Rte, Integer>of().filter(i -> i >= 0).toList());
		assertIterable(Stream.of(-1, 0, 1).filter(_ -> false).toList());
		assertIterable(Stream.of(-1, 0, 1).filter(_ -> true).toList(), -1, 0, 1);
		assertCollection(Stream.of(-1, 0, 1).filter(i -> i >= 0).toSet(), 0, 1);
	}

	@Test
	public void shouldMapElements() throws Exception {
		assertIterable(Stream.of().map(String::valueOf).toList());
		assertIterable(Stream.of(-1, null, 1).map(String::valueOf).toList(), "-1", "null", "1");
	}

	@Test
	public void shouldCollectAsArray() throws Exception {
		assertArray(Stream.of().toArray(String[]::new));
		assertArray(Stream.of("test", null, "").toArray(String[]::new), "test", null, "");
	}

	@Test
	public void shouldLimitElements() throws Exception {
		assertIterable(Stream.of(1, 2, 3, 4, 5).limit(0).toList());
		assertIterable(Stream.of(1, 2, 3, 4, 5).limit(3).toList(), 1, 2, 3);
		assertIterable(Stream.of(1, 2, 3, 4, 5).limit(6).toList(), 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldMatchAny() throws Exception {
		assertEquals(Stream.of().anyMatch(_ -> true), false);
		assertEquals(Stream.of().anyMatch(_ -> false), false);
		assertEquals(Stream.of(-1, 0, 1).anyMatch(i -> i > 1), false);
		assertEquals(Stream.of(-1, 0, 1).anyMatch(i -> i <= 0), true);
	}

	@Test
	public void shouldMatchAll() throws Exception {
		assertEquals(Stream.of().allMatch(_ -> true), true);
		assertEquals(Stream.of().allMatch(_ -> false), true);
		assertEquals(Stream.of(-1, 0, 1).allMatch(i -> i > 1), false);
		assertEquals(Stream.of(-1, 0, 1).allMatch(i -> i <= 1), true);
	}

	@Test
	public void shouldMatchNone() throws Exception {
		assertEquals(Stream.of().noneMatch(_ -> true), true);
		assertEquals(Stream.of().noneMatch(_ -> false), true);
		assertEquals(Stream.of(-1, 0, 1).noneMatch(i -> i > 1), true);
		assertEquals(Stream.of(-1, 0, 1).noneMatch(i -> i <= 1), false);
	}

	@Test
	public void shouldReturnNext() throws Exception {
		assertEquals(Stream.of().next(), null);
		assertEquals(Stream.of().next(0), 0);
		assertEquals(Stream.of(-1, 0, 1).filter(i -> i > 1).next(), null);
		assertEquals(Stream.of(-1, 0, 1).filter(i -> i > 1).next(0), 0);
		assertEquals(Stream.of(-1, 0, 1).filter(i -> i <= 1).next(), -1);
		assertEquals(Stream.of(-1, 0, 1).filter(i -> i <= 1).next(0), -1);
	}

}
