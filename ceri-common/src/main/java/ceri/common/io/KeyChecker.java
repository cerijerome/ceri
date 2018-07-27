package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.FunctionUtil;

public class KeyChecker implements Closeable {
	private static final long POLL_MS = 500;
	private final Thread threadToInterrupt;
	private final Predicate<String> checkFunction;
	private final InputStream in;
	private final ScheduledExecutorService executor;

	public static KeyChecker of() {
		return builder().build();
	}

	public static class Builder {
		long pollMs = POLL_MS;
		Thread threadToInterrupt = Thread.currentThread();
		Predicate<String> checkFunction = FunctionUtil.truePredicate();
		InputStream in = System.in;

		Builder() {}

		public Builder pollMs(long pollMs) {
			this.pollMs = pollMs;
			return this;
		}

		public Builder threadToInterrupt(Thread threadToInterrupt) {
			this.threadToInterrupt = threadToInterrupt;
			return this;
		}

		public Builder checkFunction(Predicate<String> checkFunction) {
			this.checkFunction = checkFunction;
			return this;
		}

		public Builder in(InputStream in) {
			this.in = in;
			return this;
		}

		public KeyChecker build() {
			return new KeyChecker(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	KeyChecker(Builder builder) {
		threadToInterrupt = builder.threadToInterrupt;
		checkFunction = builder.checkFunction;
		in = builder.in;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> checkForInput(), 0, builder.pollMs,
			TimeUnit.MILLISECONDS);
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
		ConcurrentUtil.executeInterruptible(() -> {
			executor.shutdown();
			executor.awaitTermination(POLL_MS, TimeUnit.MILLISECONDS);
		});
	}

}
