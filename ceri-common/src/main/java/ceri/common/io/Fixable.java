package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.function.Functional;
import ceri.common.util.Capability;

/**
 * A general fixable hardware device.
 */
public interface Fixable extends Closeable, Capability.Name, Listenable.Indirect<StateChange> {

	/**
	 * Notify the device that it is broken. For when the device itself cannot determine it is
	 * broken. Does nothing by default.
	 */
	default void broken() {}

	/**
	 * Open the device.
	 */
	void open() throws IOException;

	/**
	 * Open without throwing an exception. Returns false if open failed.
	 */
	default boolean openSilently() {
		return Functional.muteRun(this::open);
	}

	/**
	 * Convenience method to open a fixable type without throwing an exception, and return the type.
	 */
	static <T extends Fixable> T openSilently(T t) {
		t.openSilently();
		return t;
	}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Fixable {
		@Override
		default Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		default void open() throws IOException {}

		@Override
		default void close() throws IOException {}
	}

	/**
	 * A delegate wrapper.
	 */
	static class Wrapper<T extends Fixable> implements Fixable {
		protected final T delegate;

		protected Wrapper(T delegate) {
			this.delegate = delegate;
		}

		@Override
		public Listenable<StateChange> listeners() {
			return delegate.listeners();
		}

		@Override
		public String name() {
			return delegate.name();
		}

		@Override
		public void broken() {
			delegate.broken();
		}

		@Override
		public void open() throws IOException {
			delegate.open();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}
}
