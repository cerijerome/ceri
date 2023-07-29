package ceri.serial.ftdi.util;

import java.io.IOException;
import com.sun.jna.Pointer;
import ceri.log.io.SelfHealingConnector;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiDevice;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiTransferControl;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * A self-healing ftdi device. It will automatically reconnect if the cable is removed and
 * reinserted.
 */
public class SelfHealingFtdi extends SelfHealingConnector<Ftdi> implements Ftdi.Fixable {
	private final SelfHealingFtdiConfig config;
	private final FtdiConfig.Builder ftdiConfig;

	public static SelfHealingFtdi of(SelfHealingFtdiConfig config) {
		return new SelfHealingFtdi(config);
	}

	private SelfHealingFtdi(SelfHealingFtdiConfig config) {
		super(config.selfHealing);
		this.config = config;
		ftdiConfig = FtdiConfig.builder(config.ftdi);
		start();
	}

	@Override
	public ftdi_usb_strings descriptor() throws IOException {
		return device.applyIfSet(Ftdi::descriptor, ftdi_usb_strings.NULL);
	}

	@Override
	public void usbReset() throws IOException {
		device.acceptValid(Ftdi::usbReset);
	}

	@Override
	public void bitMode(FtdiBitMode bitMode) throws IOException {
		ftdiConfig.bitMode(bitMode);
		device.acceptValid(f -> f.bitMode(bitMode));
	}

	@Override
	public void baud(int baud) throws IOException {
		ftdiConfig.baud(baud);
		device.acceptValid(f -> f.baud(baud));
	}

	@Override
	public void line(FtdiLineParams params) throws IOException {
		ftdiConfig.params(params);
		device.acceptValid(f -> f.line(params));
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws IOException {
		ftdiConfig.flowControl(flowControl);
		device.acceptValid(f -> f.flowControl(flowControl));
	}

	@Override
	public void dtr(boolean state) throws IOException {
		device.acceptValid(f -> f.dtr(state));
	}

	@Override
	public void rts(boolean state) throws IOException {
		device.acceptValid(f -> f.rts(state));
	}

	@Override
	public int readPins() throws IOException {
		return device.applyValid(Ftdi::readPins);
	}

	@Override
	public int pollModemStatus() throws IOException {
		return device.applyValid(Ftdi::pollModemStatus);
	}

	@Override
	public void latencyTimer(int latency) throws IOException {
		ftdiConfig.latencyTimer(latency);
		device.acceptValid(f -> f.latencyTimer(latency));
	}

	@Override
	public int latencyTimer() throws IOException {
		return device.applyValid(Ftdi::latencyTimer);
	}

	@Override
	public void readChunkSize(int size) throws IOException {
		ftdiConfig.readChunkSize(size);
		device.acceptValid(f -> f.readChunkSize(size));
	}

	@Override
	public int readChunkSize() throws IOException {
		return device.applyValid(Ftdi::readChunkSize);
	}

	@Override
	public void writeChunkSize(int size) throws IOException {
		ftdiConfig.writeChunkSize(size);
		device.acceptValid(f -> f.writeChunkSize(size));
	}

	@Override
	public int writeChunkSize() throws IOException {
		return device.applyValid(Ftdi::writeChunkSize);
	}

	@Override
	public void purgeReadBuffer() throws IOException {
		device.acceptValid(Ftdi::purgeReadBuffer);
	}

	@Override
	public void purgeWriteBuffer() throws IOException {
		device.acceptValid(Ftdi::purgeWriteBuffer);
	}

	@Override
	public FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException {
		return device.applyValid(f -> f.readSubmit(buffer, len));
	}

	@Override
	public FtdiTransferControl writeSubmit(Pointer buffer, int len) throws IOException {
		return device.applyValid(f -> f.writeSubmit(buffer, len));
	}

	@Override
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws IOException {
		device.acceptValid(
			f -> f.readStream(callback, packetsPerTransfer, numTransfers, progressIntervalSec));
	}

	@Override
	protected Ftdi openConnector() throws IOException {
		FtdiDevice ftdi = null;
		try {
			ftdi = FtdiDevice.open(config.finder, config.iface);
			ftdiConfig.build().apply(ftdi);
			return ftdi;
		} catch (RuntimeException | LibUsbException e) {
			LogUtil.close(ftdi);
			throw e;
		}
	}

}
