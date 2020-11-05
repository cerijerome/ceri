package ceri.log.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;

public interface SocketConnector extends Closeable, Listenable.Indirect<StateChange> {

	/**
	 * For condition-aware serial connectors, notify that it is broken. Useful if the connector
	 * itself cannot determine it is broken.
	 */
	default void broken() {
		throw new UnsupportedOperationException();
	}

	void connect() throws IOException;

	InputStream in();

	OutputStream out();

	/**
	 * Creates a no-op instance.
	 */
	static SocketConnector ofNull() {
		return new Null();
	}

	static class Null implements SocketConnector {
		private final Listenable<StateChange> listenable = Listenable.ofNull();
		private final InputStream in = IoStreamUtil.nullIn();
		private final OutputStream out = IoStreamUtil.nullOut();

		private Null() {}

		@Override
		public Listenable<StateChange> listeners() {
			return listenable;
		}

		@Override
		public void broken() {}

		@Override
		public void connect() {}

		@Override
		public InputStream in() {
			return in;
		}

		@Override
		public OutputStream out() {
			return out;
		}
		
		@Override
		public void close() {}
	}
}
