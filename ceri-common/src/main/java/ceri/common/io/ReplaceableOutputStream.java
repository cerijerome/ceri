package ceri.common.io;

import java.io.IOException;
import java.io.OutputStream;
import ceri.common.event.Listenable;

/**
 * A filter output stream that can be reset with a new output stream. Useful for mending broken
 * streams.
 */
public class ReplaceableOutputStream extends OutputStream {
	private final Replaceable.Field<OutputStream> out = Replaceable.field("out");

	/**
	 * Listen for errors on invoked calls.
	 */
	public Listenable<Exception> errors() {
		return out.errors();
	}

	/**
	 * Close the current delegate, and set the new delegate. Does nothing if no change in delegate.
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
