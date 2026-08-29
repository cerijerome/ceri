package ceri.common.io;

import java.io.InputStream;
import java.io.PrintStream;
import ceri.common.function.Functions;

/**
 * Helper when overriding System i/o streams. Restores original streams on close.
 */
public class SystemIo implements Functions.Closeable {
	/** The original stream. */
	public final InputStream in;
	/** The original stream. */
	public final PrintStream out;
	/** The original stream. */
	public final PrintStream err;

	/**
	 * Returns a new instance.
	 */
	public static SystemIo of() {
		return new SystemIo();
	}

	private SystemIo() {
		in = System.in;
		out = System.out;
		err = System.err;
	}

	/**
	 * Overrides stdin.
	 */
	public void in(InputStream in) {
		System.setIn(in);
	}

	/**
	 * Returns the current stdin.
	 */
	public InputStream in() {
		return System.in;
	}

	/**
	 * Overrides stdout.
	 */
	public void out(PrintStream out) {
		System.setOut(out);
	}

	/**
	 * Returns the current stdout.
	 */
	public PrintStream out() {
		return System.out;
	}

	/**
	 * Overrides stderr.
	 */
	public void err(PrintStream err) {
		System.setErr(err);
	}

	/**
	 * Returns the current stderr.
	 */
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
