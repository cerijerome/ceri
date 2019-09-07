package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for bytes from an input stream. IOExceptions are thrown as RuntimeIoExceptions. Not
 * thread safe.
 */
public class InputStreamIterator implements Iterator<Byte> {
	private final InputStream in;
	private Byte nextByte = null;
	private boolean eof = false;

	public InputStreamIterator(InputStream in) {
		this.in = in;
	}

	@Override
	public boolean hasNext() throws RuntimeIoException {
		ensureNextByte();
		return !eof;
	}

	@Override
	public Byte next() throws RuntimeIoException {
		if (!hasNext()) throw new NoSuchElementException();
		Byte b = nextByte;
		nextByte = null;
		return b;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private Byte ensureNextByte() throws RuntimeIoException {
		if (nextByte != null) return nextByte;
		try {
			int value = in.read();
			if (value == -1) eof = true;
			else nextByte = (byte) value;
			return nextByte;
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

}
