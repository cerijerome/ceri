package ceri.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;

/**
 * A filter output stream that can be reset with a new output stream. Useful for mending broken
 * streams.
 */
public class ReplaceableOutputStream extends OutputStream implements Listenable<IOException> {
	private final Listeners<IOException> listeners = new Listeners<>();
	private volatile OutputStream out;

	@Override
	public boolean listen(Consumer<? super IOException> listener) {
		return listeners.listen(listener);
	}

	@Override
	public boolean unlisten(Consumer<? super IOException> listener) {
		return listeners.unlisten(listener);
	}

	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		try {
			checkState();
			out.write(b);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		try {
			checkState();
			out.write(b);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			checkState();
			out.write(b, off, len);
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			checkState();
			out.flush();
		} catch (IOException e) {
			listeners.accept(e);
			throw e;
		}
	}

	@Override
	public void close() throws IOException {
		checkState();
		out.close();
	}

	private void checkState() throws IOException {
		if (out == null) throw new StreamNotSetException("OutputStream is not set");
	}

}
