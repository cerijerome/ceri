package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;

/**
 * A filter input stream that can be reset with a new input stream. Useful for mending broken
 * streams.
 */
public class ReplaceableInputStream extends InputStream implements Listenable<IOException> {
	private final Listeners<IOException> listeners = new Listeners<>();
	private volatile InputStream in = null;

	@Override
	public boolean listen(Consumer<? super IOException> listener) {
		return listeners.listen(listener);
	}

	@Override
	public boolean unlisten(Consumer<? super IOException> listener) {
		return listeners.unlisten(listener);
	}

	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		try {
			checkState();
			return in.read();
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		try {
			checkState();
			return in.read(b);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			checkState();
			return in.read(b, off, len);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public long skip(long n) throws IOException {
		try {
			checkState();
			return in.skip(n);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public int available() throws IOException {
		try {
			checkState();
			return in.available();
		} catch (IOException e) {
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
		if (in == null) return;
		in.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		try {
			checkState();
			in.reset();
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public boolean markSupported() {
		if (in == null) return false;
		return in.markSupported();
	}

	private void checkState() throws IOException {
		if (in == null) throw new StreamNotSetException("InputStream is not set");
	}

}
