package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertThrown;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.test.AssertUtil;
import ceri.common.test.Captor;

public class WrappedStreamTestUtil {

	private WrappedStreamTestUtil() {}

	@SafeVarargs
	public static <E extends Exception, T> void assertForEach(WrappedStream<E, T> stream,
		T... values) {
		assertCapture(stream::forEach, values);
	}

	public static <E extends Exception> void assertForEach(WrappedIntStream<E> stream,
		int... values) {
		assertCapture(stream::forEach, values);
	}

	@SafeVarargs
	public static <E extends Exception, T> void
		assertCapture(ExceptionConsumer<E, ExceptionConsumer<E, T>> fn, T... values) {
		Captor<T> captor = Captor.of();
		try {
			fn.accept(captor::accept);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		captor.verify(values);
	}

	public static <E extends Exception> void
		assertCapture(ExceptionConsumer<E, ExceptionIntConsumer<E>> fn, int... values) {
		Captor.OfInt capture = Captor.ofInt();
		try {
			fn.accept(capture::accept);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		capture.verifyInt(values);
	}

	public static void assertTerminalThrow(Class<? extends Exception> cls,
		WrappedStream<?, ?> stream) {
		assertThrown(cls, () -> terminate(stream));
	}

	public static void assertTerminalThrow(Class<? extends Exception> cls,
		WrappedIntStream<?> stream) {
		assertThrown(cls, () -> terminate(stream));
	}

	public static <E extends Exception> void terminate(WrappedIntStream<E> stream) throws E {
		stream.terminate(s -> s.forEach(_ -> {}));
	}

	public static <E extends Exception> void terminate(WrappedStream<E, ?> stream) throws E {
		stream.terminate(s -> s.forEach(_ -> {}));
	}

	@SafeVarargs
	public static <E extends Exception, T> void assertStream(WrappedStream<E, T> stream,
		T... values) throws E {
		Stream<T> tStream = stream.terminateAs(s -> s);
		AssertUtil.assertStream(tStream, values);
	}

	public static <E extends Exception> void assertStream(WrappedIntStream<E> stream, int... values)
		throws E {
		IntStream iStream = stream.terminateAs(s -> s);
		AssertUtil.assertStream(iStream, values);
	}

}
