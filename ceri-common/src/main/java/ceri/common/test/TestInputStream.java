package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionRunnable;

/**
 * InputStream controlled by supplier functions for read(), available(), and close(). Integer data
 * can also be supplied to read() and available(). Use a value of -1 for an early EOF, -2 to throw
 * an IOException, -3 to throw a RuntimeException, otherwise (byte & 0xff). Buffered reads will
 * often squash an IOException when it is not the first byte read. To be sure of a thrown exception,
 * use -2, -2.
 */
public class TestInputStream extends InputStream {
	private static final int AVAILABLE_DEF = 16;
	private static final int EOF_VALUE = -1;
	private static final int THROW_IO_VALUE = -2;
	private static final int THROW_RT_VALUE = -3;
	private final ExceptionIntSupplier<IOException> readSupplier;
	private final ExceptionIntSupplier<IOException> availableSupplier;
	private final ExceptionRunnable<IOException> closeRunnable;

	private static class Data {
		private int pos = 0;
		private final int[] data;

		public Data(int... data) {
			this.data = data;
		}

		public int read() throws IOException {
			if (pos >= data.length) return EOF_VALUE;
			int value = data[pos++];
			if (value == THROW_IO_VALUE) throw new IOException("Read = " + THROW_IO_VALUE);
			if (value == THROW_RT_VALUE) throw new RuntimeException("Read = " + THROW_RT_VALUE);
			if (value == EOF_VALUE) pos = data.length;
			return value;
		}

		public int available() {
			return data.length - pos;
		}
	}

	public static TestInputStream of(int... bytes) {
		return builder().data(bytes).build();
	}

	public static TestInputStream of(byte[] bytes) {
		return builder().data(bytes).build();
	}

	public static class Builder {
		ExceptionIntSupplier<IOException> readSupplier = () -> 0;
		ExceptionIntSupplier<IOException> availableSupplier = () -> AVAILABLE_DEF;
		ExceptionRunnable<IOException> closeRunnable = () -> {};

		Builder() {}

		public Builder data(int... values) {
			Data data = new Data(values);
			return read(data::read).available(data::available);
		}

		public Builder data(byte[] values) {
			return data(ByteUtil.streamOf(values).toArray());
		}

		public Builder read(ExceptionIntSupplier<IOException> readSupplier) {
			this.readSupplier = readSupplier;
			return this;
		}

		public Builder available(ExceptionIntSupplier<IOException> availableSupplier) {
			this.availableSupplier = availableSupplier;
			return this;
		}

		public Builder close(ExceptionRunnable<IOException> closeRunnable) {
			this.closeRunnable = closeRunnable;
			return this;
		}

		public TestInputStream build() {
			return new TestInputStream(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	TestInputStream(Builder builder) {
		readSupplier = builder.readSupplier;
		availableSupplier = builder.availableSupplier;
		closeRunnable = builder.closeRunnable;
	}

	@Override
	public int read() throws IOException {
		return readSupplier.getAsInt();
	}

	@Override
	public int available() throws IOException {
		return availableSupplier.getAsInt();
	}

	@Override
	public void close() throws IOException {
		closeRunnable.run();
	}

}
