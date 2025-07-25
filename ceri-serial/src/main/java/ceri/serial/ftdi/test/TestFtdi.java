package ceri.serial.ftdi.test;

import java.io.IOException;
import com.sun.jna.Pointer;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.io.Direction;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiTransferControl;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;
import ceri.serial.ftdi.util.SelfHealingFtdi;

/**
 * A connector for testing logic against serial connectors.
 */
public class TestFtdi extends TestConnector implements Ftdi.Fixable {
	private static final String NAME = ReflectUtil.name(TestFtdi.class);
	public final CallSync.Supplier<ftdi_usb_strings> descriptor =
		CallSync.supplier(new ftdi_usb_strings("test", "test", "test"));
	public final CallSync.Runnable usbReset = CallSync.runnable(true);
	public final CallSync.Consumer<FtdiBitMode> bitMode = CallSync.consumer(FtdiBitMode.OFF, true);
	public final CallSync.Consumer<Integer> baud = CallSync.consumer(0, true);
	public final CallSync.Consumer<FtdiLineParams> params =
		CallSync.consumer(FtdiLineParams.DEFAULT, true);
	public final CallSync.Consumer<FtdiFlowControl> flowControl =
		CallSync.consumer(FtdiFlowControl.disabled, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Supplier<Integer> pins = CallSync.supplier(0);
	public final CallSync.Supplier<Integer> modem = CallSync.supplier(0);
	public final CallSync.Consumer<Integer> latency = CallSync.consumer(0, true); // in + out
	public final CallSync.Consumer<Integer> readChunk = CallSync.consumer(0, true); // in + out
	public final CallSync.Consumer<Integer> writeChunk = CallSync.consumer(0, true); // in + out
	public final CallSync.Runnable purgeBuffer = CallSync.runnable(true);
	public final CallSync.Function<Submit, FtdiTransferControl> submit =
		CallSync.function(null, FtdiTransferControl.NULL);
	public final CallSync.Consumer<Stream> stream = CallSync.consumer(null, true);

	/**
	 * Transfer control parameters.
	 */
	public static record Submit(Direction direction, Pointer buffer, int len) {}

	/**
	 * Read stream parameters.
	 */
	public static record Stream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) {}

	/**
	 * Returns config that generates an error on ftdi construction.
	 */
	public static SelfHealingFtdi.Config errorConfig() {
		return SelfHealingFtdi.Config.builder().ftdiFn(_ -> {
			throw new RuntimeException("generated");
		}).build();
	}

	/**
	 * Provide a pair of test ftdi devices that write to each other.
	 */
	@SuppressWarnings("resource")
	public static TestFtdi[] pairOf() {
		return TestConnector.chain(new TestFtdi(NAME + "[0->1]"), new TestFtdi(NAME + "[1->0]"));
	}

	/**
	 * Provide a test connector that echoes output to input, and writes the last byte to pins.
	 */
	public static TestFtdi ofPinEcho() {
		var ftdi = new TestFtdi(NAME + ":pinEcho");
		ftdi.echoPins();
		return ftdi;
	}

	public static TestFtdi of() {
		return new TestFtdi(null);
	}

	protected TestFtdi(String name) {
		super(name);
	}

	/**
	 * Create config for a self-healing wrapper constructor.
	 */
	public SelfHealingFtdi.Config selfHealingConfig() {
		return SelfHealingFtdi.Config.builder().factory((_, _) -> {
			open();
			return this;
		}).build();
	}

	/**
	 * Create config for a fixable ftdi constructor.
	 */
	public SelfHealingFtdi.Config config() {
		return SelfHealingFtdi.Config.builder().ftdiFn(_ -> this).build();
	}

	public void echoPins() {
		writeOverride((b, off, len) -> {
			in.to.write(b, off, len);
			if (len > 0) pins.autoResponses(b[off + len - 1] & 0xff);
		});
	}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(descriptor, usbReset, bitMode, baud, params, flowControl, dtr, rts, pins,
			modem, latency, readChunk, writeChunk, purgeBuffer);
	}

	@Override
	public ftdi_usb_strings descriptor() throws IOException {
		return descriptor.get(ExceptionAdapter.io);
	}

	@Override
	public void usbReset() throws IOException {
		usbReset.run(ExceptionAdapter.io);
	}

	@Override
	public void bitMode(FtdiBitMode bitMode) throws IOException {
		this.bitMode.accept(bitMode, ExceptionAdapter.io);
		verifyConnected();
	}

	@Override
	public void baud(int baud) throws IOException {
		this.baud.accept(baud, ExceptionAdapter.io);
	}

	@Override
	public void line(FtdiLineParams line) throws IOException {
		this.params.accept(line, ExceptionAdapter.io);
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, ExceptionAdapter.io);
		verifyConnected();
	}

	@Override
	public void dtr(boolean on) throws IOException {
		dtr.accept(on, ExceptionAdapter.io);
		verifyConnected();
	}

	@Override
	public void rts(boolean on) throws IOException {
		rts.accept(on, ExceptionAdapter.io);
		verifyConnected();
	}

	@Override
	public int readPins() throws IOException {
		int n = pins.get(ExceptionAdapter.io);
		verifyConnected();
		return n;
	}

	@Override
	public int pollModemStatus() throws IOException {
		int n = modem.get(ExceptionAdapter.io);
		verifyConnected();
		return n;
	}

	@Override
	public void latencyTimer(int latency) throws IOException {
		this.latency.accept(latency, ExceptionAdapter.io);
	}

	@Override
	public int latencyTimer() throws IOException {
		return latency.value();
	}

	@Override
	public void readChunkSize(int size) throws IOException {
		readChunk.accept(size, ExceptionAdapter.io);
	}

	@Override
	public int readChunkSize() throws IOException {
		return readChunk.value();
	}

	@Override
	public void writeChunkSize(int size) throws IOException {
		writeChunk.accept(size, ExceptionAdapter.io);
	}

	@Override
	public int writeChunkSize() throws IOException {
		return writeChunk.value();
	}

	@Override
	public void purgeReadBuffer() throws IOException {
		purgeBuffer.run(ExceptionAdapter.io);
	}

	@Override
	public void purgeWriteBuffer() throws IOException {
		purgeBuffer.run(ExceptionAdapter.io);
	}

	@Override
	public FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException {
		return submit(Direction.in, buffer, len);
	}

	@Override
	public FtdiTransferControl writeSubmit(Pointer buffer, int len) throws IOException {
		return submit(Direction.out, buffer, len);
	}

	private FtdiTransferControl submit(Direction direction, Pointer buffer, int len)
		throws IOException {
		var xfer = submit.apply(new Submit(direction, buffer, len), ExceptionAdapter.io);
		verifyConnected();
		return xfer;
	}

	@Override
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws IOException {
		stream.accept(new Stream(callback, packetsPerTransfer, numTransfers, progressIntervalSec),
			ExceptionAdapter.io);
		verifyConnected();
	}
}
