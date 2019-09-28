package ceri.common.collection;

import static ceri.common.collection.WrappedStreamTestUtil.assertCapture;
import static ceri.common.collection.WrappedStreamTestUtil.assertStream;
import static ceri.common.collection.WrappedStreamTestUtil.assertTerminalThrow;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.predicate;
import static ceri.common.function.FunctionTestUtil.toIntFunction;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.test.Capturer;

public class WrappedStreamBehavior {
	private final Capturer<Integer> capture = Capturer.of();

	// Function type generators:
	// 0 => throws RuntimeException
	// 1 => throws IOException

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

	private WrappedStream<IOException, Integer> wrap(Integer... values) {
		return WrappedStream.of(IOException.class, values);
	}

}
