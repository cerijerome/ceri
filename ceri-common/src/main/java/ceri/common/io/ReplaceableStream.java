package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.Functions;
import ceri.common.util.Basics;

public class ReplaceableStream {
	private ReplaceableStream() {}

	/**
	 * Returns a new replaceable input stream instance.
	 */
	public static In in() {
		return new In();
	}

	/**
	 * Returns a new replaceable output stream instance.
	 */
	public static Out out() {
		return new Out();
	}

	/**
	 * Returns a new fixable replaceable connector with optional delegate name.
	 */
	public static <T extends Connector.Fixable> Con.Fixable<T> con(String delegateName) {
		return new Con.Fixable<>(delegateName);
	}

	/**
	 * A filter input stream that can be reset with a new input stream. Useful for mending broken
	 * streams.
	 */
	public static class In extends InputStream {
		private final Replaceable.Field<InputStream> in = Replaceable.field("in");

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
		private final Replaceable.Field<OutputStream> out = Replaceable.field("out");

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
		private final In in = ReplaceableStream.in();
		private final Out out = ReplaceableStream.out();

		public static class Fixable<T extends Connector.Fixable> extends Con<T>
			implements Connector.Fixable {
			private final Listeners<StateChange> listeners = Listeners.of();
			private final Functions.Consumer<StateChange> listener = this::stateChange;

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
}
