package ceri.serial.ftdi.util;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionUtil;
import ceri.common.io.StateChange;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiConnector;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * A self-healing ftdi device. It will automatically reconnect if the cable is removed and
 * reinserted.
 */
public class SelfHealingFtdiConnector extends LoopingExecutor implements FtdiConnector {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingFtdiConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile Ftdi ftdi;

	public static SelfHealingFtdiConnector of(SelfHealingFtdiConfig config) {
		return new SelfHealingFtdiConnector(config);
	}

	private SelfHealingFtdiConnector(SelfHealingFtdiConfig config) {
		this.config = config;
		start();
	}

	/**
	 * Manually notify the device it is broken. Useful if the device cannot determine it is broken
	 * from IOExceptions alone.
	 */
	@Override
	public void broken() {
		setBroken();
	}

	/**
	 * Attempts to open the ftdi device. If it fails, self-healing will kick in.
	 */
	@Override
	public void connect() throws LibUsbException {
		try {
			initFtdi();
		} catch (LibUsbException e) {
			broken();
			throw e;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void bitmode(FtdiBitMode bitmode) throws LibUsbException {
		exec(ftdi -> ftdi.bitMode(bitmode));
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws LibUsbException {
		exec(ftdi -> ftdi.flowControl(flowControl));
	}

	@Override
	public void dtr(boolean state) throws LibUsbException {
		exec(ftdi -> ftdi.dtr(state));
	}

	@Override
	public void rts(boolean state) throws LibUsbException {
		exec(ftdi -> ftdi.rts(state));
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return execReturn(ftdi -> ftdi.read(buffer, offset, length));
	}

	@Override
	public int readPins() throws LibUsbException {
		return execReturn(Ftdi::readPins);
	}

	@Override
	public int write(byte[] data, int offset, int len) throws LibUsbException {
		return execReturn(ftdi -> ftdi.write(data, offset, len));
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
		fixFtdi();
		logger.info("Ftdi is now fixed");
		ConcurrentUtil.delay(config.recoveryDelayMs); // wait for clients to recover before clearing
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixFtdi() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initFtdi();
				break;
			} catch (LibUsbException e) {
				if (exceptions.add(e)) logger.error("Failed to fix ftdi, retrying:", e);
				ConcurrentUtil.delay(config.fixRetryDelayMs);
			}
		}
	}

	private void notifyListeners(StateChange state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	private void initFtdi() throws LibUsbException {
		LogUtil.close(logger, ftdi);
		ftdi = openFtdi();
	}

	private Ftdi openFtdi() throws LibUsbException {
		Ftdi ftdi = null;
		try {
			ftdi = Ftdi.open(config.finder, config.iface);
			ftdi.bitMode(config.bitMode);
			ftdi.baudRate(config.baud);
			ftdi.lineParams(config.line);
			return ftdi;
		} catch (RuntimeException | LibUsbException e) {
			LogUtil.close(logger, ftdi);
			throw e;
		}
	}

	private void exec(ExceptionConsumer<LibUsbException, Ftdi> consumer) throws LibUsbException {
		execReturn(FunctionUtil.asFunction(consumer));
	}

	@SuppressWarnings("resource")
	private <T> T execReturn(ExceptionFunction<LibUsbException, Ftdi, T> fn)
		throws LibUsbException {
		try {
			return fn.apply(ftdi());
		} catch (RuntimeException | LibUsbException e) {
			checkIfBroken(e);
			throw e;
		}
	}

	private Ftdi ftdi() throws LibUsbException {
		Ftdi ftdi = this.ftdi;
		if (ftdi != null) return ftdi;
		throw LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "Ftdi device is not available");
	}

}
