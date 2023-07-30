package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.util.Named;

/**
 * A general hardware connector that provides input and output data streams.
 */
public interface Connector extends Closeable, Named {
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
			return IoStreamUtil.nullIn;
		}

		@Override
		default OutputStream out() {
			return IoStreamUtil.nullOut;
		}
	}

	/**
	 * Transfer bytes from input to output stream in current thread, until EOF.
	 */
	@SuppressWarnings("resource")
	static void echo(Connector connector) throws IOException {
		IoUtil.pipe(connector.in(), connector.out());
	}
}
