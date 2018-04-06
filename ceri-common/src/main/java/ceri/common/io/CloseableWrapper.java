package ceri.common.io;

import java.io.IOException;
import ceri.common.function.ExceptionConsumer;

public class CloseableWrapper<T> implements java.io.Closeable {
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
