package ceri.common.test;

import static ceri.common.function.FunctionUtil.callSilently;
import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.util.BasicUtil.defaultValue;
import static ceri.common.validation.ValidationUtil.validateEqual;
import java.io.IOException;
import java.io.InputStream;
import ceri.common.collection.SubArray;
import ceri.common.collection.SubArray.Bytes;
import ceri.common.data.ByteStream;
import ceri.common.function.Fluent;
import ceri.common.io.PipedStream;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

/**
 * A test InputStream that wraps a PipedStream. Method calls can be overridden or delegated to the
 * piped stream. Errors can be also be generated.
 */
public class TestInputStream extends InputStream implements Fluent<TestInputStream> {
	public static final Integer DELEGATE = null; // CallSync response to delegate to pipe
	private static final int DEFAULT_SIZE = 1024;
	private final PipedStream piped;
	public final CallSync.Apply<Bytes, Integer> read = CallSync.function(null, DELEGATE);
	public final CallSync.Get<Integer> available = CallSync.supplier(DELEGATE);
	public final CallSync.Accept<Integer> mark = CallSync.consumer(null, true);
	public final CallSync.Run reset = CallSync.runnable(true);
	public final CallSync.Run close = CallSync.runnable(true);
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
		read.reset();
		available.reset();
		mark.reset();
		reset.reset();
		close.reset();
		execSilently(piped::clear);
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
		else read.autoResponses(DELEGATE);
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
		return defaultValue(read.apply(SubArray.of(b, off, len), IO_ADAPTER), n);
	}

	@SuppressWarnings("resource")
	@Override
	public int available() throws IOException {
		int n = piped.in().available();
		return defaultValue(available.get(IO_ADAPTER), n);
	}

	@SuppressWarnings("resource")
	@Override
	public void mark(int readLimit) {
		piped.in().mark(readLimit);
		mark.accept(readLimit);
	}

	@SuppressWarnings("resource")
	@Override
	public void reset() throws IOException {
		piped.in().reset();
		reset.run(IO_ADAPTER);
	}

	@SuppressWarnings("resource")
	@Override
	public boolean markSupported() {
		return piped.in().markSupported();
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
			"read=" + read, "available=" + available, "mark=" + mark, "reset=" + reset,
			"close=" + close).toString();
	}
}
