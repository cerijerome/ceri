package ceri.serial.ftdi.test;

import java.io.IOException;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.io.StateChange;
import ceri.common.test.SyncState;
import ceri.common.test.TestPipedConnector;
import ceri.serial.ftdi.FlowControl;
import ceri.serial.ftdi.FtdiBitmode;
import ceri.serial.ftdi.FtdiConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestFtdiConnector extends TestPipedConnector implements FtdiConnector {
	public final SyncState.Bool connectSync = SyncState.boolNoResume();
	public final SyncState<FtdiBitmode> bitmodeSync = SyncState.noResume();
	public final SyncState<Boolean> dtrSync = SyncState.noResume();
	public final SyncState<Boolean> rtsSync = SyncState.noResume();
	public final SyncState<FlowControl> flowCtrlSync = SyncState.noResume();
	public final SyncState<Integer> pinsSync = SyncState.noResume();
	public final SyncState<Boolean> brokenSync = SyncState.noResume();
	// State fields
	public volatile FtdiBitmode bitmode = FtdiBitmode.OFF;
	public volatile boolean rts;
	public volatile boolean dtr;
	public volatile FlowControl flowControl = FlowControl.disabled;
	public volatile boolean connected;
	public volatile boolean broken;

	/**
	 * Provide a test connector that echoes output to input, writes the last byte to pins.
	 */
	public static TestFtdiConnector echoPins() {
		return new TestFtdiConnector() {
			@Override
			public int readPins() throws IOException {
				Integer b = pinsSync.get();
				verifyUnbroken();
				verifyConnected();
				return b == null ? 0 : b;
			}

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
				if (length > 0) pinsSync.accept(data[offset + length - 1] & 0xff);
				writeError.generateIo();
				verifyUnbroken();
				verifyConnected();
				return length;
			}
		};
	}

	public static TestFtdiConnector of() {
		return new TestFtdiConnector();
	}

	protected TestFtdiConnector() {}

	@Override
	public void reset(boolean clearListeners) {
		connectSync.reset();
		bitmodeSync.reset();
		dtrSync.reset();
		rtsSync.reset();
		flowCtrlSync.reset();
		pinsSync.reset();
		brokenSync.reset();
		bitmode = FtdiBitmode.OFF;
		rts = false;
		dtr = false;
		flowControl = FlowControl.disabled;
		connected = false;
		broken = false;
		super.reset(clearListeners);
	}

	@Override
	public void broken() {
		boolean notify = !broken;
		broken = true;
		if (notify) listeners.accept(StateChange.broken);
		brokenSync.accept(true);
	}

	public void fixed() {
		boolean notify = broken;
		broken = false;
		connected = true;
		if (notify) listeners.accept(StateChange.fixed);
		brokenSync.accept(false);
	}

	@Override
	public void connect() throws IOException {
		connectSync.accept();
		verifyUnbroken();
		connected = true;
	}

	@Override
	public void bitmode(FtdiBitmode bitmode) throws IOException {
		bitmodeSync.accept(bitmode);
		verifyUnbroken();
		verifyConnected();
		this.bitmode = bitmode;
	}

	@Override
	public void dtr(boolean on) throws IOException {
		dtrSync.accept(on);
		verifyUnbroken();
		verifyConnected();
		dtr = on;
	}

	@Override
	public void rts(boolean on) throws IOException {
		rtsSync.accept(on);
		verifyUnbroken();
		verifyConnected();
		rts = on;
	}

	@Override
	public void flowControl(FlowControl flowControl) throws IOException {
		flowCtrlSync.accept(flowControl);
		verifyUnbroken();
		verifyConnected();
		this.flowControl = flowControl;
	}

	@Override
	public int readPins() throws IOException {
		Integer n = ConcurrentUtil.executeGetInterruptible(pinsSync::awaitCall);
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
		connected = false;
	}

	protected void verifyConnected() throws IOException {
		if (!connected) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken) throw new IOException("Connector is broken");
	}

}
