package ceri.serial.ftdi.test;

import java.io.IOException;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestPipedConnector;
import ceri.serial.ftdi.FlowControl;
import ceri.serial.ftdi.FtdiBitmode;
import ceri.serial.ftdi.FtdiConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestFtdiConnector extends TestPipedConnector implements FtdiConnector {
	public final CallSync.Accept<Boolean> broken = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> connect = CallSync.consumer(false, true);
	public final CallSync.Accept<FtdiBitmode> bitmode = CallSync.consumer(FtdiBitmode.OFF, true);
	public final CallSync.Accept<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Accept<FlowControl> flowControl =
		CallSync.consumer(FlowControl.disabled, true);
	public final CallSync.Get<Integer> pins = CallSync.supplier(null);

	/**
	 * Provide a test connector that echoes output to input, and writes the last byte to pins.
	 */
	public static TestFtdiConnector echoPins() {
		var con = new TestFtdiConnector() {
			@SuppressWarnings("resource")
			@Override
			public int read(byte[] buffer, int offset, int length) throws IOException {
				if (in().available() == 0) return 0;
				return super.read(buffer, offset, length);
			}

			@Override
			public int write(byte[] data, int offset, int length) throws IOException {
				to.write(data, offset, length);
				// Write last byte only
				if (length > 0) pins.autoResponse(data[offset + length - 1] & 0xff);
				writeError.generateIo();
				verifyUnbroken();
				verifyConnected();
				return length;
			}
		};
		con.pins.autoResponse(0);
		return con;
	}

	public static TestFtdiConnector of() {
		return new TestFtdiConnector();
	}

	protected TestFtdiConnector() {}

	@Override
	public void reset(boolean clearListeners) {
		broken.reset();
		connect.reset();
		bitmode.reset();
		dtr.reset();
		rts.reset();
		flowControl.reset();
		pins.reset();
		super.reset(clearListeners);
	}

	@Override
	public void broken() {
		if (!broken.value()) listeners.accept(StateChange.broken);
		broken.accept(true);
		connect.value(false);
	}

	public void fixed() {
		if (broken.value()) listeners.accept(StateChange.fixed);
		broken.accept(false);
		connect.value(true);
	}

	@Override
	public void connect() throws IOException {
		connect.accept(true, IOException::new);
		verifyUnbroken();
	}

	@Override
	public void bitmode(FtdiBitmode bitmode) throws IOException {
		this.bitmode.accept(bitmode, IOException::new);
		verifyUnbroken();
		verifyConnected();
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
	public int readPins() throws IOException {
		Integer n = pins.get(IOException::new);
		verifyUnbroken();
		verifyConnected();
		return n == null ? 0 : n;
	}

	@SuppressWarnings("resource")
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int n = in().read(buffer, offset, length);
		verifyUnbroken();
		verifyConnected();
		return n;
	}

	@SuppressWarnings("resource")
	@Override
	public int write(byte[] data, int offset, int length) throws IOException {
		out().write(data, offset, length);
		verifyUnbroken();
		verifyConnected();
		return length;
	}

	@Override
	public void close() {
		connect.value(false);
	}

	protected void verifyConnected() throws IOException {
		if (!connect.value()) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken.value()) throw new IOException("Connector is broken");
	}

}
