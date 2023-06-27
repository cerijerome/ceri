package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;

/**
 * A connector pass-through that allows the underlying connector to be replaced. Calling replace()
 * closes the current connector, whereas set() relies on the caller to manage connector lifecycle.
 */
public class ReplaceableConnector<T extends Connector> implements Connector {
	private final Listeners<Exception> errorListeners = Listeners.of();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	protected volatile T connector = null;

	public static class Fixable<T extends Connector.Fixable> extends ReplaceableConnector<T>
		implements Connector.Fixable {
		private final Listeners<StateChange> listeners = Listeners.of();
		private final Consumer<StateChange> listener = this::listen;

		@Override
		public Listenable<StateChange> listeners() {
			return listeners;
		}

		@Override
		public void set(T connector) {
			unlisten(this.connector);
			super.set(connector);
			connector.listeners().listen(listener);
		}

		@SuppressWarnings("resource")
		@Override
		public void broken() {
			runtimeConnector().broken();
		}

		@Override
		public void open() throws IOException {
			exec(Connector.Fixable::open);
		}

		@Override
		public void close() throws IOException {
			unlisten(connector);
			super.close();
		}

		private void listen(StateChange state) {
			listeners.accept(state);
		}

		private void unlisten(T connector) {
			if (connector != null) connector.listeners().unlisten(listener);
		}
	}

	public ReplaceableConnector() {
		in.listeners().listen(errorListeners);
		out.listeners().listen(errorListeners);
	}

	public Listenable<Exception> errorListeners() {
		return errorListeners;
	}

	/**
	 * Close the current connector, and set the new connector.
	 */
	public void replace(T connector) throws IOException {
		close();
		set(connector);
	}

	/**
	 * Sets the socket connector. Does not close the current connector.
	 */
	@SuppressWarnings("resource")
	public void set(T connector) {
		this.connector = connector;
		in.setInputStream(connector.in());
		out.setOutputStream(connector.out());
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void close() throws IOException {
		close(connector);
	}

	protected void exec(ExceptionConsumer<IOException, T> consumer) throws IOException {
		execGet(socket -> {
			consumer.accept(socket);
			return null;
		});
	}

	@SuppressWarnings("resource")
	protected <U> U execGet(ExceptionFunction<IOException, T, U> function) throws IOException {
		try {
			return function.apply(connector());
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	protected T connector() throws IOException {
		var connector = this.connector;
		if (connector == null) throw new IOException("Connector unavailable");
		return connector;
	}

	protected T runtimeConnector() {
		var connector = this.connector;
		if (connector == null) throw new IllegalStateException("Connector unavailable");
		return connector;
	}

	private void close(T connector) throws IOException {
		if (connector != null) connector.close();
	}
}
