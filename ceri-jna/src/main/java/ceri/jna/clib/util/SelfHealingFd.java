package ceri.jna.clib.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.util.BasicUtil;
import ceri.jna.clib.FileDescriptor;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A self-healing file descriptor. It will automatically be reopened if the resource becomes
 * inaccessible, then accessible again.
 */
public class SelfHealingFd extends LoopingExecutor
	implements FileDescriptor, Listenable.Indirect<StateChange> {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingFdConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile FileDescriptor fd = null;

	public static SelfHealingFd of(SelfHealingFdConfig config) {
		return new SelfHealingFd(config);
	}

	private SelfHealingFd(SelfHealingFdConfig config) {
		this.config = config;
		in.listeners().listen(this::checkIfBroken);
		out.listeners().listen(this::checkIfBroken);
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
		} catch (IOException e) {
			broken();
			throw e;
		}
	}

	/**
	 * Attempts to open the file. On failure, the error will be logged, and self-healing will kick
	 * in. Returns true if open was successful.
	 */
	public boolean openQuietly() {
		return LogUtil.execute(logger, this::open);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public <E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E {
		execInt(fd -> {
			fd.accept(consumer);
			return 0;
		});
	}

	@Override
	public <E extends Exception> int applyAsInt(ExceptionIntUnaryOperator<E> operator) throws E {
		return execInt(fd -> fd.applyAsInt(operator));
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, fd);
	}

	@SuppressWarnings("resource")
	private <E extends Exception> int execInt(ExceptionToIntFunction<E, FileDescriptor> fn)
		throws E {
		try {
			return fn.applyAsInt(fileDescriptor());
		} catch (RuntimeException e) {
			checkIfBroken(e);
			throw e;
		} catch (Exception e) {
			checkIfBroken(e);
			throw BasicUtil.<E>uncheckedCast(e);
		}
	}

	private FileDescriptor fileDescriptor() throws IOException {
		FileDescriptor fd = this.fd;
		if (fd != null) return fd;
		throw new IOException("File descriptor is invalid");
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
		if (
			!config.brokenPredicate.test(e)
			||
			sync.isSet())
			return;
		setBroken();
	}

	private void setBroken() {
		notifyListeners(StateChange.broken);
		sync.signal();
	}

	@SuppressWarnings("resource")
	private void initFd() throws IOException {
		LogUtil.close(logger, fd);
		fd = config.open();
		in.setInputStream(fd.in());
		out.setOutputStream(fd.out());
	}

}
