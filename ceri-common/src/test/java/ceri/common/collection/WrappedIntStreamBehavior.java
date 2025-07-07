package ceri.common.collection;

import static ceri.common.collection.WrappedStreamTestUtil.assertCapture;
import static ceri.common.collection.WrappedStreamTestUtil.assertStream;
import static ceri.common.collection.WrappedStreamTestUtil.assertTerminalThrow;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertRte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.E;
import ceri.common.test.Captor;
import ceri.common.test.TestUtil.Ioe;

public class WrappedIntStreamBehavior {
	private final Captor.OfInt capture = Captor.ofInt();

	// Function type generators:
	// 0 => throws RuntimeException
	// 1 => throws IOException

	@SuppressWarnings("resource")
	@Test
	public void shouldWrapRange() throws IOException {
		assertStream(WrappedIntStream.<IOException>range(3), 0, 1, 2);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldThrowTypedExceptionFromMap() throws IOException {
		assertStream(wrap(4, 3, 2).map(E.intOperator), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).map(E.intOperator));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).map(E.intOperator));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldThrowTypedExceptionFromMapToInt() throws IOException {
		assertStream(wrap(4, 3, 2).mapToObj(E.intFunction), 4, 3, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).mapToObj(E.intFunction));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).mapToObj(E.intFunction));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldThrowTypedExceptionFromFilter() throws IOException {
		assertStream(wrap(4, -1, 2).filter(E.intPredicate), 4, 2);
		assertTerminalThrow(IOException.class, wrap(2, 1, 0).filter(E.intPredicate));
		assertTerminalThrow(RuntimeException.class, wrap(3, 2, 0).filter(E.intPredicate));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBoxInts() throws IOException {
		assertStream(wrap(4, -1, 2).boxed(), 4, -1, 2);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldThrowTypedExceptionFromForEach() {
		assertCapture(wrap(4, 3, 2)::forEach, 4, 3, 2);
		assertIoe(() -> wrap(2, 1, 0).forEach(E.intConsumer));
		assertRte(() -> wrap(3, 2, 0).forEach(E.intConsumer));
		assertIoe(() -> wrap(2, 1, 0).map(E.intOperator).forEach(_ -> {}));
		assertRte(() -> wrap(3, 2, 0).map(E.intOperator).forEach(_ -> {}));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldThrowTypedExceptionFromCollect() throws IOException {
		assertIterable(wrap(4, 3, 2).collect(ArrayList::new, List::add, List::addAll), 4, 3, 2);
		try (WrappedIntStream<Ioe> stream = wrap(4, 3, 1).map(E.intOperator)) {
			assertIoe(() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
		try (WrappedIntStream<Ioe> stream = wrap(4, 3, 0).map(E.intOperator)) {
			assertRte(() -> stream.collect(ArrayList::new, List::add, List::addAll));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldApplyStreamMethods() throws IOException {
		assertStream(wrap(4, 3, 2).apply(s -> s.limit(1)), 4);
		assertStream(wrap(4, 3, 2).applyObj(s -> s.mapToObj(i -> i - 1)), 3, 2, 1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldTerminateStream() throws IOException {
		assertEquals(wrap(4, 3, 2).terminateAs(IntStream::count), 3L);
		wrap(4, 3, 2).terminate(s -> s.forEach(capture.reset()));
		capture.verify(4, 3, 2);
	}

	private WrappedIntStream<Ioe> wrap(int... values) {
		return WrappedIntStream.of(values);
	}
}
