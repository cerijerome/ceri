package ceri.serial.ftdi.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.util.List;
import com.sun.jna.Pointer;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.CallSync;
import ceri.common.test.TestConnector;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiTransferControl;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;

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
	public final CallSync.Consumer<FtdiLineParams> line =
		CallSync.consumer(FtdiLineParams.DEFAULT, true);
	public final CallSync.Consumer<FtdiFlowControl> flowControl =
		CallSync.consumer(FtdiFlowControl.disabled, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Supplier<Integer> pins = CallSync.supplier(0);
	public final CallSync.Supplier<Integer> modem = CallSync.supplier(0);
	public final CallSync.Consumer<Integer> latency = CallSync.consumer(0, true); // in + out
	public final CallSync.Consumer<Integer> chunk = CallSync.consumer(0, true); // in + out
	public final CallSync.Runnable purgeBuffer = CallSync.runnable(true);
	// List<?> = buffer, len
	public final CallSync.Function<List<?>, FtdiTransferControl> submit =
		CallSync.function(List.of(), FtdiTransferControl.NULL);
	// List<?> = callback, packetsPerTransfer, numTransfers, progressIntervalSec
	public final CallSync.Consumer<List<?>> stream = CallSync.consumer(List.of(), true);

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

	public void echoPins() {
		writeOverride((b, off, len) -> {
			in.to.write(b, off, len);
			if (len > 0) pins.autoResponses(b[off + len - 1] & 0xff);
		});
	}

	@Override
	public void reset() {
		super.reset();
		CallSync.resetAll(descriptor, usbReset, bitMode, baud, line, flowControl, dtr, rts, pins,
			modem, latency, chunk, purgeBuffer);
	}

	@Override
	public ftdi_usb_strings descriptor() throws IOException {
		return descriptor.get(IO_ADAPTER);
	}

	@Override
	public void usbReset() throws IOException {
		usbReset.run(IO_ADAPTER);
	}

	@Override
	public void bitMode(FtdiBitMode bitMode) throws IOException {
		this.bitMode.accept(bitMode, IO_ADAPTER);
		verifyConnected();
	}

	@Override
	public void baud(int baud) throws IOException {
		this.baud.accept(baud, IO_ADAPTER);
	}

	@Override
	public void line(FtdiLineParams line) throws IOException {
		this.line.accept(line, IO_ADAPTER);
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws IOException {
		this.flowControl.accept(flowControl, IO_ADAPTER);
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
	public int readPins() throws IOException {
		int n = pins.get(IO_ADAPTER);
		verifyConnected();
		return n;
	}

	@Override
	public int pollModemStatus() throws IOException {
		int n = modem.get(IO_ADAPTER);
		verifyConnected();
		return n;
	}

	@Override
	public void latencyTimer(int latency) throws IOException {
		this.latency.accept(latency, IO_ADAPTER);
	}

	@Override
	public int latencyTimer() throws IOException {
		return latency.value();
	}

	@Override
	public void readChunkSize(int size) throws IOException {
		chunk.accept(size, IO_ADAPTER);
	}

	@Override
	public int readChunkSize() throws IOException {
		return chunk.value();
	}

	@Override
	public void writeChunkSize(int size) throws IOException {
		chunk.accept(size, IO_ADAPTER);
	}

	@Override
	public int writeChunkSize() throws IOException {
		return chunk.value();
	}

	@Override
	public void purgeReadBuffer() throws IOException {
		purgeBuffer.run(IO_ADAPTER);
	}

	@Override
	public void purgeWriteBuffer() throws IOException {
		purgeBuffer.run(IO_ADAPTER);
	}

	@Override
	public FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException {
		return submit.apply(List.of(buffer, len), IO_ADAPTER);
	}

	@Override
	public FtdiTransferControl writeSubmit(Pointer buffer, int len) throws IOException {
		return submit.apply(List.of(buffer, len), IO_ADAPTER);
	}

	@Override
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws IOException {
		stream.accept(List.of(callback, packetsPerTransfer, numTransfers, progressIntervalSec),
			IO_ADAPTER);
	}
}
