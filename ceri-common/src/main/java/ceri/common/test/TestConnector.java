package ceri.common.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.function.FunctionUtil;
import ceri.common.io.Connector;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.IoStreamUtil.Write;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;

/**
 * A connector implementation for tests, using piped streams.
 */
public class TestConnector implements Connector.Fixable {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final CallSync.Consumer<Boolean> open = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> broken = CallSync.consumer(false, true);
	public final CallSync.Runnable close = CallSync.runnable(true);
	public final TestInputStream in;
	public final TestOutputStream out;
	private final String name;
	private final InputStream wrappedIn;
	private final OutputStream wrappedOut;
	private volatile Write writeOverride = null;

	/**
	 * Convenience method to enable echo from input to output streams.
	 */
	public static <T extends TestConnector> T echoOn(T connector) {
		connector.echoOn();
		return connector;
	}

	/**
	 * Connects outputs to inputs for consecutive connectors.
	 */
	@SafeVarargs
	public static <T extends TestConnector> T[] chain(T... connectors) {
		for (int i = 0; i < connectors.length; i++)
			connectors[i].pairWith(connectors[(i + 1) % connectors.length]);
		return connectors;
	}

	/**
	 * Create a new instance with default name.
	 */
	public static TestConnector of() {
		return new TestConnector(null);
	}

	/**
	 * Constructor with optional name override. Use null for the default name.
	 */
	protected TestConnector(String name) {
		this.name = name;
		in = TestInputStream.of();
		out = TestOutputStream.of();
		wrappedIn = IoStreamUtil.filterIn(in, this::read, this::available);
		wrappedOut = IoStreamUtil.filterOut(out, this::writeWithReturn);
	}

	/**
	 * Clear state.
	 */
	public void reset() {
		listeners.clear();
		CallSync.resetAll(broken, open);
		in.resetState();
		out.resetState();
	}

	/**
	 * Enable echo; input data is written to output.
	 */
	public void echoOn() {
		pairWith(this);
	}

	/**
	 * Enable pairing; input data is written to another connector.
	 */
	public void pairWith(TestConnector other) {
		writeOverride((b, off, len) -> other.in.to.write(b, off, len));
	}

	/**
	 * Enable pairing; input data is written to another connector.
	 */
	public void pairWith(Connector other) {
		writeOverride((b, off, len) -> other.out().write(b, off, len));
	}

	/**
	 * Override writing, instead of writing to the test output stream.
	 */
	public void writeOverride(Write writeOverride) {
		this.writeOverride = writeOverride;
	}

	@Override
	public String name() {
		return name == null ? Connector.Fixable.super.name() : name;
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void broken() {
		if (!broken.value()) listeners.accept(StateChange.broken);
		broken.accept(true);
		open.value(false);
	}

	/**
	 * Manually mark the connector as fixed.
	 */
	public void fixed() {
		open.value(true); // don't signal call
		if (broken.value()) listeners.accept(StateChange.fixed);
		broken.accept(false);
	}

	@Override
	public void open() throws IOException {
		open.accept(true, IO_ADAPTER);
		verifyUnbroken();
	}

	@Override
	public InputStream in() {
		return wrappedIn;
	}

	@Override
	public OutputStream out() {
		return wrappedOut;
	}

	@Override
	public void close() throws IOException {
		open.value(false);
		in.close();
		out.close();
		close.run(IO_ADAPTER);
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@Override
	public String toString() {
		return ToString.ofClass(this, listeners.size(), "broken=" + broken, "open=" + open)
			.children("in=" + in, "out=" + out).toString();
	}

	/**
	 * Calls available before error generation logic.
	 */
	private int available(InputStream in) throws IOException {
		int n = in.available();
		verifyConnected();
		return n;
	}

	/**
	 * Calls read before error generation logic. EOF overrides read response.
	 */
	private int read(InputStream in, byte[] b, int offset, int length) throws IOException {
		int n = in.read(b, offset, length);
		verifyConnected();
		return n;
	}

	/**
	 * Calls write before error generation logic.
	 */
	private void write(OutputStream out, byte[] b, int offset, int length) throws IOException {
		verifyConnected();
		if (!FunctionUtil.safeAccept(writeOverride, w -> w.write(b, offset, length)))
			out.write(b, offset, length);
	}

	protected void verifyConnected() throws IOException {
		verifyUnbroken();
		if (!open.value()) throw new IOException("Not connected");
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
