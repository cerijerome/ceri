package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.FunctionUtil;
import ceri.common.io.Connector;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.IoStreamUtil.Write;
import ceri.common.text.ToString;

/**
 * A connector implementation for tests, using piped streams.
 */
public class TestConnector extends TestFixable implements Connector.Fixable {
	public final ErrorGen error = ErrorGen.of(); // for generating general errors
	public final TestInputStream in;
	public final TestOutputStream out;
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
		super(name);
		in = TestInputStream.of();
		out = TestOutputStream.of();
		wrappedIn = IoStreamUtil.filterIn(in, this::read, this::available);
		wrappedOut = IoStreamUtil.filterOut(out, this::writeWithReturn);
	}

	/**
	 * Clear state.
	 */
	@Override
	public void reset() {
		super.reset();
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
	public InputStream in() {
		error.call();
		return wrappedIn;
	}

	@Override
	public OutputStream out() {
		error.call();
		return wrappedOut;
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
		super.close();
	}

	@Override
	protected ToString asString() {
		return super.asString().children(in, out);
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

	private boolean writeWithReturn(OutputStream out, byte[] b, int offset, int length)
		throws IOException {
		write(out, b, offset, length);
		return true;
	}
}
