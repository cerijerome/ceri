package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import ceri.common.function.ExceptionConsumer;

/**
 * Provides a Closeable type for an object and a given method on that object. 
 */
public class CloseableWrapper<T> implements Closeable {
	public final T subject;
	private final ExceptionConsumer<IOException, T> closer;

	public static <T> CloseableWrapper<T> of(T subject, ExceptionConsumer<IOException, T> closer) {
		return new CloseableWrapper<>(subject, closer);
	}

	private CloseableWrapper(T subject, ExceptionConsumer<IOException, T> closer) {
		this.subject = subject;
		this.closer = closer;
	}

	@Override
	public void close() throws IOException {
		if (subject == null) return;
		closer.accept(subject);
	}

}
