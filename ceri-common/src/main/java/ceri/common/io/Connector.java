package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.util.Capability;

/**
 * A general hardware connector that provides input and output data streams.
 */
public interface Connector extends Closeable, Capability.Name {
	/**
	 * The hardware input stream.
	 */
	InputStream in();

	/**
	 * The hardware output stream.
	 */
	OutputStream out();

	/**
	 * A connector that is state-aware, with state change notifications.
	 */
	interface Fixable extends Connector, ceri.common.io.Fixable {}

	/**
	 * A no-op, stateless, connector implementation.
	 */
	interface Null extends ceri.common.io.Fixable.Null, Connector.Fixable {
		@Override
		default InputStream in() {
			return IoStream.nullIn;
		}

		@Override
		default OutputStream out() {
			return IoStream.nullOut;
		}
	}

	/**
	 * Transfer bytes from input to output stream in current thread, until EOF.
	 */
	@SuppressWarnings("resource")
	static void echo(Connector connector) throws IOException {
		IoUtil.pipe(connector.in(), connector.out());
	}

	/**
	 * A wrapper base class.
	 */
	static class Wrapper<T extends Connector.Fixable> extends ceri.common.io.Fixable.Wrapper<T>
		implements Connector.Fixable {

		protected Wrapper(T delegate) {
			super(delegate);
		}

		@Override
		public InputStream in() {
			return delegate.in();
		}

		@Override
		public OutputStream out() {
			return delegate.out();
		}
	}
}
