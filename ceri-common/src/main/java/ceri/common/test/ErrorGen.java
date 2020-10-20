package ceri.common.test;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.Counter;

/**
 * Utility for generating common errors during tests.
 */
public class ErrorGen {
	private static final String MESSAGE = "generated";
	private volatile Supplier<Mode> modeSupplier;

	public static enum Mode {
		none,
		rt,
		rtInterrupted,
		interrupted, // only for generateXxxWithInterrupt
		checked;
	}

	public static ErrorGen of() {
		return new ErrorGen();
	}

	private ErrorGen() {}

	public ErrorGen reset() {
		return mode(Mode.none);
	}

	public ErrorGen mode(Mode mode) {
		return mode(() -> mode);
	}

	public ErrorGen mode(Supplier<Mode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		return this;
	}

	/**
	 * Sets the error generator mode, based on call count.
	 */
	public ErrorGen modeForIndex(Mode mode, int... indexes) {
		List<Integer> list = ArrayUtil.intList(indexes);
		Counter counter = Counter.of();
		return mode(() -> list.contains(counter.intInc() - 1) ? mode : Mode.none);
	}

	public <T> T generate() {
		Mode mode = mode();
		generateRt(mode);
		return null;
	}

	public <T> T generateWithInterrupt() throws InterruptedException {
		Mode mode = mode();
		generateInterrupted(mode);
		generateRt(mode);
		return null;
	}

	public <T> T generateIo() throws IOException {
		Mode mode = mode();
		generateIo(mode);
		generateRt(mode);
		return null;
	}

	public <T> T generateIoWithInterrupt() throws IOException, InterruptedException {
		Mode mode = mode();
		generateIo(mode);
		generateInterrupted(mode);
		generateRt(mode);
		return null;
	}

	public <E extends Exception, T> T generate(Function<String, E> errorFn) throws E {
		Mode mode = mode();
		generate(mode, errorFn);
		generateRt(mode);
		return null;
	}

	public <E extends Exception, T> T generateWithInterrupt(Function<String, E> errorFn)
		throws E, InterruptedException {
		Mode mode = mode();
		generate(mode, errorFn);
		generateInterrupted(mode);
		generateRt(mode);
		return null;
	}

	private static <E extends Exception> void generate(Mode mode, Function<String, E> errorFn)
		throws E {
		if (mode == Mode.checked) throw errorFn.apply(MESSAGE);
	}

	private static void generateIo(Mode mode) throws IOException {
		if (mode == Mode.checked) throw new IOException(MESSAGE);
	}

	private static void generateInterrupted(Mode mode) throws InterruptedException {
		if (mode == Mode.interrupted) throw new InterruptedException(MESSAGE);
	}

	private static void generateRt(Mode mode) {
		if (mode == Mode.none) return;
		if (mode == Mode.rtInterrupted) throw new RuntimeInterruptedException(MESSAGE);
		throw new RuntimeException(MESSAGE); // catch-all
	}

	private Mode mode() {
		Supplier<Mode> modeSupplier = this.modeSupplier;
		if (modeSupplier == null) return Mode.none;
		Mode mode = modeSupplier.get();
		return mode == null ? Mode.none : mode;
	}
}
