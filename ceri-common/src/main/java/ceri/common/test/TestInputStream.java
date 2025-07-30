package ceri.common.test;

import static ceri.common.util.BasicUtil.def;
import static ceri.common.validation.ValidationUtil.validateEqual;
import java.io.IOException;
import java.io.InputStream;
import ceri.common.array.RawArrays;
import ceri.common.data.ByteStream;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Fluent;
import ceri.common.function.FunctionUtil;
import ceri.common.io.PipedStream;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

/**
 * An InputStream that wraps a PipedStream for testing. Read calls read from the pipe. CallSync
 * fields can be used to override behavior and generate errors.
 */
public class TestInputStream extends InputStream implements Fluent<TestInputStream> {
	private static final int DEFAULT_SIZE = 1024;
	private final PipedStream piped;
	public final CallSync.Function<RawArrays.Sub<byte[]>, Integer> read =
		CallSync.function(null, (Integer) null);
	public final CallSync.Supplier<Integer> available = CallSync.supplier((Integer) null);
	public final CallSync.Consumer<Integer> mark = CallSync.consumer(null, true);
	public final CallSync.Runnable reset = CallSync.runnable(true);
	public final CallSync.Runnable close = CallSync.runnable(true);
	public final CallSync.Supplier<Boolean> markSupported = CallSync.supplier((Boolean) null);
	public final ByteStream.Writer to; // write to input

	@SuppressWarnings("resource")
	public static TestInputStream from(int... bytes) {
		return of().apply(in -> in.to.writeBytes(bytes));
	}

	@SuppressWarnings("resource")
	public static TestInputStream from(String format, Object... args) {
		return of().apply(in -> in.to.writeUtf8(StringUtil.format(format, args)));
	}

	public static TestInputStream of() {
		return of(DEFAULT_SIZE);
	}

	public static TestInputStream of(int pipeSize) {
		return new TestInputStream(pipeSize);
	}

	@SuppressWarnings("resource")
	private TestInputStream(int pipeSize) {
		piped = PipedStream.of(pipeSize);
		to = ByteStream.writer(piped.out());
	}

	public void resetState() {
		CallSync.resetAll(read, available, mark, reset, close);
		FunctionUtil.runSilently(piped::clear);
	}

	/**
	 * Wait for PipedInputStream to read available bytes.
	 */
	public void awaitFeed() throws IOException {
		piped.awaitRead(1);
	}

	/**
	 * Set read auto-response to EOF or delegate (null). This replaces any currently configured
	 * auto-response function.
	 */
	public void eof(boolean enabled) {
		if (enabled) read.autoResponses(-1);
		else read.autoResponses((Integer) null);
	}

	@Override
	public int read() throws IOException {
		byte[] bytes = new byte[1];
		int n = read(bytes);
		if (n < 0) return n;
		validateEqual(n, 1);
		return bytes[0] & 0xff;
	}

	@SuppressWarnings("resource")
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int n = piped.in().read(b, off, len);
		return def(read.apply(RawArrays.Sub.of(b, off, len), ExceptionAdapter.io), n);
	}

	@SuppressWarnings("resource")
	@Override
	public int available() throws IOException {
		int n = piped.in().available();
		return def(available.get(ExceptionAdapter.io), n);
	}

	@Override
	public void mark(int readLimit) {
		// Not supported by PipedInputStream
		mark.accept(readLimit);
	}

	@Override
	public void reset() throws IOException {
		// Not supported by PipedInputStream
		reset.run(ExceptionAdapter.io);
	}

	@Override
	public boolean markSupported() {
		return markSupported.get();
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
		return ToString
			.ofClass(this, FunctionUtil.getSilently(piped.in()::available)).children("read=" + read,
				"available=" + available, "mark=" + mark, "reset=" + reset, "close=" + close)
			.toString();
	}
}
