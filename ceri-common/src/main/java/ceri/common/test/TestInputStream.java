package ceri.common.test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import ceri.common.data.ByteUtil;
import ceri.common.data.IntArray.Encoder;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionRunnable;
import ceri.common.math.MathUtil;
import ceri.common.text.Utf8Util;

/**
 * InputStream controlled by supplier functions for read(), available(), and close(). Integer data
 * can also be supplied to read() and available(). Use a value of -1 to return EOF, -2 to throw an
 * IOException, -3 to throw a RuntimeException, -4 to throw EOFException, otherwise (byte & 0xff).
 * Buffered reads will often squash an IOException when it is not the first byte read. To be sure of
 * a thrown exception, use -2, -2.
 * <p/>
 * String data can embed actions with constant strings EOFS, BRKS, EOFXS, IOXS, and RTXS. The
 * strings use Unicode private use code points from U+f000.
 */
public class TestInputStream extends InputStream {
	private static final int AVAILABLE_DEF = 16;
	private static final int CODE_POINT_BASE = 0xf000;
	/** Action for early EOF */
	public static final int EOF = -1;
	public static final String EOFS = str(EOF);
	/** Action to mark available() endpoint */
	public static final int BRK = -2;
	public static final String BRKS = str(BRK);
	/** Action to throw an EOFException */
	public static final int EOFX = -101;
	public static final String EOFXS = str(EOFX);
	/** Action to throw an IOException */
	public static final int IOX = -102;
	public static final String IOXS = str(IOX);
	/** Action to throw a RuntimeException */
	public static final int RTX = -103;
	public static final String RTXS = str(RTX);
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
			return actions(encodeWithActions(s));
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

	private static String str(int action) {
		return new String(Character.toChars(CODE_POINT_BASE - action));
	}

	/**
	 * Encode string as UTF8 unsigned bytes. Actions are codepoints
	 */
	private static int[] encodeWithActions(String s) {
		return Encoder.of().apply(enc -> s.codePoints().forEach(cp -> encodeCodePoint(enc, cp)))
			.ints();
	}

	private static void encodeCodePoint(Encoder enc, int cp) {
		if (encodeAction(enc, cp)) return;
		for (byte b : Utf8Util.encode(cp))
			enc.writeInt(MathUtil.ubyte(b));
	}

	private static boolean encodeAction(Encoder enc, int cp) {
		if (!Character.isBmpCodePoint(cp)) return false;
		int action = CODE_POINT_BASE - cp;
		if (!allowedActions.contains(action)) return false;
		enc.writeInt(action);
		return true;
	}

}
