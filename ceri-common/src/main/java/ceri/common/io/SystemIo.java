package ceri.common.io;

import java.io.InputStream;
import java.io.PrintStream;
import ceri.common.function.RuntimeCloseable;

/**
 * Helper when overriding System i/o streams. Restores original streams on close.
 */
public class SystemIo implements RuntimeCloseable {
	private final InputStream in;
	private final PrintStream out;
	private final PrintStream err;

	public static SystemIo of() {
		return new SystemIo();
	}

	private SystemIo() {
		in = System.in;
		out = System.out;
		err = System.err;
	}

	public void in(InputStream in) {
		System.setIn(in);
	}

	public InputStream in() {
		return System.in;
	}

	public void out(PrintStream out) {
		System.setOut(out);
	}

	public PrintStream out() {
		return System.out;
	}

	public void err(PrintStream err) {
		System.setErr(err);
	}

	public PrintStream err() {
		return System.err;
	}

	@SuppressWarnings("resource")
	@Override
	public void close() {
		if (in() != in) System.setIn(in);
		if (out() != out) System.setOut(out);
		if (err() != err) System.setErr(err);
	}

}
