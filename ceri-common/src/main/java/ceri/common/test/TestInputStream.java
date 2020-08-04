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
import ceri.common.text.StringUtil;
import ceri.common.text.Utf8Util;

/**
 * An InputStream controlled by supplier functions for read() and available(), based on read byte
 * count, and on runnable function for close(). Returned int values for read() and available() are
 * interpreted as actions if < 0. EOF returns EOF, BRK is used to mark an available() endpoint, EOFX
 * throws an EOFException, IOX throws an IOException, and RTX throws a RuntimeException.
 * <p/>
 * String data can embed actions with constant strings EOFS, BRKS, EOFXS, IOXS, and RTXS. The
 * strings use Unicode private-use code points from U+f000.
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
	private volatile ExceptionIntUnaryOperator<IOException> readSupplier = i -> 0;
	private volatile ExceptionIntUnaryOperator<IOException> availableSupplier = i -> AVAILABLE_DEF;
	private volatile ExceptionRunnable<IOException> closeRunnable = () -> {};
	private volatile int count = 0;
	private volatile int mark = 0;

	/**
	 * Provide byte data. EOF is returned once all data is read. Resets current count to 0.
	 */
	public void data(byte[] values) {
		data(values, 0);
	}

	/**
	 * Provide byte data. EOF is returned once all data is read. Resets current count to 0.
	 */
	public void data(byte[] values, int offset) {
		data(values, offset, values.length - offset);
	}

	/**
	 * Provide byte data. EOF is returned once all data is read. Resets current count to 0.
	 */
	public void data(byte[] values, int offset, int length) {
		actions(ByteUtil.ustream(values, offset, length).toArray());
	}

	/**
	 * Set read() and available() functions based on given list of actions. Resets current count to
	 * 0.
	 */
	public void actions(int... actions) {
		for (int action : actions)
			verify(action);
		read(i -> action(i, actions));
		available(i -> TestInputStream.available(i, actions));
	}

	/**
	 * Set read() and available() functions based on ascii chars and encoded actions. Resets current
	 * count to 0.
	 */
	public void actions(String format, Object... args) {
		actions(encodeWithActions(StringUtil.format(format, args)));
	}

	/**
	 * Delegate read() and available() functions to the given input stream.
	 */
	public void in(InputStream in) {
		read(i -> in.read());
		available(i -> in.available());
	}

	/**
	 * Set stream read() function. Returns an int, given the current read byte count. Int value is
	 * processed as an action if < 0. Resets current count to 0.
	 */
	public void read(ExceptionIntUnaryOperator<IOException> readSupplier) {
		this.readSupplier = readSupplier;
		resetState();
	}

	/**
	 * Set stream available() function. Returns an int, given the current read byte count. Int value
	 * is processed as an action if < 0.
	 */
	public void available(ExceptionIntUnaryOperator<IOException> availableSupplier) {
		this.availableSupplier = availableSupplier;
	}

	/**
	 * Set close() function.
	 */
	public void close(ExceptionRunnable<IOException> closeRunnable) {
		this.closeRunnable = closeRunnable;
	}

	@Override
	public void mark(int n) {
		mark = count;
	}

	@Override
	public void reset() {
		count = mark;
	}

	public void resetState() {
		mark = 0;
		count = 0;
	}

	public int count() {
		return count;
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
