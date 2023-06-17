package ceri.serial.ftdi.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiConnector;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestFtdiConnector extends TestConnector implements FtdiConnector {
	public final CallSync.Consumer<FtdiBitMode> bitmode = CallSync.consumer(FtdiBitMode.OFF, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Consumer<FtdiFlowControl> flowControl =
		CallSync.consumer(FtdiFlowControl.disabled, true);
	public final CallSync.Supplier<Integer> pins = CallSync.supplier(0);

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
				in.to.write(data, offset, length);
				// Write last byte only
				if (length > 0) pins.autoResponses(data[offset + length - 1] & 0xff);
				// Don't write to pipe; it may fill up
				out.write.error.call(IO_ADAPTER);
				verifyConnected();
				return length;
			}
		};
		con.pins.autoResponses(0);
		return con;
	}

	public static TestFtdiConnector of() {
		return new TestFtdiConnector();
	}

	protected TestFtdiConnector() {}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(bitmode, dtr, rts, flowControl, pins);
	}

	@Override
	public void bitmode(FtdiBitMode bitmode) throws IOException {
		this.bitmode.accept(bitmode, IO_ADAPTER);
		verifyConnected();
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
	public void flowControl(FtdiFlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public int readPins() throws IOException {
		int n = pins.get(IO_ADAPTER);
		verifyConnected();
		return n;
	}

	@SuppressWarnings("resource")
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return in().read(buffer, offset, length);
	}

	@SuppressWarnings("resource")
	@Override
	public int write(byte[] data, int offset, int length) throws IOException {
		out().write(data, offset, length);
		return length;
	}

}
