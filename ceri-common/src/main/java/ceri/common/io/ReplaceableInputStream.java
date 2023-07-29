package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import ceri.common.event.Listenable;

/**
 * A filter input stream that can be reset with a new input stream. Useful for mending broken
 * streams.
 */
public class ReplaceableInputStream extends InputStream {
	private final Replaceable.Field<InputStream> in = Replaceable.field("in");

	/**
	 * Listen for errors on invoked calls.
	 */
	public Listenable<Exception> errors() {
		return in.errors();
	}

	/**
	 * Close the current delegate, and set the new delegate. Does nothing if no change in delegate.
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
