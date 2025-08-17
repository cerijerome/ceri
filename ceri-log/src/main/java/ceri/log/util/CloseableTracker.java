package ceri.log.util;

import java.util.ArrayList;
import java.util.List;
import ceri.common.collection.Immutable;

/**
 * Use to keep track of closeable objects during construction. If an exception occurs, all the
 * tracked objects can be closed.
 */
public class CloseableTracker {
	private final List<AutoCloseable> tracked = new ArrayList<>();

	public static CloseableTracker of() {
		return new CloseableTracker();
	}

	private CloseableTracker() {}

	public <T extends AutoCloseable> T add(T t) {
		tracked.add(t);
		return t;
	}

	public List<AutoCloseable> list() {
		return Immutable.wrap(tracked);
	}

	public void close() {
		LogUtil.closeReversed(tracked);
	}

}
