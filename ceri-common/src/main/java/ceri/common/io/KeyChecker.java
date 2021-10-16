package ceri.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.function.RuntimeCloseable;

public class KeyChecker implements RuntimeCloseable {
	private final int shutdownTimeoutMs;
	private final Runnable action;
	private final Predicate<String> checkFunction;
	private final InputStream in;
	private final ScheduledExecutorService executor;

	public static KeyChecker of() {
		return builder().build();
	}

	public static class Builder {
		int shutdownTimeoutMs = 1000;
		int pollMs = 500;
		Runnable action = Thread.currentThread()::interrupt;
		Predicate<String> checkFunction = FunctionUtil.truePredicate();
		InputStream in = System.in;

		Builder() {}

		public Builder shutdownTimeoutMs(int shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}

		public Builder pollMs(int pollMs) {
			this.pollMs = pollMs;
			return this;
		}

		public Builder action(Runnable action) {
			this.action = action;
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
		shutdownTimeoutMs = builder.shutdownTimeoutMs;
		action = builder.action;
		checkFunction = builder.checkFunction;
		in = builder.in;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this::checkForInput, 0, builder.pollMs, TimeUnit.MILLISECONDS);
	}

	private void checkForInput() {
		try {
			byte[] b = in.readNBytes(in.available());
			String input = new String(b).trim();
			if (!checkFunction.test(input)) return;
		} catch (IOException e) {
			// ignore error, exit
		}
		action.run();
	}

	@Override
	public void close() {
		ConcurrentUtil.close(executor, shutdownTimeoutMs);
	}

}
