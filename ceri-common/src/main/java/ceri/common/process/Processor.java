package ceri.common.process;

import static ceri.common.process.ProcessUtil.stdErr;
import static ceri.common.time.TimeSupplier.millis;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import ceri.common.data.ByteArray;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionSupplier;
import ceri.common.io.IoUtil;
import ceri.common.text.StringUtil;
import ceri.common.time.Timer;

public class Processor {
	public static final Processor DEFAULT = builder().build();
	public static final Processor LONG_RUNNING = builder().noTimeout().captureStdOut(false).build();
	public static final Processor IGNORE_EXIT_VALUE = builder().verifyExitValue(false).build();
	private final Function<Parameters, ExceptionSupplier<IOException, Process>> processStarter;
	private final int pollMs;
	private final int timeoutMs;
	private final boolean captureStdOut;
	private final boolean verifyExitValue;
	private final boolean verifyErr;

	public static class Builder {
		// Allows tests to set process without sacrificing code coverage
		Function<Parameters, ExceptionSupplier<IOException, Process>> processStarter =
			p -> new ProcessBuilder(p.list())::start;
		int pollMs = 50;
		int timeoutMs = 5000;
		boolean captureStdOut = true;
		boolean verifyExitValue = true;
		boolean verifyErr = true;

		Builder() {}

		public Builder processStarter(
			Function<Parameters, ExceptionSupplier<IOException, Process>> processStarter) {
			this.processStarter = processStarter;
			return this;
		}

		/**
		 * Specify poll timeout for reading/emptying output buffer.
		 */
		public Builder pollMs(int pollMs) {
			this.pollMs = pollMs;
			return this;
		}

		/**
		 * Specify process timeout.
		 */
		public Builder timeoutMs(int timeoutMs) {
			this.timeoutMs = timeoutMs;
			return this;
		}

		/**
		 * Specify no process timeout.
		 */
		public Builder noTimeout() {
			this.timeoutMs = (int) Timer.INFINITE.period;
			return this;
		}

		/**
		 * Captures and returns stdout.
		 */
		public Builder captureStdOut(boolean captureStdOut) {
			this.captureStdOut = captureStdOut;
			return this;
		}

		/**
		 * Checks for non-zero exit code.
		 */
		public Builder verifyExitValue(boolean verifyExitValue) {
			this.verifyExitValue = verifyExitValue;
			return this;
		}

		/**
		 * Checks for no output to stderr.
		 */
		public Builder verifyErr(boolean verifyErr) {
			this.verifyErr = verifyErr;
			return this;
		}

		public Processor build() {
			return new Processor(this);
		}
	}

	public static Builder builder(Processor processor) {
		return new Builder().processStarter(processor.processStarter).timeoutMs(processor.timeoutMs)
			.verifyExitValue(processor.verifyExitValue).verifyErr(processor.verifyErr);
	}

	public static Builder builder() {
		return new Builder();
	}

	protected Processor(Builder builder) {
		processStarter = builder.processStarter;
		pollMs = builder.pollMs;
		timeoutMs = builder.timeoutMs;
		captureStdOut = builder.captureStdOut;
		verifyExitValue = builder.verifyExitValue;
		verifyErr = builder.verifyErr;
	}

	public String exec(String... parameters) throws IOException {
		return exec(Parameters.ofAll(parameters));
	}

	public String exec(Parameters parameters) throws IOException {
		if (parameters.list().isEmpty()) return null;
		Process process = null;
		try {
			process = processStarter.apply(parameters).get();
			return exec(process, parameters);
		} finally {
			if (process != null) process.destroyForcibly();
		}
	}

	private String exec(Process process, Parameters parameters) throws IOException {
		String stdOut = waitFor(process, parameters);
		verifyErr(process);
		verifyExitValue(process);
		return stdOut;
	}

	private String waitFor(Process process, Parameters parameters) throws IOException {
		if (captureStdOut) return waitForCapture(process, parameters);
		waitForProcess(process, parameters, IoUtil::clear);
		return "";
	}

	private String waitForCapture(Process process, Parameters parameters) throws IOException {
		var buffer = ByteArray.Encoder.of();
		waitForProcess(process, parameters, buffer::transferAvailableFrom);
		return buffer.immutable().getString(0);
	}

	@SuppressWarnings("resource")
	private void waitForProcess(Process process, Parameters parameters,
		ExceptionConsumer<IOException, InputStream> consumer) throws IOException {
		InputStream in = process.getInputStream();
		Timer timer = Timer.of(timeoutMs, millis).start();
		while (true) {
			consumer.accept(in);
			if (!process.isAlive()) return;
			if (timer.snapshot().expired())
				throw new IOException("Failed to complete in " + timeoutMs + "ms: " + parameters);
			millis.delay(pollMs);
		}
	}

	private void verifyErr(Process process) throws IOException {
		if (!verifyErr) return;
		String err = stdErr(process);
		if (!StringUtil.blank(err)) throw new IOException(err.trim());
	}

	private void verifyExitValue(Process process) throws IOException {
		if (!verifyExitValue) return;
		int exitValue = process.exitValue();
		if (exitValue != 0) throw new IOException("Exit value: " + exitValue);
	}
}
