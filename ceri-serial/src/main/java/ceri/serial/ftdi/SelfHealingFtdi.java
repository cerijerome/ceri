package ceri.serial.ftdi;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import java.nio.ByteBuffer;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionUtil;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.Ftdi.StreamCallback;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_flow_control;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_string_descriptors;
import ceri.serial.libusb.UsbDevice;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

/**
 * A self-healing ftdi device. It will automatically reconnect if the cable is removed and
 * reinserted.
 */
public class SelfHealingFtdi extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private final libusb_device_criteria find;
	private final ftdi_interface iface;
	private final Integer baud;
	private final FtdiLineProperties line;
	private final FtdiBitmode bitmode;
	private final int fixRetryDelayMs;
	private final int recoveryDelayMs;
	private final Predicate<Exception> brokenPredicate;
	private final Listeners<State> listeners = new Listeners<>();
	private final BooleanCondition sync = BooleanCondition.create();
	private volatile Ftdi ftdi;

	public static class Builder {
		libusb_device_criteria find;
		ftdi_interface iface;
		Integer baud;
		FtdiLineProperties line;
		FtdiBitmode bitmode;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = SelfHealingFtdi::isBroken;

		Builder(libusb_device_criteria find) {
			this.find = find;
		}

		public Builder iface(ftdi_interface iface) {
			this.iface = iface;
			return this;
		}

		public Builder baud(int baud) {
			this.baud = baud;
			return this;
		}

		public Builder line(FtdiLineProperties line) {
			this.line = line;
			return this;
		}

		public Builder bitmode(FtdiBitmode bitmode) {
			this.bitmode = bitmode;
			return this;
		}

		public Builder bitmode(ftdi_mpsse_mode mode) {
			this.bitmode = FtdiBitmode.of(mode);
			return this;
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public SelfHealingFtdi build() {
			return new SelfHealingFtdi(this);
		}
	}

	public static Builder builder(libusb_device_criteria find) {
		return new Builder(find);
	}

	SelfHealingFtdi(Builder builder) {
		find = builder.find;
		iface = builder.iface;
		baud = builder.baud;
		line = builder.line;
		bitmode = builder.bitmode;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
		start();
	}

	public static enum State {
		fixed,
		broken;
	}

	/**
	 * Manually notify the device it is broken. Useful if the device cannot determine it is broken
	 * from IOExceptions alone.
	 */
	public void broken() {
		setBroken();
	}

	/**
	 * Attempts to open the ftdi device. If it fails, self-healing will kick in.
	 */
	public void open() throws LibUsbException {
		try {
			initFtdi();
		} catch (LibUsbException e) {
			broken();
			throw e;
		}
	}

	/**
	 * Attempts to open the ftdi device. If it fails, the error will be logged, and self-healing
	 * will kick in. Returns true if open was successful.
	 */
	public boolean openQuietly() {
		try {
			initFtdi();
			return true;
		} catch (LibUsbException e) {
			logger.catching(e);
			broken();
			return false;
		}
	}

	public Listenable<State> listeners() {
		return listeners;
	}

	public ftdi_string_descriptors usbStrings(UsbDevice dev) throws LibUsbException {
		return execReturn(ftdi -> ftdi.usbStrings(dev));
	}

	public void reset() throws LibUsbException {
		exec(ftdi -> ftdi.reset());
	}

	public void purgeRxBuffer() throws LibUsbException {
		exec(ftdi -> ftdi.purgeRxBuffer());
	}

	public void purgeTxBuffer() throws LibUsbException {
		exec(ftdi -> ftdi.purgeTxBuffer());
	}

	public void purgeBuffers() throws LibUsbException {
		exec(ftdi -> ftdi.purgeBuffers());
	}

	public void baudrate(int baudrate) throws LibUsbException {
		exec(ftdi -> ftdi.baudrate(baudrate));
	}

	public void lineProperty(FtdiLineProperties properties) throws LibUsbException {
		exec(ftdi -> ftdi.lineProperty(properties));
	}

	public int write(int... data) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(data));
	}

	public int write(byte[] data) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(data));
	}

	public int write(byte[] data, int offset) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(data, offset));
	}

	public int write(byte[] data, int offset, int len) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(data, offset, len));
	}

	public int write(ByteBuffer buffer) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(buffer));
	}

	public int write(ByteBuffer buffer, int len) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(buffer, len));
	}

	public int read() throws LibUsbException {
		return execReturn(ftdi -> ftdi.read());
	}

	public byte[] read(int size) throws LibUsbException {
		return execReturn(ftdi -> ftdi.read(size));
	}

	public int read(ByteBuffer buffer) throws LibUsbException {
		return execReturn(ftdi -> ftdi.read(buffer));
	}

	public int read(ByteBuffer buffer, int size) throws LibUsbException {
		return execReturn(ftdi -> ftdi.read(buffer, size));
	}

	public FtdiTransferControl writeSubmit(Pointer buf, int size) throws LibUsbException {
		return execReturn(ftdi -> ftdi.writeSubmit(buf, size));
	}

	public FtdiTransferControl readSubmit(Pointer buf, int size) throws LibUsbException {
		return execReturn(ftdi -> ftdi.readSubmit(buf, size));
	}

	public <T> void readStream(StreamCallback<T> callback, T userData, int packetsPerTransfer,
		int numTransfers) throws LibUsbException {
		exec(ftdi -> ftdi.readStream(callback, userData, packetsPerTransfer, numTransfers));
	}

	public void readChunkSize(int chunkSize) throws LibUsbException {
		exec(ftdi -> ftdi.readChunkSize(chunkSize));
	}

	public void bitmode(FtdiBitmode bitmode) throws LibUsbException {
		exec(ftdi -> ftdi.bitmode(bitmode));
	}

	public int readPins() throws LibUsbException {
		return execReturn(ftdi -> ftdi.readPins());
	}

	public void latencyTimer(int latency) throws LibUsbException {
		exec(ftdi -> ftdi.latencyTimer(latency));
	}

	public int latencyTimer() throws LibUsbException {
		return execReturn(ftdi -> ftdi.latencyTimer());
	}

	public int pollModemStatus() throws LibUsbException {
		return execReturn(ftdi -> ftdi.pollModemStatus());
	}

	public void flowCtrl(ftdi_flow_control flowCtrl) throws LibUsbException {
		exec(ftdi -> ftdi.flowCtrl(flowCtrl));
	}

	public void dtr(boolean state) throws LibUsbException {
		exec(ftdi -> ftdi.dtr(state));
	}

	public void rts(boolean state) throws LibUsbException {
		exec(ftdi -> ftdi.rts(state));
	}

	public void dtrRts(boolean dtr, boolean rts) throws LibUsbException {
		exec(ftdi -> ftdi.dtrRts(dtr, rts));
	}

	public void eventChar(char eventch, boolean enable) throws LibUsbException {
		exec(ftdi -> ftdi.eventChar(eventch, enable));
	}

	public void errorChar(char errorch, boolean enable) throws LibUsbException {
		exec(ftdi -> ftdi.errorChar(errorch, enable));
	}

	private void exec(ExceptionConsumer<LibUsbException, Ftdi> consumer) throws LibUsbException {
		execReturn(FunctionUtil.asFunction(consumer));
	}

	@SuppressWarnings("resource")
	private <T> T execReturn(ExceptionFunction<LibUsbException, Ftdi, T> fn)
		throws LibUsbException {
		Ftdi ftdi = ftdi();
		try {
			return fn.apply(ftdi);
		} catch (RuntimeException | LibUsbException e) {
			checkIfBroken(e);
			throw e;
		}
	}

	private Ftdi ftdi() throws LibUsbException {
		Ftdi ftdi = this.ftdi;
		if (ftdi != null) return ftdi;
		throw new LibUsbException("Ftdi device is not available", LIBUSB_ERROR_NO_DEVICE);
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, ftdi);
	}

	/**
	 * Checks if an exception thrown from ftdi activities means the device needs to reconnect.
	 */
	public static boolean isBroken(Exception e) {
		if (e == null) return false;
		return Ftdi.isFatal(BasicUtil.castOrNull(LibUsbException.class, e));
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Ftdi is broken - attempting to fix");
		String lastErrorMsg = null;
		while (true) {
			try {
				initFtdi();
				break;
			} catch (LibUsbException e) {
				String errorMsg = e.getMessage();
				if (lastErrorMsg == null || !lastErrorMsg.equals(errorMsg))
					logger.debug("Failed to fix ftdi, retrying: {}", errorMsg);
				lastErrorMsg = errorMsg;
				BasicUtil.delay(fixRetryDelayMs);
			}
		}
		logger.info("Ftdi is now fixed");
		BasicUtil.delay(recoveryDelayMs); // wait for clients to recover before clearing
		sync.clear();
		notifyListeners(State.fixed);
	}

	private void notifyListeners(State state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void checkIfBroken(Exception e) {
		if (!brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(State.broken);
	}

	private void initFtdi() throws LibUsbException {
		LogUtil.close(logger, ftdi);
		ftdi = openFtdi();
	}

	private Ftdi openFtdi() throws LibUsbException {
		Ftdi ftdi = null;
		try {
			ftdi = Ftdi.create();
			if (iface != null) ftdi.setInterface(iface);
			ftdi.open(find);
			if (bitmode != null) ftdi.bitmode(bitmode);
			if (baud != null) ftdi.baudrate(baud.intValue());
			if (line != null) ftdi.lineProperty(line);
			return ftdi;
		} catch (RuntimeException | LibUsbException e) {
			LogUtil.close(logger, ftdi);
			throw e;
		}
	}

}
