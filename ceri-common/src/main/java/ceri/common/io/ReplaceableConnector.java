package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;

/**
 * A connector pass-through that allows the underlying connector to be replaced. Calling replace()
 * closes the current connector, whereas set() requires the caller to manage connector lifecycles.
 */
public class ReplaceableConnector<T extends Connector> extends Replaceable<T> implements Connector {
	private static final String NAME = "connector";
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();

	public static class Fixable<T extends Connector.Fixable> extends ReplaceableConnector<T>
		implements Connector.Fixable {
		private final Listeners<StateChange> listeners = Listeners.of();
		private final Consumer<StateChange> listener = this::stateChange;

		public Fixable() {}

		protected Fixable(String delegateName) {
			super(delegateName);
		}

		@Override
		public Listenable<StateChange> listeners() {
			return listeners;
		}

		@Override
		public void set(T connector) {
			unlisten();
			super.set(connector);
			connector.listeners().listen(listener);
		}

		@Override
		public void broken() {
			acceptIfSet(Connector.Fixable::broken);
		}

		@Override
		public void open() throws IOException {
			acceptValid(Connector.Fixable::open);
		}

		@Override
		public void close() throws IOException {
			unlisten();
			super.close();
		}

		private void stateChange(StateChange state) {
			listeners.accept(state);
		}

		private void unlisten() {
			acceptIfSet(connector -> connector.listeners().unlisten(listener));
		}
	}

	public ReplaceableConnector() {
		this(NAME);
	}

	protected ReplaceableConnector(String delegateName) {
		super(delegateName);
		in.errors().listen(errorListeners);
		out.errors().listen(errorListeners);
	}

	@SuppressWarnings("resource")
	@Override
	public void set(T delegate) {
		super.set(delegate);
		in.set(delegate.in());
		out.set(delegate.out());
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}
}
