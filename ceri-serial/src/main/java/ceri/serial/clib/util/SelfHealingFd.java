package ceri.serial.clib.util;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
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
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.Seek;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * A self-healing file descriptor. It will automatically be reopened if the resource becomes
 * inaccessible, then accessible again.
 */
public class SelfHealingFd extends LoopingExecutor
	implements FileDescriptor, Listenable.Indirect<StateChange> {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingFdConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile FileDescriptor fd;

	public static SelfHealingFd of(SelfHealingFdConfig config) {
		return new SelfHealingFd(config);
	}

	private SelfHealingFd(SelfHealingFdConfig config) {
		this.config = config;
		start();
	}

	/**
	 * Manually notify the file descriptor it is broken. Useful when unable to determine if broken
	 * from IOExceptions alone.
	 */
	public void broken() {
		setBroken();
	}

	/**
	 * Attempts to open the file. On failure, self-healing will kick in.
	 */
	public void open() throws IOException {
		try {
			initFd();
		} catch (LibUsbException e) {
			broken();
			throw e;
		}
	}

	/**
	 * Attempts to open the file. On failure, the error will be logged, and self-healing will kick
	 * in. Returns true if open was successful.
	 */
	public boolean openQuietly() {
		try {
			initFd();
			return true;
		} catch (IOException e) {
			logger.catching(e);
			broken();
			return false;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	@SuppressWarnings("resource")
	public int fd() throws IOException {
		return fileDescriptor().fd();
	}

	@Override
	public int read(Pointer p, int offset, int length) throws IOException {
		return execReturn(fd -> fd.read(p, offset, length));
	}

	@Override
	public void write(Pointer p, int offset, int length) throws IOException {
		exec(fd -> fd.write(p, offset, length));
	}

	@Override
	public int seek(int offset, Seek whence) throws IOException {
		return execReturn(fd -> fd.seek(offset, whence));
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	@Override
	public int ioctl(String name, int request, Object... objs) throws IOException {
		return execReturn(fd -> fd.ioctl(name, request, objs));
	}

	private void exec(ExceptionConsumer<IOException, FileDescriptor> consumer) throws IOException {
		execReturn(FunctionUtil.asFunction(consumer));
	}

	@SuppressWarnings("resource")
	private <T> T execReturn(ExceptionFunction<IOException, FileDescriptor, T> fn)
		throws IOException {
		try {
			return fn.apply(fileDescriptor());
		} catch (RuntimeException | IOException e) {
			checkIfBroken(e);
			throw e;
		}
	}

	private FileDescriptor fileDescriptor() throws IOException {
		FileDescriptor fd = this.fd;
		if (fd != null) return fd;
		throw new IOException("File descriptor is invalid");
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, fd);
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("File descriptor is invalid - attempting to fix");
		fixFd();
		logger.info("File is now open");
		ConcurrentUtil.delay(config.recoveryDelayMs); // wait for clients to recover before clearing
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixFd() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initFd();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to open file, retrying:", e);
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

	private void initFd() throws IOException {
		LogUtil.close(logger, fd);
		fd = config.open();
	}

}
