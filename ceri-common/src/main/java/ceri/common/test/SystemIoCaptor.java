package ceri.common.test;

import java.io.PrintStream;
import ceri.common.io.PipedStream;
import ceri.common.io.SystemIo;
import ceri.common.text.StringUtil;

/**
 * Utility to write to System.in, and capture System.out, System.err.
 */
public class SystemIoCaptor implements AutoCloseable {
	private static final int SIZE_DEF = 1024;
	private final PipedStream pipe;
	public final PrintStream in;
	public final StringBuilder out = new StringBuilder();
	public final StringBuilder err = new StringBuilder();
	public final SystemIo io;

	public static SystemIoCaptor of() {
		return of(SIZE_DEF);
	}

	public static SystemIoCaptor of(int inBufferSize) {
		return new SystemIoCaptor(inBufferSize);
	}

	@SuppressWarnings("resource")
	private SystemIoCaptor(int inBufferSize) {
		io = SystemIo.of();
		pipe = PipedStream.of(inBufferSize);
		in = new PrintStream(pipe.out(), true);
		io.in(pipe.in());
		io.out(StringUtil.asPrintStream(out));
		io.err(StringUtil.asPrintStream(err));
	}

	@Override
	public void close() {
		pipe.close();
		io.close();
	}
}
