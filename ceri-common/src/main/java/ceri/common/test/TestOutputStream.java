package ceri.common.test;

import static ceri.common.test.Assert.assertEquals;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.array.RawArray;
import ceri.common.data.ByteStream;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Functional;
import ceri.common.io.IoUtil;
import ceri.common.io.PipedStream;
import ceri.common.text.ToString;

/**
 * An OutputStream that wraps a PipedStream for testing. Write calls write to the pipe. CallSync
 * fields can be used to override behavior and generate errors.
 */
public class TestOutputStream extends OutputStream {
	private static final int DEFAULT_SIZE = 1024;
	private final PipedStream piped;
	public final CallSync.Consumer<RawArray.Sub<byte[]>> write = CallSync.consumer(null, true);
	public final CallSync.Runnable flush = CallSync.runnable(true);
	public final CallSync.Runnable close = CallSync.runnable(true);
	public final ByteStream.Reader from; // read from output

	public static TestOutputStream of() {
		return of(DEFAULT_SIZE);
	}

	public static TestOutputStream of(int pipeSize) {
		return new TestOutputStream(pipeSize);
	}

	@SuppressWarnings("resource")
	private TestOutputStream(int pipeSize) {
		piped = PipedStream.of(pipeSize);
		from = ByteStream.reader(piped.in());
	}

	public void resetState() {
		CallSync.resetAll(write, flush, close);
		Functional.muteRun(piped::clear);
	}

	public void assertAvailable(int n) throws IOException {
		assertEquals(from.available(), n);
	}

	@Override
	public void write(int b) throws IOException {
		write(ArrayUtil.bytes.of(b));
	}

	@SuppressWarnings("resource")
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		piped.out().write(b, off, len);
		write.accept(RawArray.Sub.of(b, off, len), ExceptionAdapter.io);
	}

	@SuppressWarnings("resource")
	@Override
	public void flush() throws IOException {
		piped.out().flush();
		flush.run(ExceptionAdapter.io);
	}

	@Override
	public void close() throws IOException {
		piped.close();
		close.run(ExceptionAdapter.io);
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@SuppressWarnings("resource")
	@Override
	public String toString() {
		return ToString.ofClass(this, Functional.muteGet(piped.in()::available))
			.children("write=" + write, "flush=" + flush, "close=" + close).toString();
	}

	/**
	 * Capture output until it matches the text pattern. Use (?s) for dot-all matches.
	 */
	public String awaitMatch(String pattern) throws IOException {
		return awaitMatch(pattern, Charset.defaultCharset());
	}

	/**
	 * Capture output until it matches the text pattern. Use (?s) for dot-all matches.
	 */
	public String awaitMatch(String pattern, Charset charset) throws IOException {
		StringBuilder b = new StringBuilder();
		Pattern p = Pattern.compile(pattern);
		while (true) {
			write.awaitAuto();
			b.append(IoUtil.availableString(from, charset));
			if (p.matcher(b).matches()) return b.toString();
		}
	}
}
