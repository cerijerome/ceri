package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.concurrent.Listenable;
import ceri.common.concurrent.Listeners;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.Basics;
import ceri.common.util.Capability;

/**
 * A delegate pass-through that allows the underlying delegate to be replaced. Calling replace()
 * closes the current delegate, whereas set() requires the caller to manage delegate lifecycle.
 */
public abstract class Replaceable<T extends Closeable> implements Closeable, Capability.Name {
	private static final String NAME = "delegate";
	protected final Listeners<Exception> errorListeners = Listeners.of();
	private final String delegateName;
	private volatile T delegate = null;

	/**
	 * An exception thrown when a delegate has not been set.
	 */
	@SuppressWarnings("serial")
	public static class NotSetException extends IOException {
		public NotSetException(String message) {
			super(message);
		}
	}

	/**
	 * An implementation to be used as a member field, with exposed methods. Useful for classes that
	 * cannot extend Replaceable.
	 */
	public static class Field<T extends Closeable> extends Replaceable<T> {
		protected Field(String delegateName) {
			super(delegateName);
		}

		@Override
		public <E extends Exception> void acceptIfSet(Excepts.Consumer<E, T> consumer) throws E {
			super.acceptIfSet(consumer);
		}

		@Override
		public <E extends Exception, R> R applyIfSet(Excepts.Function<E, T, R> function, R def)
			throws E {
			return super.applyIfSet(function, def);
		}

		@Override
		public <E extends Exception> void acceptValid(Excepts.Consumer<E, T> consumer)
			throws IOException, E {
			super.acceptValid(consumer);
		}

		@Override
		public <E extends Exception, R> R applyValid(Excepts.Function<E, T, R> function)
			throws IOException, E {
			return super.applyValid(function);
		}
	}

	/**
	 * A filter input stream that can be reset with a new input stream. Useful for mending broken
	 * streams.
	 */
	public static class In extends InputStream {
		private final Field<InputStream> in = ofField("in");

		private In() {}

		/**
		 * Listen for errors on invoked calls.
		 */
		public Listenable<Exception> errors() {
			return in.errors();
		}

		/**
		 * Close the current delegate, and set the new delegate. Does nothing if no change in
		 * delegate.
		 */
		public void replace(InputStream in) throws IOException {
			this.in.replace(in);
		}

		/**
		 * Set the delegate. Does not close the current delegate.
		 */
		public void set(InputStream in) {
			this.in.set(in);
		}

		@Override
		public int available() throws IOException {
			return in.applyValid(InputStream::available);
		}

		@Override
		public int read() throws IOException {
			return in.applyValid(InputStream::read);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return in.applyValid(i -> i.read(b));
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return in.applyValid(i -> i.read(b, off, len));
		}

		@Override
		public long skip(long n) throws IOException {
			return in.applyValid(i -> i.skip(n));
		}

		@Override
		public void close() throws IOException {
			in.close();
		}

		@Override
		public void mark(int readlimit) {
			in.acceptIfSet(i -> i.mark(readlimit));
		}

		@Override
		public void reset() throws IOException {
			in.acceptValid(InputStream::reset);
		}

		@Override
		public boolean markSupported() {
			return in.applyIfSet(i -> i.markSupported(), false);
		}
	}

	/**
	 * A filter output stream that can be reset with a new output stream. Useful for mending broken
	 * streams.
	 */
	public static class Out extends OutputStream {
		private final Field<OutputStream> out = ofField("out");

		/**
		 * Listen for errors on invoked calls.
		 */
		public Listenable<Exception> errors() {
			return out.errors();
		}

		/**
		 * Close the current delegate, and set the new delegate. Does nothing if no change in
		 * delegate.
		 */
		public void replace(OutputStream out) throws IOException {
			this.out.replace(out);
		}

		/**
		 * Set the delegate. Does not close the current delegate.
		 */
		public void set(OutputStream out) {
			this.out.set(out);
		}

		@Override
		public void write(int b) throws IOException {
			out.acceptValid(o -> o.write(b));
		}

		@Override
		public void write(byte[] b) throws IOException {
			out.acceptValid(o -> o.write(b));
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.acceptValid(o -> o.write(b, off, len));
		}

		@Override
		public void flush() throws IOException {
			out.acceptValid(OutputStream::flush);
		}

		@Override
		public void close() throws IOException {
			out.close();
		}
	}

	/**
	 * A connector pass-through that allows the underlying connector to be replaced. Calling
	 * replace() closes the current connector, whereas set() requires the caller to manage connector
	 * lifecycles.
	 */
	public static class Con<T extends Connector> extends Replaceable<T> implements Connector {
		private static final String NAME = "connector";
		private final In in = ofIn();
		private final Out out = ofOut();

		/**
		 * Extends the connector to be fixable.
		 */
		public static class Fixable<T extends Connector.Fixable> extends Con<T>
			implements Connector.Fixable {
			private final Listeners<Device.Change> listeners = Listeners.of();
			private final Functions.Consumer<Device.Change> listener = this::stateChange;

			protected Fixable(String delegateName) {
				super(delegateName);
			}

			@Override
			public Listenable<Device.Change> listeners() {
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

			private void stateChange(Device.Change state) {
				listeners.accept(state);
			}

			private void unlisten() {
				acceptIfSet(connector -> connector.listeners().unlisten(listener));
			}
		}

		protected Con(String delegateName) {
			super(Basics.def(delegateName, NAME));
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

	/**
	 * Creates a delegate field instance.
	 */
	public static <T extends Closeable> Field<T> ofField() {
		return ofField(NAME);
	}

	/**
	 * Creates a named replaceable field instance.
	 */
	public static <T extends Closeable> Field<T> ofField(String name) {
		return new Field<>(name);
	}

	/**
	 * Returns a new replaceable input stream instance.
	 */
	public static In ofIn() {
		return new In();
	}

	/**
	 * Returns a new replaceable output stream instance.
	 */
	public static Out ofOut() {
		return new Out();
	}

	/**
	 * Returns a new fixable replaceable connector with optional delegate name.
	 */
	public static <T extends Connector.Fixable> Con.Fixable<T> ofCon(String delegateName) {
		return new Con.Fixable<>(delegateName);
	}

	protected Replaceable(String delegateName) {
		this.delegateName = delegateName;
	}

	/**
	 * Listen for errors on invoked calls.
	 */
	public Listenable<Exception> errors() {
		return errorListeners;
	}

	/**
	 * Close the current delegate, and set the new delegate. Does nothing if no change in delegate.
	 */
	public void replace(T delegate) throws IOException {
		if (this.delegate == delegate) return;
		close();
		set(delegate);
	}

	/**
	 * Sets the delegate. Does not close the current delegate.
	 */
	public void set(T delegate) {
		this.delegate = delegate;
	}

	@Override
	public void close() throws IOException {
		close(delegate);
	}

	/**
	 * Invokes the consumer with the current delegate. Does nothing if the delegate is not set.
	 */
	protected <E extends Exception> void acceptIfSet(Excepts.Consumer<E, T> consumer) throws E {
		applyIfSet(delegate -> {
			consumer.accept(delegate);
			return null;
		}, null);
	}

	/**
	 * Invokes the function with the current delegate. Returns the given default value if the
	 * delegate is not set.
	 */
	protected <E extends Exception, R> R applyIfSet(Excepts.Function<E, T, R> function, R def)
		throws E {
		var delegate = this.delegate;
		if (delegate == null) return def;
		try {
			return function.apply(delegate);
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	/**
	 * Invokes the consumer with the current delegate. If the delegate is not set, an exception is
	 * thrown. Error listeners are notified of any exception thrown by the consumer.
	 */
	protected <E extends Exception> void acceptValid(Excepts.Consumer<E, T> consumer)
		throws IOException, E {
		applyValid(delegate -> {
			consumer.accept(delegate);
			return null;
		});
	}

	/**
	 * Invokes the function with the current delegate. If the delegate is not set, an exception is
	 * thrown. Error listeners are notified of any exception thrown by the function.
	 */
	@SuppressWarnings("resource")
	protected <E extends Exception, R> R applyValid(Excepts.Function<E, T, R> function)
		throws IOException, E {
		var delegate = validDelegate();
		try {
			return function.apply(delegate);
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	/**
	 * Access the delegate. Throws exception if not set.
	 */
	private T validDelegate() throws IOException {
		var delegate = this.delegate;
		if (delegate == null) throw new NotSetException(delegateName);
		return delegate;
	}

	private void close(T delegate) throws IOException {
		if (delegate != null) delegate.close();
	}
}
