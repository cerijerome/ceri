package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.util.Named;

/**
 * A general fixable hardware device.
 */
public interface Fixable extends Closeable, Named, Listenable.Indirect<StateChange> {

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
	 * Open without throwing a checked exception. Returns false if open failed.
	 */
	default boolean openQuietly() {
		try {
			open();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * A no-op, stateless  implementation.
	 */
	class Null implements Fixable {

		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public void open() throws IOException {}

		@Override
		public void close() {}
	}
}
