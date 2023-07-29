package ceri.log.util;

import static ceri.common.collection.CollectionUtil.reverse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		return Collections.unmodifiableList(reverse(new ArrayList<>(tracked)));
	}

	public void close() {
		LogUtil.close(list());
	}

}
