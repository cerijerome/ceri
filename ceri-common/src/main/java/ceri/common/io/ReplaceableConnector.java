package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionUtil;

/**
 * A connector pass-through that allows the underlying connector to be replaced. Calling replace()
 * closes the current connector, whereas set() requires the caller to manage connector lifecycles.
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

		@Override
		public void broken() {
			acceptConnector(Connector.Fixable::broken);
		}

		@Override
		public void open() throws IOException {
			acceptValidConnector(Connector.Fixable::open);
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
		Objects.requireNonNull(connector);
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

	/**
	 * Invokes the consumer with the current connector. Does nothing if the connector is not valid.
	 */
	protected <E extends Exception> void acceptConnector(ExceptionConsumer<E, T> consumer)
		throws E {
		FunctionUtil.safeAccept(connector, consumer);
	}

	/**
	 * Invokes the function with the current connector. Returns the given default value if the
	 * connector is not valid.
	 */
	protected <E extends Exception, R> R applyConnector(ExceptionFunction<E, T, R> function, R def)
		throws E {
		return FunctionUtil.safeApply(connector, function, def);
	}

	/**
	 * Invokes the consumer with the current connector. If the connector is not valid, an
	 * IOException is thrown. Error listeners are notified of any exception thrown by the consumer.
	 */
	protected <E extends Exception> void acceptValidConnector(ExceptionConsumer<E, T> consumer)
		throws IOException, E {
		applyValidConnector(socket -> {
			consumer.accept(socket);
			return null;
		});
	}

	/**
	 * Invokes the function with the current connector. If the connector is not valid, an
	 * IOException is thrown. Error listeners are notified of any exception thrown by the function.
	 */
	@SuppressWarnings("resource")
	protected <E extends Exception, R> R applyValidConnector(ExceptionFunction<E, T, R> function)
		throws IOException, E {
		var connector = validConnector();
		try {
			return function.apply(connector);
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	/**
	 * Access the connector. Throws IOException if not connected.
	 */
	private T validConnector() throws IOException {
		var connector = this.connector;
		if (connector == null) throw new IOException("Connector unavailable");
		return connector;
	}

	private void close(T connector) throws IOException {
		if (connector != null) connector.close();
	}
}
