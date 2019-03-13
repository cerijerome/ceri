package ceri.common.event;

import java.io.Closeable;
import java.util.function.IntConsumer;

/**
 * Provides a Closeable wrapper for listeners, that unlistens when closed.
 */
public class CloseableIntListener implements Closeable {
	private static final CloseableIntListener NULL = new CloseableIntListener(null, null);
	private final IntListenable listenable;
	private final IntConsumer listener;

	public static CloseableIntListener of(IntListenable.Indirect listenable, IntConsumer listener) {
		if (listenable == null) return NULL;
		return of(listenable.listeners(), listener);
	}

	public static CloseableIntListener of(IntListenable listenable, IntConsumer listener) {
		return new CloseableIntListener(listenable, listener);
	}

	private CloseableIntListener(IntListenable listenable, IntConsumer listener) {
		this.listenable = listenable;
		this.listener = listener;
		if (listenable != null && listener != null) listenable.listen(listener);
	}

	@Override
	public void close() {
		if (listenable != null && listener != null) listenable.unlisten(listener);
	}
}
