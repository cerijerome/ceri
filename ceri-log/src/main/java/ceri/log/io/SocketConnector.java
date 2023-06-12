package ceri.log.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;

public interface SocketConnector extends Closeable, StateChange.Fixable {

	HostPort hostPort();

	void open() throws IOException;

	InputStream in();

	OutputStream out();

	/**
	 * A stateless, no-op instance.
	 */
	SocketConnector NULL = new SocketConnector() {
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public void broken() {}

		@Override
		public HostPort hostPort() {
			return HostPort.NULL;
		}

		@Override
		public void open() {}

		@Override
		public InputStream in() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public OutputStream out() {
			return IoStreamUtil.nullOut;
		}

		@Override
		public void close() {}
	};
}
