package ceri.serial.comm.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.jna.util.ThreadBuffers;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerial.Config.SerialFactory;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestSerial extends TestConnector implements Serial.Fixable {
	private static final String NAME = ReflectUtil.name(TestSerial.class);
	public final CallSync.Supplier<String> port = CallSync.supplier("test");
	public final CallSync.Consumer<Integer> inBufferSize =
		CallSync.consumer(ThreadBuffers.SIZE_DEF, true);
	public final CallSync.Consumer<Integer> outBufferSize =
		CallSync.consumer(ThreadBuffers.SIZE_DEF, true);
	public final CallSync.Consumer<SerialParams> params =
		CallSync.consumer(SerialParams.DEFAULT, true);
	public final CallSync.Consumer<Set<FlowControl>> flowControl =
		CallSync.consumer(FlowControl.NONE, true);
	public final CallSync.Consumer<Boolean> brk = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Supplier<Boolean> cd = CallSync.supplier(false);
	public final CallSync.Supplier<Boolean> cts = CallSync.supplier(false);
	public final CallSync.Supplier<Boolean> dsr = CallSync.supplier(false);
	public final CallSync.Supplier<Boolean> ri = CallSync.supplier(false);

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

	/**
	 * Returns a factory that opens and returns this instance.
	 */
	public SerialFactory factory() {
		return port -> {
			open();
			return this;
		};
	}

	public SelfHealingSerial.Config selfHealingConfig(String port) {
		return SelfHealingSerial.Config.builder(port).factory(factory()).build();
	}
	
	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(port, inBufferSize, outBufferSize, params, flowControl, brk, rts, dtr, cd,
			cts, dsr, ri);
	}

	@Override
	public String port() {
		return port.get();
	}

	@Override
	public void inBufferSize(int size) {
		inBufferSize.accept(size);
	}

	@Override
	public int inBufferSize() {
		return inBufferSize.value();
	}

	@Override
	public void outBufferSize(int size) {
		outBufferSize.accept(size);
	}

	@Override
	public int outBufferSize() {
		return outBufferSize.value();
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
		rts.accept(on, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void dtr(boolean on) throws IOException {
		dtr.accept(on, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public boolean rts() throws IOException {
		verifyConnected();
		return rts.lastValue(IO_ADAPTER);
	}

	@Override
	public boolean dtr() throws IOException {
		verifyConnected();
		return dtr.lastValue(IO_ADAPTER);
	}

	@Override
	public boolean cd() throws IOException {
		verifyConnected();
		return cd.get(IO_ADAPTER);
	}

	@Override
	public boolean cts() throws IOException {
		verifyConnected();
		return cts.get(IO_ADAPTER);
	}

	@Override
	public boolean dsr() throws IOException {
		verifyConnected();
		return dsr.get(IO_ADAPTER);
	}

	@Override
	public boolean ri() throws IOException {
		verifyConnected();
		return ri.get(IO_ADAPTER);
	}
}
