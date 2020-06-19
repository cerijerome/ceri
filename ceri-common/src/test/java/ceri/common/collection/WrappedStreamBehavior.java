package ceri.common.collection;

import static ceri.common.collection.WrappedStreamTestUtil.assertCapture;
import static ceri.common.collection.WrappedStreamTestUtil.assertStream;
import static ceri.common.collection.WrappedStreamTestUtil.assertTerminalThrow;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.predicate;
import static ceri.common.function.FunctionTestUtil.toIntFunction;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.FunctionTestUtil;
import ceri.common.test.Capturer;

@SuppressWarnings("resource")
public class WrappedStreamBehavior {
	private final Capturer<Integer> capture = Capturer.of();

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

	private static ExceptionPredicate<IOException, Consumer<? super Integer>>
		tryAdvanceFn(int... is) {
		Iterator<Integer> i = IntStream.of(is).iterator();
		ExceptionConsumer<IOException, Integer> consumer = FunctionTestUtil.consumer();
		return c -> {
			if (!i.hasNext()) return false;
			int n = i.next();
			consumer.accept(n);
			c.accept(n);
			return true;
		};
	}

	@Test
	public void shouldStreamCollections() throws IOException {
		assertStream(WrappedStream.stream(List.of(4, 3, 2)), 4, 3, 2);
		assertStream(WrappedStream.stream(List.of(4, 3, 2), FunctionTestUtil.function()), 4, 3, 2);
	}

	@Test
	public void shouldWrapValues() throws IOException {
		assertStream(WrappedStream.<IOException, Integer>of(4, 3, 2), 4, 3, 2);
	}

	@Test
	public void shouldThrowTypedExceptionFromMap() throws IOException {
		assertStream(wrap(4, 3, 2).map(function()), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).map(function()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).map(function()));
	}

	@Test
	public void shouldThrowTypedExceptionFromMapToInt() throws IOException {
		assertStream(wrap(4, 3, 2).mapToInt(toIntFunction()), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).mapToInt(toIntFunction()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).mapToInt(toIntFunction()));
	}

	@Test
	public void shouldThrowTypedExceptionFromFilter() throws IOException {
		assertStream(wrap(4, -1, 2).filter(predicate()), 4, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).filter(predicate()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).filter(predicate()));
	}

	@Test
	public void shouldThrowTypedExceptionFromForEach() {
		assertCapture(wrap(4, 3, 2)::forEach, 4, 3, 2);
		assertThrown(IOException.class, () -> wrap(2, 1, 0).forEach(consumer()));
		assertThrown(RuntimeException.class, () -> wrap(3, 2, 0).forEach(consumer()));
		assertThrown(IOException.class, () -> wrap(2, 1, 0).map(function()).forEach(x -> {}));
		assertThrown(RuntimeException.class, () -> wrap(3, 2, 0).map(function()).forEach(x -> {}));
	}

	@Test
	public void shouldThrowTypedExceptionFromCollect() throws IOException {
		assertIterable(wrap(4, 3, 2).collect(Collectors.toList()), 4, 3, 2);
		assertIterable(wrap(4, 3, 2).collect(ArrayList::new, List::add, List::addAll), 4, 3, 2);
		try (WrappedStream<IOException, Integer> stream =
			wrap(4, 3, 1).map(FunctionTestUtil.function())) {
			assertThrown(IOException.class,
				() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
		try (WrappedStream<IOException, Integer> stream =
			wrap(4, 3, 1).map(FunctionTestUtil.function())) {
			assertThrown(IOException.class, () -> stream.collect(Collectors.toList()));
		}
		try (WrappedStream<IOException, Integer> stream =
			wrap(4, 3, 0).map(FunctionTestUtil.function())) {
			assertThrown(RuntimeException.class,
				() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
		try (WrappedStream<IOException, Integer> stream = wrap(4, 3, 0) //
			.map(FunctionTestUtil.function())) {
			assertThrown(RuntimeException.class, () -> stream.collect(Collectors.toList()));
		}
	}

	@Test
	public void shouldApplyStreamMethods() throws IOException {
		assertStream(wrap(4, 3, 2).apply(s -> s.limit(1)), 4);
		assertStream(wrap(4, 3, 2).applyInt(s -> s.mapToInt(i -> i - 1)), 3, 2, 1);
	}

	@Test
	public void shouldTerminateStream() throws IOException {
		assertThat(wrap(4, 3, 2).terminateAs(Stream::count), is(3L));
		wrap(4, 3, 2).terminate(s -> s.forEach(capture.reset()));
		capture.verify(4, 3, 2);
	}

	@Test
	public void shouldFindFirst() throws IOException {
		assertThat(wrap(4, 3, 2).filter(i -> i < 4).findFirst().get(), is(3));
		assertThat(wrap(4, 3, 2).filter(i -> i > 4).findFirst().isEmpty(), is(true));
	}

	@Test
	public void shouldFindAny() throws IOException {
		assertThat(wrap(4, 3, 2).filter(i -> i < 4).findAny().get(), is(3));
		assertThat(wrap(4, 3, 2).filter(i -> i > 4).findAny().isEmpty(), is(true));
	}

	private WrappedStream<IOException, Integer> wrap(Integer... values) {
		return WrappedStream.of(values);
	}

}
