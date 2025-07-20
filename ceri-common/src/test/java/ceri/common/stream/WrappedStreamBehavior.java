package ceri.common.stream;

import static ceri.common.stream.WrappedStreamTestUtil.assertCapture;
import static ceri.common.stream.WrappedStreamTestUtil.assertStream;
import static ceri.common.stream.WrappedStreamTestUtil.assertTerminalThrow;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.function.FunctionTestUtil.E;
import ceri.common.stream.WrappedStream;
import ceri.common.test.Captor;
import ceri.common.test.TestUtil.Ioe;

@SuppressWarnings("resource")
public class WrappedStreamBehavior {
	private final Captor<Integer> captor = Captor.of();

	// Function type generators:
	// 0 => throws RuntimeException
	// 1 => throws IOException

	@Test
	public void shouldStreamFromFunctions() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "a", "b", "c");
		assertStream(WrappedStream.stream(() -> !list.isEmpty(), () -> list.remove(0)), "a", "b",
			"c");
	}

	@Test
	public void shouldAllowStreamingFromNullFunctions() {
		assertStream(WrappedStream.stream(null, () -> "test"));
		int[] count = { 0 };
		assertStream(WrappedStream.stream(() -> count[0]++ < 5, null));
	}

	@Test
	public void shouldStreamAsSpliterator() throws IOException {
		assertStream(WrappedStream.stream(tryAdvanceFn(4, 3, 2)), 4, 3, 2);
		assertTerminalThrow(IOException.class, WrappedStream.stream(tryAdvanceFn(4, 3, 1)));
		assertTerminalThrow(RuntimeException.class, WrappedStream.stream(tryAdvanceFn(4, 3, 0)));
	}

	private static Excepts.Predicate<IOException, Consumer<? super Integer>>
		tryAdvanceFn(int... is) {
		Iterator<Integer> i = IntStream.of(is).iterator();
		return c -> {
			if (!i.hasNext()) return false;
			int n = i.next();
			E.consumer.accept(n);
			c.accept(n);
			return true;
		};
	}

	@Test
	public void shouldStreamCollections() throws Exception {
		assertStream(WrappedStream.stream(List.of(4, 3, 2)), 4, 3, 2);
		assertStream(WrappedStream.stream(List.of(4, 3, 2), E.function), 4, 3, 2);
	}

	@Test
	public void shouldWrapValues() throws IOException {
		assertStream(WrappedStream.<IOException, Integer>of(4, 3, 2), 4, 3, 2);
	}

	@Test
	public void shouldThrowTypedExceptionFromMap() throws IOException {
		assertStream(wrap(4, 3, 2).map(E.function), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).map(E.function));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).map(E.function));
	}

	@Test
	public void shouldThrowTypedExceptionFromMapToInt() throws IOException {
		assertStream(wrap(4, 3, 2).mapToInt(E.toIntFunction), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).mapToInt(E.toIntFunction));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).mapToInt(E.toIntFunction));
	}

	@Test
	public void shouldThrowTypedExceptionFromFilter() throws IOException {
		assertStream(wrap(4, -1, 2).filter(E.predicate), 4, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).filter(E.predicate));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).filter(E.predicate));
	}

	@Test
	public void shouldThrowTypedExceptionFromForEach() {
		assertCapture(wrap(4, 3, 2)::forEach, 4, 3, 2);
		assertIoe(() -> wrap(2, 1, 0).forEach(E.consumer));
		assertRte(() -> wrap(3, 2, 0).forEach(E.consumer));
		assertIoe(() -> wrap(2, 1, 0).map(E.function).forEach(_ -> {}));
		assertRte(() -> wrap(3, 2, 0).map(E.function).forEach(_ -> {}));
	}

	@Test
	public void shouldThrowTypedExceptionFromCollect() throws IOException {
		assertIterable(wrap(4, 3, 2).collect(Collectors.toList()), 4, 3, 2);
		assertIterable(wrap(4, 3, 2).collect(ArrayList::new, List::add, List::addAll), 4, 3, 2);
		try (WrappedStream<Ioe, Integer> stream = wrap(4, 3, 1).map(E.function)) {
			assertIoe(() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
		try (WrappedStream<Ioe, Integer> stream = wrap(4, 3, 1).map(E.function)) {
			assertIoe(() -> stream.collect(Collectors.toList()));
		}
		try (WrappedStream<Ioe, Integer> stream = wrap(4, 3, 0).map(E.function)) {
			assertRte(() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
		try (WrappedStream<Ioe, Integer> stream = wrap(4, 3, 0) //
			.map(E.function)) {
			assertRte(() -> stream.collect(Collectors.toList()));
		}
	}

	@Test
	public void shouldApplyStreamMethods() throws IOException {
		assertStream(wrap(4, 3, 2).apply(s -> s.limit(1)), 4);
		assertStream(wrap(4, 3, 2).applyInt(s -> s.mapToInt(i -> i - 1)), 3, 2, 1);
	}

	@Test
	public void shouldTerminateStream() throws IOException {
		assertEquals(wrap(4, 3, 2).terminateAs(Stream::count), 3L);
		wrap(4, 3, 2).terminate(s -> s.forEach(captor.reset()));
		captor.verify(4, 3, 2);
	}

	@Test
	public void shouldFindAny() throws IOException {
		assertEquals(wrap(4, 3, 2).filter(i -> i < 4).findAny().get(), 3);
		assertTrue(wrap(4, 3, 2).filter(i -> i > 4).findAny().isEmpty());
	}

	private WrappedStream<Ioe, Integer> wrap(Integer... values) {
		return WrappedStream.of(values);
	}

}
