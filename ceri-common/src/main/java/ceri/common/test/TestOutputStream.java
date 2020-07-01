package ceri.common.test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionRunnable;

/**
 * A test output stream that collects written bytes, and allows a callback on each write, and on
 * stream closure. Standard write callbacks include providing a write limit, and throwing exceptions
 * based on current byte count.
 */
public class TestOutputStream extends OutputStream {
	private static final int EOF_VALUE = -1;
	private static final int THROW_IO_VALUE = -2;
	private static final int THROW_RT_VALUE = -3;
	private final ExceptionBiConsumer<IOException, Integer, Integer> writeConsumer;
	private final ExceptionRunnable<IOException> closer;
	private final ByteArrayOutputStream collector;
	private int count = 0;

	/**
	 * A test output stream that collects bytes.
	 */
	public static TestOutputStream of() {
		return builder().build();
	}

	/**
	 * A test output stream that collects bytes up to the limit.
	 */
	public static TestOutputStream limit(int limit) {
		return builder().limit(limit).build();
	}

	/**
	 * A test output stream that collects bytes and takes action based on byte count. An action
	 * value of -1 throws an EOFException, -2 throws an IOException, -3 throws a RuntimeException.
	 * Any other value does nothing. When the action count is exceeded, an EOFException is thrown.
	 */
	public static TestOutputStream actions(int... actions) {
		return builder().actions(actions).build();
	}

	public static class Builder {
		ExceptionBiConsumer<IOException, Integer, Integer> writeConsumer = (i, b) -> {};
		ExceptionRunnable<IOException> closer = () -> {};
		boolean collect = true;

		Builder() {}

		/**
		 * Throws an EOFException after size bytes have been written.
		 */
		public Builder limit(int size) {
			return write((i, b) -> verifyLimit(i, size));
		}

		/**
		 * Take action based on written byte count. An action value of -1 throws an EOFException, -2
		 * throws an IOException, -3 throws a RuntimeException. Any other value does nothing. When
		 * the action count is exceeded, an EOFException is thrown.
		 */
		public Builder actions(int... actions) {
			return write((i, b) -> action(i, actions));
		}

		/**
		 * Register a callback for written byte values.
		 */
		public Builder write(ExceptionIntConsumer<IOException> writeConsumer) {
			return write((i, b) -> writeConsumer.accept(b));
		}

		/**
		 * Register a callback for written byte count and values.
		 */
		public Builder write(ExceptionBiConsumer<IOException, Integer, Integer> writeConsumer) {
			this.writeConsumer = writeConsumer;
			return this;
		}

		/**
		 * Enable or disable collection of written bytes.
		 */
		public Builder collect(boolean collect) {
			this.collect = collect;
			return this;
		}

		/**
		 * Register a callback for stream closure.
		 */
		public Builder close(ExceptionRunnable<IOException> closer) {
			this.closer = closer;
			return this;
		}

		public TestOutputStream build() {
			return new TestOutputStream(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	TestOutputStream(Builder builder) {
		writeConsumer = builder.writeConsumer;
		closer = builder.closer;
		collector = builder.collect ? new ByteArrayOutputStream() : null;
	}

	@Override
	public void write(int b) throws IOException {
		writeConsumer.accept(count++, b);
		if (collector != null) collector.write(b);
	}

	public byte[] written() {
		if (collector == null) return ArrayUtil.EMPTY_BYTE;
		return collector.toByteArray();
	}

	/**
	 * Resets the byte count and byte collector.
	 */
	public void reset() {
		count = 0;
		if (collector != null) collector.reset();
	}

	@Override
	public void close() throws IOException {
		closer.run();
	}

	private static void verifyLimit(int i, int limit) throws IOException {
		if (i < limit) return;
		throw new EOFException("Exceeded limit: " + i);
	}

	private static void action(int i, int[] actions) throws IOException {
		if (i >= actions.length) throw new EOFException("End of actions");
		int value = actions[i];
		if (value == THROW_IO_VALUE) throw new IOException("Write = " + THROW_IO_VALUE);
		if (value == THROW_RT_VALUE) throw new RuntimeException("Write = " + THROW_RT_VALUE);
		if (value == EOF_VALUE) throw new EOFException("Write = " + EOF_VALUE);
	}

}
