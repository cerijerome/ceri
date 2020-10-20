package ceri.log.io.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestPipedConnector;
import ceri.log.io.SocketConnector;

/**
 * A connector for testing logic against socket connectors.
 */
public class TestSocketConnector extends TestPipedConnector implements SocketConnector {
	public final CallSync.Accept<Boolean> connect = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> broken = CallSync.consumer(false, true);

	/**
	 * Provide a test connector that echoes output to input.
	 */
	public static TestSocketConnector echo() {
		return new TestSocketConnector() {
			@Override
			protected void write(OutputStream out, byte[] b, int offset, int length)
				throws IOException {
				verifyUnbroken();
				verifyConnected();
				to.write(b, offset, length);
			}
		};
	}

	public static TestSocketConnector of() {
		return new TestSocketConnector();
	}

	protected TestSocketConnector() {}

	@Override
	public void reset(boolean clearListeners) {
		connect.reset();
		broken.reset();
		super.reset(clearListeners);
	}

	@Override
	public void broken() {
		if (!broken.value()) listeners.accept(StateChange.broken);
		broken.accept(true);
		connect.value(false);
	}

	public void fixed() {
		if (!broken.value()) listeners.accept(StateChange.fixed);
		broken.accept(false);
		connect.value(true);
	}

	@Override
	public void connect() throws IOException {
		connect.accept(true);
		verifyUnbroken();
	}

	@Override
	public void close() {
		connect.value(false);
	}

	@Override
	protected int available(InputStream in) throws IOException {
		int n = super.available(in);
		verifyUnbroken();
		verifyConnected();
		return n;
	}

	@Override
	protected int read(InputStream in, byte[] b, int offset, int length) throws IOException {
		int n = super.read(in, b, offset, length);
		verifyUnbroken();
		verifyConnected();
		return n;
	}

	@Override
	protected void write(OutputStream out, byte[] b, int offset, int length) throws IOException {
		super.write(out, b, offset, length);
		verifyUnbroken();
		verifyConnected();
	}

	protected void verifyConnected() throws IOException {
		if (!connect.value()) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken.value()) throw new IOException("Connector is broken");
	}

}
