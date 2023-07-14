package ceri.serial.comm.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.util.ThreadBuffers;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestSerial extends TestConnector implements Serial.Fixable {
	private static final String NAME = ReflectUtil.name(TestSerial.class);
	public static final int DTR = CIoctl.TIOCM_DTR;
	public static final int RTS = CIoctl.TIOCM_RTS;
	public static final int CTS = CIoctl.TIOCM_CTS;
	public static final int CD = CIoctl.TIOCM_CD;
	public static final int RI = CIoctl.TIOCM_RI;
	public static final int DSR = CIoctl.TIOCM_DSR;
	public final CallSync.Supplier<String> port = CallSync.supplier("test");
	public final CallSync.Consumer<Integer> bufferSize =
		CallSync.consumer(ThreadBuffers.SIZE_DEF, true); // shared for in and out
	public final CallSync.Consumer<SerialParams> params =
		CallSync.consumer(SerialParams.DEFAULT, true);
	public final CallSync.Consumer<Set<FlowControl>> flowControl =
		CallSync.consumer(FlowControl.NONE, true);
	public final CallSync.Consumer<Boolean> brk = CallSync.consumer(false, true);
	// List<?> = int flag, boolean on
	public final CallSync.Consumer<List<?>> flagOut = CallSync.consumer(List.of(), true);
	public final CallSync.Function<Integer, Boolean> flagIn = CallSync.function(0, false);

	/**
	 * Provide a test serial port that echoes output to input.
	 */
	@SuppressWarnings("resource")
	public static TestSerial ofEcho() {
		return TestConnector.echoOn(new TestSerial(NAME + ":echo"));
	}

	/**
	 * Provide a pair of test serial ports that write to each other.
	 */
	@SuppressWarnings("resource")
	public static TestSerial[] pairOf() {
		return TestConnector.chain(new TestSerial(NAME + "[0->1]"),
			new TestSerial(NAME + "[1->0]"));
	}

	public static TestSerial of() {
		return new TestSerial(null);
	}

	private TestSerial(String name) {
		super(name);
	}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(port, bufferSize, params, flowControl, brk, flagIn, flagOut);
	}

	@Override
	public String port() {
		return port.get();
	}

	@Override
	public void inBufferSize(int size) {
		bufferSize.accept(size);
	}

	@Override
	public int inBufferSize() {
		return bufferSize.value();
	}

	@Override
	public void outBufferSize(int size) {
		bufferSize.accept(size);
	}

	@Override
	public int outBufferSize() {
		return bufferSize.value();
	}

	@Override
	public void params(SerialParams params) throws IOException {
		this.params.accept(params, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public SerialParams params() {
		return params.value();
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		this.flowControl.accept(Set.copyOf(flowControl), IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public Set<FlowControl> flowControl() {
		return flowControl.value();
	}

	@Override
	public void brk(boolean on) throws IOException {
		brk.accept(on, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void rts(boolean on) throws IOException {
		flagOut(RTS, on);
	}

	@Override
	public void dtr(boolean on) throws IOException {
		flagOut(DTR, on);
	}

	@Override
	public boolean rts() throws IOException {
		return flagIn(RTS);
	}

	@Override
	public boolean dtr() throws IOException {
		return flagIn(DTR);
	}

	@Override
	public boolean cd() throws IOException {
		return flagIn(CD);
	}

	@Override
	public boolean cts() throws IOException {
		return flagIn(CTS);
	}

	@Override
	public boolean dsr() throws IOException {
		return flagIn(DSR);
	}

	@Override
	public boolean ri() throws IOException {
		return flagIn(RI);
	}

	private void flagOut(int flag, boolean on) throws IOException {
		flagOut.accept(List.of(flag, on), IO_ADAPTER);
		verifyConnected();
	}

	private boolean flagIn(int flag) throws IOException {
		boolean on = flagIn.apply(flag);
		verifyConnected();
		return on;
	}

}
