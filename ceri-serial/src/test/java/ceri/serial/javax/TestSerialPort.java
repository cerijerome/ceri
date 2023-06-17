package ceri.serial.javax;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.test.CallSync;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;

public class TestSerialPort extends SerialPort {
	public final CallSync.Consumer<String> open = CallSync.consumer(null, true);
	public final CallSync.Consumer<SerialPortParams> params = CallSync.consumer(null, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Consumer<FlowControl> flowControl =
		CallSync.consumer(FlowControl.none, true);
	public final CallSync.Consumer<Boolean> breakBit = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> closed = CallSync.consumer(false, true);
	public final TestInputStream in = TestInputStream.of();
	public final TestOutputStream out = TestOutputStream.of();
	
	public static TestSerialPort of() {
		return new TestSerialPort();
	}

	private TestSerialPort() {
		super(null);
	}

	public TestSerialPort openPort(String comPort) throws IOException {
		open.accept(comPort, IO_ADAPTER);
		return this;
	}
	
	@Override
	public void setParams(SerialPortParams params) throws IOException {
		this.params.accept(params, IO_ADAPTER);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public void setBreakBit() throws IOException {
		breakBit.accept(true, IO_ADAPTER);
	}
	
	@Override
	public void clearBreakBit() throws IOException {
		breakBit.accept(false, IO_ADAPTER);
	}
	
	@Override
	public void setDTR(boolean state) {
		dtr.accept(state);
	}
	
	@Override
	public void setRTS(boolean state) {
		rts.accept(state);
	}
	
	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, IO_ADAPTER);
	}
	
	@Override
	public void close() {
		closed.accept(true);
	}
}
