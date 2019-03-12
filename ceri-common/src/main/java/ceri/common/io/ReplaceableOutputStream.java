package ceri.common.io;

import java.io.IOException;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;

/**
 * A filter output stream that can be reset with a new output stream. Useful for mending broken
 * streams.
 */
public class ReplaceableOutputStream extends OutputStream
	implements Listenable.Indirect<Exception> {
	private final Listeners<Exception> listeners = new Listeners<>();
	private volatile OutputStream out;

	@Override
	public Listenable<Exception> listeners() {
		return listeners;
	}

	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		try {
			checkState();
			out.write(b);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		try {
			checkState();
			out.write(b);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			checkState();
			out.write(b, off, len);
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			checkState();
			out.flush();
		} catch (RuntimeException | IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void close() throws IOException {
		if (out != null) out.close();
	}

	private void checkState() throws IOException {
		if (out == null) throw new StreamNotSetException("out");
	}

}
