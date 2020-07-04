package ceri.common.test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionIntBinaryOperator;
import ceri.common.function.ExceptionRunnable;

/**
 * A test output stream that collects written bytes, and allows a callback on each write, and on
 * stream closure. Standard write callbacks include providing a write limit, and throwing exceptions
 * based on current byte count.
 */
public class TestOutputStream extends OutputStream {
	public static final int EOFX = TestInputStream.EOFX;
	public static final int IOX = TestInputStream.IOX;
	public static final int RTX = TestInputStream.RTX;
	private static final Set<Integer> allowedActions = Set.of(EOFX, IOX, RTX);
	private final ExceptionIntBinaryOperator<IOException> writeConsumer;
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
		ExceptionIntBinaryOperator<IOException> writeConsumer = (i, b) -> 0;
		ExceptionRunnable<IOException> closer = () -> {};
		boolean collect = true;

		Builder() {}

		/**
		 * Throws an EOFException after size bytes have been written.
		 */
		public Builder limit(int size) {
			return write((i, b) -> TestOutputStream.limit(i, size));
		}

		/**
		 * Take action based on written byte count. An action value of -1 throws an EOFException, -2
		 * throws an IOException, -3 throws a RuntimeException. Any other value does nothing. When
		 * the action count is exceeded, an EOFException is thrown.
		 */
		public Builder actions(int... actions) {
			for (int action : actions)
				verify(action);
			return writeAction((i, b) -> TestOutputStream.action(i, actions));
		}

		/**
		 * Register a callback for written byte values.
		 */
		public Builder writeAction(ExceptionIntBinaryOperator<IOException> writeConsumer) {
			this.writeConsumer = writeConsumer;
			return this;
		}

		/**
		 * Register a callback for written byte values.
		 */
		public Builder write(ExceptionBiConsumer<IOException, Integer, Integer> writeConsumer) {
			return writeAction((i, b) -> {
				writeConsumer.accept(i, b);
				return 0;
			});
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
		int action = writeConsumer.applyAsInt(count++, b);
		throwAction(action);
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

	private static void verify(int action) {
		if (action >= 0 || allowedActions.contains(action)) return;
		throw new AssertionError("Invalid action: " + action);
	}

	private static int limit(int i, int limit) throws IOException {
		if (i < limit) return 0;
		throw new EOFException("Exceeded limit: " + i + " >= " + limit);
	}

	private static int action(int i, int[] actions) throws IOException {
		if (i < actions.length) return actions[i];
		throw new EOFException("Exceeded limit: " + i + " >= " + actions.length);
	}

	private static int throwAction(int action) throws IOException {
		if (action == EOFX) throw new EOFException("Action = " + EOFX);
		if (action == IOX) throw new IOException("Action = " + IOX);
		if (action == RTX) throw new RuntimeException("Action = " + RTX);
		if (action < 0) throw new AssertionError("Invalid action: " + action);
		return action;
	}
}
