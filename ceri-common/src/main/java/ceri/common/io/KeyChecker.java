package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Asynchronously checks input from a stream, and interrupts the given thread if the input matches.
 * Useful for manually stopping long running threads without killing the process. The check runs at
 * a scheduled interval, and drops historical readings. To check multiple characters it is
 * recommended to have the predicate keep the history each time test() is called.
 */
public class KeyChecker implements Closeable {
	private static final long POLL_MS = 500;
	private final Thread threadToInterrupt;
	private final ScheduledExecutorService executor;
	private final Predicate<String> checkFunction;
	private final InputStream in;

	public static KeyChecker create() {
		return create(null);
	}

	public static KeyChecker create(Predicate<String> checkFunction) {
		return create(checkFunction, null);
	}

	public static KeyChecker create(Predicate<String> checkFunction, Long pollMs) {
		return create(checkFunction, pollMs, System.in);
	}

	public static KeyChecker create(Predicate<String> checkFunction, Long pollMs, InputStream in) {
		return create(checkFunction, pollMs, in, Thread.currentThread());
	}

	public static KeyChecker create(Predicate<String> checkFunction, Long pollMs, InputStream in,
		Thread threadToInterrupt) {
		return new KeyChecker(checkFunction, pollMs, in, threadToInterrupt);
	}

	private KeyChecker(Predicate<String> checkFunction, Long pollMs, InputStream in,
		Thread threadToInterrupt) {
		this.in = in;
		this.threadToInterrupt = threadToInterrupt;
		if (pollMs == null) pollMs = POLL_MS;
		this.checkFunction = checkFunction == null ? (input) -> true : checkFunction;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> checkForInput(), 0, pollMs, TimeUnit.MILLISECONDS);
	}

	private void checkForInput() {
		try {
			int available = in.available();
			if (available <= 0) return;
			byte[] b = new byte[available];
			in.read(b);
			String input = new String(b).trim();
			if (!checkFunction.test(input)) return;
		} catch (IOException e) {
			// ignore error, exit
		}
		threadToInterrupt.interrupt();
	}

	@Override
	public void close() {
		try {
			executor.shutdown();
			executor.awaitTermination(POLL_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

}
