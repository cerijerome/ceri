package ceri.common.event;

import java.io.Closeable;
import java.util.function.Consumer;
import ceri.common.util.BasicUtil;

public class CloseableListener<T> implements Closeable {
	private static final CloseableListener<?> NULL = new CloseableListener<>(null, null);
	private final Listenable<T> listenable;
	private final Consumer<T> listener;

	public static <T> CloseableListener<T> of(Listenable.Indirect<T> listenable,
		Consumer<T> listener) {
		if (listenable == null) return BasicUtil.uncheckedCast(NULL);
		return of(listenable.listeners(), listener);
	}

	public static <T> CloseableListener<T> of(Listenable<T> listenable, Consumer<T> listener) {
		return new CloseableListener<>(listenable, listener);
	}

	private CloseableListener(Listenable<T> listenable, Consumer<T> listener) {
		this.listenable = listenable;
		this.listener = listener;
		if (listenable != null && listener != null) listenable.listen(listener);
	}

	@Override
	public void close() {
		if (listenable != null && listener != null) listenable.unlisten(listener);
	}
}
