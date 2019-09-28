package ceri.common.collection;

import static ceri.common.test.TestUtil.assertThrown;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.test.Capturer;
import ceri.common.test.TestUtil;

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
	public static <E extends Exception, T> void assertCapture(
		ExceptionConsumer<E, ExceptionConsumer<E, T>> fn, T... values) {
		Capturer<T> capture = Capturer.of();
		try {
			fn.accept(capture.toEx());
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		capture.verify(values);
	}

	public static <E extends Exception> void assertCapture(
		ExceptionConsumer<E, ExceptionIntConsumer<E>> fn, int... values) {
		Capturer.Int capture = Capturer.ofInt();
		try {
			fn.accept(capture.toExInt());
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
		stream.terminate(s -> s.forEach(x -> {}));
	}

	public static <E extends Exception> void terminate(WrappedStream<E, ?> stream) throws E {
		stream.terminate(s -> s.forEach(x -> {}));
	}

	@SafeVarargs
	public static <E extends Exception, T> void assertStream(WrappedStream<E, T> stream,
		T... values) throws E {
		Stream<T> tStream = stream.terminateAs(s -> s);
		TestUtil.assertStream(tStream, values);
	}

	public static <E extends Exception> void assertStream(WrappedIntStream<E> stream,
		int... values)
		throws E {
		IntStream iStream = stream.terminateAs(s -> s);
		TestUtil.assertStream(iStream, values);
	}

}
