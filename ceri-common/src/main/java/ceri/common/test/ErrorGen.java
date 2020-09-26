package ceri.common.test;

import java.io.IOException;
import java.util.function.Function;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Utility for generating common errors during tests.
 */
public class ErrorGen {
	private static final String MESSAGE = "generated";
	private volatile Mode mode = Mode.none;

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
		this.mode = mode;
		return this;
	}

	public void generateRt() {
		if (mode == Mode.none) return;
		if (mode == Mode.rtInterrupted) throw new RuntimeInterruptedException(MESSAGE);
		throw new RuntimeException(MESSAGE); // catch-all
	}

	public void generateWithInterrupt() throws InterruptedException {
		if (mode == Mode.interrupted) throw new InterruptedException(MESSAGE);
		generateRt();
	}

	public void generateIo() throws IOException {
		if (mode == Mode.checked) throw new IOException(MESSAGE);
		generateRt();
	}

	public void generateIoWithInterrupt() throws IOException, InterruptedException {
		if (mode == Mode.checked) throw new IOException(MESSAGE);
		generateWithInterrupt();
	}

	public <E extends Exception> void generate(Function<String, E> errorFn) throws E {
		if (mode == Mode.checked) throw errorFn.apply(MESSAGE);
		generateRt();
	}

	public <E extends Exception> void generateWithInterrupt(Function<String, E> errorFn)
		throws E, InterruptedException {
		if (mode == Mode.checked) throw errorFn.apply(MESSAGE);
		generateWithInterrupt();
	}

}
