package ceri.common.collection;

import static ceri.common.collection.WrappedStreamTestUtil.assertCapture;
import static ceri.common.collection.WrappedStreamTestUtil.assertStream;
import static ceri.common.collection.WrappedStreamTestUtil.assertTerminalThrow;
import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.function.FunctionTestUtil.intFunction;
import static ceri.common.function.FunctionTestUtil.intPredicate;
import static ceri.common.function.FunctionTestUtil.intUnaryOperator;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.test.Capturer;

public class WrappedIntStreamBehavior {
	private final Capturer.Int capture = Capturer.ofInt();

	// Function type generators:
	// 0 => throws RuntimeException
	// 1 => throws IOException

	@Test
	public void shouldWrapRange() throws IOException {
		assertStream(WrappedIntStream.<IOException>range(3), 0, 1, 2);
	}

	@Test
	public void shouldThrowTypedExceptionFromMap() throws IOException {
		assertStream(wrap(4, 3, 2).map(intUnaryOperator()), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).map(intUnaryOperator()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).map(intUnaryOperator()));
	}

	@Test
	public void shouldThrowTypedExceptionFromMapToInt() throws IOException {
		assertStream(wrap(4, 3, 2).mapToObj(intFunction()), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).mapToObj(intFunction()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).mapToObj(intFunction()));
	}

	@Test
	public void shouldThrowTypedExceptionFromFilter() throws IOException {
		assertStream(wrap(4, -1, 2).filter(intPredicate()), 4, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).filter(intPredicate()));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).filter(intPredicate()));
	}

	@Test
	public void shouldThrowTypedExceptionFromForEach() throws IOException {
		assertCapture(wrap(4, 3, 2)::forEach, 4, 3, 2);
		assertThrown(IOException.class, () -> wrap(2, 1, 0).forEach(intConsumer()));
		assertThrown(RuntimeException.class, () -> wrap(3, 2, 0).forEach(intConsumer()));
		assertThrown(IOException.class,
			() -> wrap(2, 1, 0).map(intUnaryOperator()).forEach(x -> {}));
		assertThrown(RuntimeException.class,
			() -> wrap(3, 2, 0).map(intUnaryOperator()).forEach(x -> {}));
	}

	@Test
	public void shouldApplyStreamMethods() throws IOException {
		assertStream(wrap(4, 3, 2).apply(s -> s.limit(1)), 4);
		assertStream(wrap(4, 3, 2).applyObj(s -> s.mapToObj(i -> i - 1)), 3, 2, 1);
	}

	@Test
	public void shouldTerminateStream() throws IOException {
		assertThat(wrap(4, 3, 2).terminateAs(IntStream::count), is(3L));
		wrap(4, 3, 2).terminate(s -> s.forEach(capture.reset()));
		capture.verify(4, 3, 2);
	}

	private WrappedIntStream<IOException> wrap(int... values) {
		return WrappedIntStream.of(values);
	}
}
