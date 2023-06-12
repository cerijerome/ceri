package ceri.common.test;

import static ceri.common.function.FunctionUtil.callSilently;
import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.SubArray;
import ceri.common.collection.SubArray.Bytes;
import ceri.common.data.ByteStream;
import ceri.common.io.IoUtil;
import ceri.common.io.PipedStream;
import ceri.common.text.ToString;

public class TestOutputStream extends OutputStream {
	private static final int DEFAULT_SIZE = 1024;
	private final PipedStream piped;
	public final CallSync.Consumer<Bytes> write = CallSync.consumer(null, true);
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
		execSilently(piped::clear);
	}

	public void assertAvailable(int n) throws IOException {
		assertEquals(from.available(), n);
	}

	@Override
	public void write(int b) throws IOException {
		write(ArrayUtil.bytes(b));
	}

	@SuppressWarnings("resource")
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		piped.out().write(b, off, len);
		write.accept(SubArray.of(b, off, len), IO_ADAPTER);
	}

	@SuppressWarnings("resource")
	@Override
	public void flush() throws IOException {
		piped.out().flush();
		flush.run(IO_ADAPTER);
	}

	@Override
	public void close() throws IOException {
		piped.close();
		close.run(IO_ADAPTER);
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@SuppressWarnings("resource")
	@Override
	public String toString() {
		return ToString.ofClass(this, callSilently(piped.in()::available)).children( //
			"write=" + write, "flush=" + flush, "close=" + close).toString();
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
