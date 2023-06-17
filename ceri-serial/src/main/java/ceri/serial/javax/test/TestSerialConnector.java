package ceri.serial.javax.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.OutputStream;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestSerialConnector extends TestConnector implements SerialConnector {
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Consumer<FlowControl> flowControl =
		CallSync.consumer(FlowControl.none, true);
	public final CallSync.Consumer<Boolean> breakBit = CallSync.consumer(false, true);

	/**
	 * Provide a test connector that echoes output to input.
	 */
	public static TestSerialConnector echo() {
		return new TestSerialConnector() {
			@Override
			protected void write(OutputStream out, byte[] b, int offset, int length)
				throws IOException {
				super.write(out, b, offset, length);
				TestConnector.echo(this, b, offset, length);
			}
		};
	}

	public static TestSerialConnector of() {
		return new TestSerialConnector();
	}

	protected TestSerialConnector() {}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(dtr, rts, flowControl, breakBit);
	}

	@Override
	public void dtr(boolean on) throws IOException {
		dtr.accept(on, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void rts(boolean on) throws IOException {
		rts.accept(on, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void flowControl(FlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void breakBit(boolean on) throws IOException {
		breakBit.accept(on, IO_ADAPTER);
		verifyConnected();
	}
}
