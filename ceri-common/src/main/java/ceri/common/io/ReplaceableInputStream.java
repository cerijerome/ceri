package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;

/**
 * A filter input stream that can be reset with a new input stream. Useful for mending broken
 * streams.
 */
public class ReplaceableInputStream extends InputStream implements Listenable.Indirect<Exception> {
	private final Listeners<Exception> listeners = Listeners.of();
	private volatile InputStream in = null;

	@Override
	public Listenable<Exception> listeners() {
		return listeners;
	}

	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		try {
			checkState();
			return in.read();
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		try {
			checkState();
			return in.read(b);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			checkState();
			return in.read(b, off, len);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public long skip(long n) throws IOException {
		try {
			checkState();
			return in.skip(n);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int available() throws IOException {
		try {
			checkState();
			return in.available();
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void close() throws IOException {
		if (in != null) in.close();
	}

	@Override
	public void mark(int readlimit) {
		try {
			if (in != null) in.mark(readlimit);
		} catch (RuntimeException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void reset() throws IOException {
		try {
			checkState();
			in.reset();
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public boolean markSupported() {
		try {
			return (in != null) && in.markSupported();
		} catch (RuntimeException e) {
			listeners.accept(e);
			throw e;
		}
	}

	private void checkState() throws IOException {
		if (in == null) throw new StreamNotSetException("in");
	}

}
