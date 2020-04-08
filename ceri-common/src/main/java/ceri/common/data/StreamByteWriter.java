package ceri.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.ExceptionAdapter;

/**
 * {@link ByteWriter} wrapper for a {@link java.io.OutputStream}. This provides sequential writing
 * of bytes. The type T allows typed access to the OutputStream methods, such as
 * ByteArrayOutputStream.toByteArray().
 */
public class StreamByteWriter<T extends OutputStream> implements ByteWriter<StreamByteWriter<T>> {
	private static final ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;
	private final T out;

	/**
	 * Returns a byte array copy of the ByteArrayOutputStream current state. 
	 */
	public static byte[] bytes(StreamByteWriter<ByteArrayOutputStream> writer) {
		return writer.out().toByteArray();
	}
	
	/**
	 * Creates an immutable byte array copy of the ByteArrayOutputStream current state. 
	 */
	public static Immutable immutable(StreamByteWriter<ByteArrayOutputStream> writer) {
		return Immutable.wrap(bytes(writer));
	}
	
	/**
	 * Creates a mutable byte array copy of the ByteArrayOutputStream current state. 
	 */
	public static Mutable mutable(StreamByteWriter<ByteArrayOutputStream> writer) {
		return Mutable.wrap(bytes(writer));
	}
	
	public static StreamByteWriter<ByteArrayOutputStream> of() {
		return of(new ByteArrayOutputStream());
	}

	public static <T extends OutputStream> StreamByteWriter<T> of(T out) {
		return new StreamByteWriter<>(out);
	}

	private StreamByteWriter(T out) {
		this.out = out;
	}

	/* ByteReader overrides */

	@Override
	public StreamByteWriter<T> writeByte(int value) {
		return run(() -> out.write(value));
	}

	@Override
	public StreamByteWriter<T> fill(int length, int value) {
		return writeFrom(ByteUtil.fill(length, value));
	}

	@Override
	public StreamByteWriter<T> writeFrom(byte[] array, int offset, int length) {
		return run(() -> out.write(array, offset, length));
	}

	@Override
	public StreamByteWriter<T> writeFrom(ByteProvider provider, int offset, int length) {
		return run(() -> provider.writeTo(offset, out, length));
	}

	@Override
	public int transferFrom(InputStream in, int length) throws IOException {
		return ByteWriter.transferBufferFrom(this, in, length);
	}

	/* OutputStream methods */
	
	/**
	 * Typed access to the output stream.
	 */
	public T out() {
		return out;
	}

	/**
	 * Flushes the stream if supported by the wrapped stream, otherwise no-op.
	 */
	public StreamByteWriter<T> flush() {
		return run(out::flush);
	}
	
	private StreamByteWriter<T> run(ExceptionRunnable<IOException> runnable) {
		ioAdapter.run(runnable);
		return this;
	}

}
