package ceri.serial.javax.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.StateChange;
import ceri.common.test.SyncState;
import ceri.common.test.TestPipedConnector;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestSerialConnector extends TestPipedConnector implements SerialConnector {
	public final SyncState.Bool connectSync = SyncState.bool();
	public final SyncState<Boolean> dtrSync = SyncState.of();
	public final SyncState<Boolean> rtsSync = SyncState.of();
	public final SyncState<FlowControl> flowCtrlSync = SyncState.of();
	public final SyncState<Boolean> breakBitSync = SyncState.of();
	public final SyncState<Boolean> brokenSync = SyncState.of();
	// State fields
	public volatile boolean breakBit;
	public volatile boolean rts;
	public volatile boolean dtr;
	public volatile FlowControl flowControl = FlowControl.none;
	public volatile boolean connected;
	public volatile boolean broken;

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

	protected TestSerialConnector() {
		connectSync.resume(false);
		dtrSync.resume(false);
		rtsSync.resume(false);
		flowCtrlSync.resume(false);
		breakBitSync.resume(false);
		brokenSync.resume(false);
	}

	@Override
	public void reset(boolean clearListeners) {
		connectSync.reset();
		dtrSync.reset();
		rtsSync.reset();
		flowCtrlSync.reset();
		breakBitSync.reset();
		breakBit = false;
		rts = false;
		dtr = false;
		flowControl = FlowControl.none;
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
	public void setDtr(boolean on) throws IOException {
		dtrSync.accept(on);
		verifyUnbroken();
		verifyConnected();
		dtr = on;
	}

	@Override
	public void setRts(boolean on) throws IOException {
		rtsSync.accept(on);
		verifyUnbroken();
		verifyConnected();
		rts = on;
	}

	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		flowCtrlSync.accept(flowControl);
		verifyUnbroken();
		verifyConnected();
		this.flowControl = flowControl;
	}

	@Override
	public void setBreakBit(boolean on) throws IOException {
		breakBitSync.accept(on);
		verifyUnbroken();
		verifyConnected();
		breakBit = on;
	}

	@Override
	public void close() {
		connected = false;
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
		if (!connected) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken) throw new IOException("Connector is broken");
	}

}
