package ceri.common.test;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.PipedStream;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;

/**
 * Blocking input streams for testing hardware device controllers by simulating hardware
 * interaction. Allows for generation of errors when making i/o calls. Writing to the controller
 * will not block (unless PipedInputStream buffer is full); to block, call awaitFeed() to wait for
 * feed data to be read.
 */
public class TestPipedConnector implements Closeable, Listenable.Indirect<StateChange> {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	private final PipedStream.Connector con;
	public final ErrorGen readError = ErrorGen.of();
	public final ErrorGen availableError = ErrorGen.of();
	public final ErrorGen writeError = ErrorGen.of();
	private final InputStream in;
	private final OutputStream out;
	public final ByteStream.Reader from; // read code output
	public final ByteStream.Writer to; // write code input
	private volatile boolean eof = false;

	public static TestPipedConnector of() {
		return new TestPipedConnector();
	}

	@SuppressWarnings("resource")
	protected TestPipedConnector() {
		con = PipedStream.connector();
		in = IoStreamUtil.filterIn(con.in(), this::read, this::available);
		out = IoStreamUtil.filterOut(con.out(), this::writeWithReturn);
		to = ByteStream.writer(con.inFeed());
		from = ByteStream.reader(con.outSink());
	}

	/**
	 * Clear state.
	 */
	public void reset(boolean clearListeners) {
		if (clearListeners) listeners.clear();
		eof(false);
		readError.reset();
		availableError.reset();
		writeError.reset();
		execSilently(con::clear);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	/**
	 * Overrides read response with EOF.
	 */
	public void eof(boolean eof) {
		this.eof = eof;
	}

	/**
	 * The hardware input stream used by the device controller.
	 */
	public InputStream in() {
		return in;
	}

	/**
	 * The hardware output stream used by the device controller.
	 */
	public OutputStream out() {
		return out;
	}

	/**
	 * Wait for PipedInputStream to read available bytes.
	 */
	public void awaitFeed() throws IOException {
		con.pipedIn.awaitRead(1);
	}

	/**
	 * Assert available bytes from controller.
	 */
	@SuppressWarnings("resource")
	public void assertAvailable(int n) throws IOException {
		assertEquals(con.outSink().available(), n);
	}

	@Override
	public void close() {
		con.close();
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@Override
	public String toString() {
		return RUNTIME
			.get(() -> ToString.ofClass(this).field("listeners", listeners.size()).children(
				String.format("in=%s;%d;%s;%s", readError.mode(), in.available(),
					availableError.mode(), eof ? "EOF" : ""),
				String.format("out=%s;%d", writeError.mode(), from.available())))
			.toString();
	}

	/**
	 * Calls available before error generation logic.
	 */
	protected int available(InputStream in) throws IOException {
		int n = in.available();
		availableError.generateIo();
		return n;
	}

	/**
	 * Calls read before error generation logic. EOF overrides read response.
	 */
	protected int read(InputStream in, byte[] b, int offset, int length) throws IOException {
		int n = in.read(b, offset, length);
		if (eof) n = -1;
		readError.generateIo();
		return n;
	}

	/**
	 * Calls write before error generation logic.
	 */
	protected void write(OutputStream out, byte[] b, int offset, int length) throws IOException {
		out.write(b, offset, length);
		writeError.generateIo();
	}

	private boolean writeWithReturn(OutputStream out, byte[] b, int offset, int length)
		throws IOException {
		write(out, b, offset, length);
		return true;
	}
}
