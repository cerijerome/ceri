package ceri.common.test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionRunnable;

/**
 * InputStream controlled by supplier functions for read(), available(), and close(). Integer data
 * can also be supplied to read() and available(). Use a value of -1 to return EOF, -2 to throw an
 * IOException, -3 to throw a RuntimeException, -4 to throw EOFException, otherwise (byte & 0xff).
 * Buffered reads will often squash an IOException when it is not the first byte read. To be sure of
 * a thrown exception, use -2, -2.
 */
public class TestInputStream extends InputStream {
	private static final int AVAILABLE_DEF = 16;
	// Action values
	public static final int EOF = -1; // early EOF
	public static final int BRK = -2; // mark available() endpoints
	public static final int EOFX = -101; // throw EOFException
	public static final int IOX = -102; // throw IOException
	public static final int RTX = -103; // throw RuntimeException
	private static final Set<Integer> allowedActions = Set.of(EOF, BRK, EOFX, IOX, RTX);
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

	public static TestInputStream of(String s) {
		return builder().data(s).build();
	}

	public static class Builder {
		ExceptionIntUnaryOperator<IOException> readSupplier = i -> 0;
		ExceptionIntUnaryOperator<IOException> availableSupplier = i -> AVAILABLE_DEF;
		ExceptionRunnable<IOException> closeRunnable = () -> {};

		Builder() {}

		public Builder actions(int... actions) {
			for (int action : actions)
				verify(action);
			return read(i -> TestInputStream.action(i, actions))
				.available(i -> TestInputStream.available(i, actions));
		}

		public Builder data(String s) {
			return data(s.getBytes(StandardCharsets.UTF_8));
		}
		
		public Builder data(byte[] values) {
			return data(values, 0);
		}
		
		public Builder data(byte[] values, int offset) {
			return data(values, offset, values.length - offset);
		}
		
		public Builder data(byte[] values, int offset, int length) {
			return actions(ByteUtil.ustream(values, offset, length).toArray());
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
		while (true) {
			int value = throwAction(readSupplier.applyAsInt(count++));
			if (value == EOF) count--;
			if (value != BRK) return value;
		}
	}

	@Override
	public int available() throws IOException {
		return throwAction(availableSupplier.applyAsInt(count));
	}

	@Override
	public void close() throws IOException {
		closeRunnable.run();
	}

	private static void verify(int action) {
		if (action >= 0 || allowedActions.contains(action)) return;
		throw new AssertionError("Invalid action: " + action);
	}

	private static int action(int i, int[] actions) throws IOException {
		return i >= actions.length ? EOF : throwAction(actions[i]);
	}

	private static int throwAction(int action) throws IOException {
		if (action == EOFX) throw new EOFException("Action = " + EOFX);
		if (action == IOX) throw new IOException("Action = " + IOX);
		if (action == RTX) throw new RuntimeException("Action = " + RTX);
		if (action < BRK) throw new AssertionError("Invalid action: " + action);
		return action;
	}

	private static int available(int i, int[] actions) {
		if (i < actions.length && actions[i] == BRK) i++; // skip if currently on break
		for (int count = 0;; i++, count++)
			if (i >= actions.length || actions[i] == BRK) return count;
	}

}
