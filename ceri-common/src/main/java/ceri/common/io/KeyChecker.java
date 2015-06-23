package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Asynchronously checks input from a stream, and interrupts the given thread in the input matches.
 * Useful for manually stopping long running threads without killing the process.
 */
public class KeyChecker implements Closeable {
	private static final long POLL_MS = 500;
	private final Thread threadToInterrupt;
	private final ScheduledExecutorService executor;
	private final Function<String, Boolean> checkFunction;
	private final InputStream in;

	public KeyChecker() {
		this(input -> true);
	}

	public KeyChecker(Function<String, Boolean> checkFunction) {
		this(checkFunction, POLL_MS);
	}

	public KeyChecker(Function<String, Boolean> checkFunction, Long pollMs) {
		this(checkFunction, pollMs, System.in);
	}

	public KeyChecker(Function<String, Boolean> checkFunction, Long pollMs, InputStream in) {
		this(checkFunction, pollMs, in, Thread.currentThread());
	}

	public KeyChecker(Function<String, Boolean> checkFunction, Long pollMs, InputStream in,
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
			System.out.println("input=\"" + input + "\"");
			if (!checkFunction.apply(input)) return;
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
