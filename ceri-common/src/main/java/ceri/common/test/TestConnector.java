package ceri.common.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;

/**
 * Blocking streams for testing hardware device controllers by simulating hardware interaction.
 * Allows for generation of errors when making i/o calls. Writing to the controller will not block
 * (unless PipedInputStream buffer is full); to block, call awaitFeed() to wait for feed data to be
 * read.
 */
public class TestConnector implements Closeable, Listenable.Indirect<StateChange> {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final CallSync.Accept<Boolean> connect = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> broken = CallSync.consumer(false, true);
	public final TestInputStream in;
	public final TestOutputStream out;
	private final InputStream wrappedIn;
	private final OutputStream wrappedOut;

	/**
	 * Echo write data to input, consistent with connect/broken states.
	 */
	protected static void echo(TestConnector con, byte[] b, int offset, int length)
		throws IOException {
		con.verifyConnected();
		con.in.to.write(b, offset, length);
	}

	public static TestConnector of() {
		return new TestConnector();
	}

	protected TestConnector() {
		in = TestInputStream.of();
		out = TestOutputStream.of();
		wrappedIn = IoStreamUtil.filterIn(in, this::read, this::available);
		wrappedOut = IoStreamUtil.filterOut(out, this::writeWithReturn);
	}

	/**
	 * Clear state.
	 */
	public void reset(boolean clearListeners) {
		if (clearListeners) listeners.clear();
		broken.reset();
		connect.reset();
		in.resetState();
		out.resetState();
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	public void broken() {
		if (!broken.value()) listeners.accept(StateChange.broken);
		broken.accept(true);
		connect.value(false);
	}

	public void fixed() {
		connect.value(true); // don't signal call
		if (broken.value()) listeners.accept(StateChange.fixed);
		broken.accept(false);
	}

	public void connect() throws IOException {
		connect.accept(true, IO_ADAPTER);
		verifyUnbroken();
	}

	/**
	 * The hardware input stream used by the device controller.
	 */
	public InputStream in() {
		return wrappedIn;
	}

	/**
	 * The hardware output stream used by the device controller.
	 */
	public OutputStream out() {
		return wrappedOut;
	}

	@Override
	public void close() throws IOException {
		connect.value(false);
		in.close();
		out.close();
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@Override
	public String toString() {
		return ToString.ofClass(this, listeners.size())
			.children("broken=" + broken, "connect=" + connect, "in=" + in, "out=" + out)
			.toString();
	}

	/**
	 * Calls available before error generation logic.
	 */
	protected int available(InputStream in) throws IOException {
		int n = in.available();
		verifyConnected();
		return n;
	}

	/**
	 * Calls read before error generation logic. EOF overrides read response.
	 */
	protected int read(InputStream in, byte[] b, int offset, int length) throws IOException {
		int n = in.read(b, offset, length);
		verifyConnected();
		return n;
	}

	/**
	 * Calls write before error generation logic.
	 */
	protected void write(OutputStream out, byte[] b, int offset, int length) throws IOException {
		out.write(b, offset, length);
		verifyConnected();
	}

	protected void verifyConnected() throws IOException {
		verifyUnbroken();
		if (!connect.value()) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken.value()) throw new IOException("Connector is broken");
	}

	private boolean writeWithReturn(OutputStream out, byte[] b, int offset, int length)
		throws IOException {
		write(out, b, offset, length);
		return true;
	}
}
