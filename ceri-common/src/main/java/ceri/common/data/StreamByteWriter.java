package ceri.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionAdapter;

/**
 * {@link ByteWriter} wrapper for a {@link java.io.OutputStream}. This provides sequential writing
 * of bytes. The type T allows typed access to the OutputStream methods, such as
 * ByteArrayOutputStream.toByteArray().
 */
@Deprecated
public class StreamByteWriter<T extends StreamByteWriter<T>> implements ByteWriter<T> {
	private static final ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;
	private final OutputStream out;

	private StreamByteWriter(OutputStream out) {
		this.out = out;
	}

	public StreamByteWriter<T> apply(Consumer<? super StreamByteWriter<T>> consumer) {
		consumer.accept(this);
		return this;
	}
	
	public <U> U map(Function<? super StreamByteWriter<T>, U> fn) {
		return fn.apply(this);
	}
	
	/* ByteReader overrides */

	@Override
	public T writeByte(int value) {
		return run(() -> out.write(value));
	}

	@Override
	public T fill(int length, int value) {
		return writeFrom(ByteUtil.fill(length, value));
	}

	@Override
	public T writeFrom(byte[] array, int offset, int length) {
		return run(() -> out.write(array, offset, length));
	}

	@Override
	public T writeFrom(ByteProvider provider, int offset, int length) {
		return run(() -> provider.writeTo(offset, out, length));
	}

	@Override
	public int transferFrom(InputStream in, int length) throws IOException {
		return ByteWriter.transferBufferFrom(this, in, length);
	}

	/* OutputStream methods */
	
	/**
	 * Access to the output stream.
	 */
	public OutputStream out() {
		return out;
	}

	/**
	 * Flushes the stream if supported by the wrapped stream, otherwise no-op.
	 */
	public StreamByteWriter<T> flush() {
		return run(out::flush);
	}
	
	private T run(ExceptionRunnable<IOException> runnable) {
		ioAdapter.run(runnable);
		return typedThis();
	}

	private T typedThis() {
		return BasicUtil.uncheckedCast(this);
	}
	
}
