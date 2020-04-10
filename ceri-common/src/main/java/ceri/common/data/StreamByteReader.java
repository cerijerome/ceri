package ceri.common.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.ExceptionAdapter;

/**
 * {@link ByteReader} wrapper for a {@link java.io.InputStream}. This provides sequential reading of
 * bytes. The type T allows typed access to the InputStream methods.
 */
@Deprecated
public class StreamByteReader<T extends InputStream> implements ByteReader {
	private static final ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;
	private static final int READ_LIMIT_DEF = 8 * 1024;
	private final T in;

	public static <T extends InputStream> StreamByteReader<T> of(T in) {
		return new StreamByteReader<>(in);
	}

	private StreamByteReader(T in) {
		this.in = in;
	}

	/* ByteReader overrides */

	@Override
	public byte readByte() {
		int b = ioAdapter.getInt(in::read);
		if (b < 0) throw new RuntimeIoException("End of stream");
		return (byte) b;
	}

	@Override
	public StreamByteReader<T> skip(int length) {
		ioAdapter.run(() -> in.skipNBytes(length));
		return this;
	}

	@Override
	public byte[] readBytes(int length) {
		byte[] bytes = ioAdapter.get(() -> in.readNBytes(length));
		verifyLength(bytes.length, length);
		return bytes;
	}

	@Override
	public int readInto(byte[] dest, int offset, int length) {
		int n = ioAdapter.getInt(() -> in.readNBytes(dest, offset, length));
		verifyLength(n, length);
		return offset + n;
	}

	@Override
	public int readInto(ByteReceiver receiver, int offset, int length) {
		int i = ioAdapter.getInt(() -> receiver.readFrom(offset, in, length));
		verifyLength(i - offset, length);
		return i;
	}

	@Override
	public int transferTo(OutputStream out, int length) throws IOException {
		return ByteReader.transferBufferTo(this, out, length);
	}

	/* InputStream methods */

	/**
	 * Typed access to the InputStream.
	 */
	public T in() {
		return in;
	}

	/**
	 * Returns the number of bytes available for reading without blocking. If not supported by the
	 * InputStream, it returns 0. Wraps any IOExceptions with unchecked RuntimeIoException.
	 */
	public int available() {
		return ioAdapter.getInt(in::available);
	}

	/**
	 * Returns true if the InputStream supports mark/reset.
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/**
	 * Marks the current position, with a default maximum read limit. A call to reset() will reset
	 * the stream position if supported.
	 */
	public StreamByteReader<T> mark() {
		return mark(READ_LIMIT_DEF);
	}

	/**
	 * Marks the current position, with the given read limit. A call to reset() will reset the
	 * stream position if supported.
	 */
	public StreamByteReader<T> mark(int readlimit) {
		in.mark(readlimit);
		return this;
	}

	/**
	 * Moves the stream to the mark() position, if supported by the InputStream. Wraps any
	 * IOExceptions with unchecked RuntimeIoException.
	 */
	public StreamByteReader<T> reset() {
		ioAdapter.run(in::reset);
		return this;
	}

	private void verifyLength(int actual, int expected) {
		if (actual < expected) throw new RuntimeIoException(
			String.format("End of stream, not enough bytes: %d/%d", actual, expected));
	}

}
