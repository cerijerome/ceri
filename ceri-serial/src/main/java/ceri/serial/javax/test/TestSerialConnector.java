package ceri.serial.javax.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestPipedConnector;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestSerialConnector extends TestPipedConnector implements SerialConnector {
	public final CallSync.Accept<Boolean> broken = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> connect = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Accept<FlowControl> flowControl =
		CallSync.consumer(FlowControl.none, true);
	public final CallSync.Accept<Boolean> breakBit = CallSync.consumer(false, true);

	/**
	 * Provide a test connector that echoes output to input.
	 */
	public static TestSerialConnector echo() {
		return new TestSerialConnector() {
			@Override
			protected void write(OutputStream out, byte[] b, int offset, int length)
				throws IOException {
				verifyUnbroken();
				verifyConnected();
				to.write(b, offset, length);
			}
		};
	}

	public static TestSerialConnector of() {
		return new TestSerialConnector();
	}

	protected TestSerialConnector() {}

	@Override
	public void reset(boolean clearListeners) {
		broken.reset();
		connect.reset();
		dtr.reset();
		rts.reset();
		flowControl.reset();
		breakBit.reset();
		super.reset(clearListeners);
	}

	@Override
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

	@Override
	public void connect() throws IOException {
		connect.accept(true, IOException::new);
		verifyUnbroken();
	}

	@Override
	public void dtr(boolean on) throws IOException {
		dtr.accept(on, IOException::new);
		verifyUnbroken();
		verifyConnected();
	}

	@Override
	public void rts(boolean on) throws IOException {
		rts.accept(on, IOException::new);
		verifyUnbroken();
		verifyConnected();
	}

	@Override
	public void flowControl(FlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, IOException::new);
		verifyUnbroken();
		verifyConnected();
	}

	@Override
	public void breakBit(boolean on) throws IOException {
		breakBit.accept(on, IOException::new);
		verifyUnbroken();
		verifyConnected();
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
