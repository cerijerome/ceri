package ceri.common.test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionIntBinaryOperator;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionRunnable;

/**
 * An OutputStream controlled by consumer functions for write() and flush(), based on written byte
 * count, and runnable function for close(). Returned int values for write() and flush() are
 * interpreted as actions if < 0. EOFX throws an EOFException, IOX throws an IOException, and RTX
 * throws a RuntimeException.
 */
public class TestOutputStream extends OutputStream {
	public static final int EOFX = TestInputStream.EOFX;
	public static final int IOX = TestInputStream.IOX;
	public static final int RTX = TestInputStream.RTX;
	private static final Set<Integer> allowedActions = Set.of(EOFX, IOX, RTX);
	private final ByteArrayOutputStream collector = new ByteArrayOutputStream();
	private volatile ExceptionIntBinaryOperator<IOException> writeConsumer = (i, b) -> 0;
	private volatile ExceptionIntUnaryOperator<IOException> flusher = i -> 0;
	private volatile ExceptionRunnable<IOException> closer = () -> {};
	private volatile boolean collect = true;
	private volatile int count = 0;

	/**
	 * Throws an EOFException after size bytes have been written.
	 */
	public void limit(int size) {
		write((i, b) -> limit(i, size));
	}

	/**
	 * Take action based on written byte count. An action value of -1 throws an EOFException, -2
	 * throws an IOException, -3 throws a RuntimeException. Any other value does nothing. When the
	 * action count is exceeded, an EOFException is thrown.
	 */
	public void actions(int... actions) {
		for (int action : actions)
			verify(action);
		writeAction((i, b) -> TestOutputStream.action(i, actions));
	}

	/**
	 * Register a callback for written byte values.
	 */
	public void writeAction(ExceptionIntBinaryOperator<IOException> writeConsumer) {
		this.writeConsumer = writeConsumer;
	}

	/**
	 * Register a callback for written byte values.
	 */
	public void write(ExceptionBiConsumer<IOException, Integer, Integer> writeConsumer) {
		writeAction((i, b) -> {
			writeConsumer.accept(i, b);
			return 0;
		});
	}

	/**
	 * Delegates functions to the output stream.
	 */
	public void out(OutputStream out) {
		write((i, b) -> out.write(b));
		flush(i -> out.flush());
		close(out::close);
	}
	
	/**
	 * Enable or disable collection of written bytes.
	 */
	public void collect(boolean collect) {
		this.collect = collect;
	}

	/**
	 * Register a callback for stream flush.
	 */
	public void flushAction(ExceptionIntUnaryOperator<IOException> flusher) {
		this.flusher = flusher;
	}

	/**
	 * Register a callback for stream flush.
	 */
	public void flush(ExceptionIntConsumer<IOException> flusher) {
		flushAction(i -> {
			flusher.accept(i);
			return 0;
		});
	}

	/**
	 * Register a callback for stream closure.
	 */
	public void close(ExceptionRunnable<IOException> closer) {
		this.closer = closer;
	}

	@Override
	public void write(int b) throws IOException {
		int action = writeConsumer.applyAsInt(count++, b);
		throwAction(action);
		if (collect) collector.write(b);
	}

	public byte[] written() {
		return collector.toByteArray();
	}

	public String writtenString() {
		return ByteUtil.fromAscii(written());
	}

	/**
	 * Resets the byte count and byte collector.
	 */
	public void resetState() {
		count = 0;
		collector.reset();
	}

	@Override
	public void flush() throws IOException {
		int action = flusher.applyAsInt(count);
		throwAction(action);
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
