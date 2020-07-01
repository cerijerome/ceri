package ceri.common.test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionRunnable;

/**
 * InputStream controlled by supplier functions for read(), available(), and close(). Integer data
 * can also be supplied to read() and available(). Use a value of -1 to throw EOFException, -2 to
 * throw an IOException, -3 to throw a RuntimeException, otherwise (byte & 0xff). Buffered reads
 * will often squash an IOException when it is not the first byte read. To be sure of a thrown
 * exception, use -2, -2.
 */
public class TestInputStream extends InputStream {
	private static final int AVAILABLE_DEF = 16;
	private static final int EOF_VALUE = -1;
	private static final int THROW_IO_VALUE = -2;
	private static final int THROW_RT_VALUE = -3;
	private final ExceptionIntUnaryOperator<IOException> readSupplier;
	private final ExceptionIntUnaryOperator<IOException> availableSupplier;
	private final ExceptionRunnable<IOException> closeRunnable;
	private int count = 0;

	public static TestInputStream of(int... actions) {
		return builder().actions(actions).build();
	}

	public static TestInputStream of(byte[] bytes) {
		return builder().data(bytes).build();
	}

	public static class Builder {
		ExceptionIntUnaryOperator<IOException> readSupplier = i -> 0;
		ExceptionIntUnaryOperator<IOException> availableSupplier = i -> AVAILABLE_DEF;
		ExceptionRunnable<IOException> closeRunnable = () -> {};

		Builder() {}

		public Builder actions(int... actions) {
			return read(i -> action(i, actions)).available(i -> Math.max(0, actions.length - i));
		}

		public Builder data(byte[] values) {
			return actions(ByteUtil.ustream(values).toArray());
		}

		public Builder read(ExceptionIntUnaryOperator<IOException> readSupplier) {
			this.readSupplier = readSupplier;
			return this;
		}

		public Builder available(ExceptionIntUnaryOperator<IOException> availableSupplier) {
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

	public int count() {
		return count;
	}

	@Override
	public void reset() {
		count = 0;
	}

	@Override
	public int read() throws IOException {
		int value = readSupplier.applyAsInt(count++);
		if (value < 0) count--;
		return value;
	}

	@Override
	public int available() throws IOException {
		return availableSupplier.applyAsInt(count);
	}

	@Override
	public void close() throws IOException {
		closeRunnable.run();
	}

	private static int action(int i, int[] data) throws IOException {
		if (i >= data.length) return EOF_VALUE;
		int value = data[i];
		if (value == THROW_IO_VALUE) throw new IOException("Read = " + THROW_IO_VALUE);
		if (value == THROW_RT_VALUE) throw new RuntimeException("Read = " + THROW_RT_VALUE);
		if (value == EOF_VALUE) throw new EOFException("Read = " + EOF_VALUE);
		return value;
	}

}
