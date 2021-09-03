package ceri.common.process;

import static ceri.common.process.ProcessUtil.stdErr;
import static ceri.common.process.ProcessUtil.stdOut;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.ExceptionSupplier;
import ceri.common.text.StringUtil;

public class Processor {
	public static final Processor DEFAULT = builder().build();
	public static final Processor LONG_RUNNING = builder().noTimeout().captureStdOut(false).build();
	public static final Processor IGNORE_EXIT_VALUE =
		Processor.builder().verifyExitValue(false).build();
	private final Function<Parameters, ExceptionSupplier<IOException, Process>> processStarter;
	private final Integer timeoutMs;
	private final boolean captureStdOut;
	private final boolean verifyExitValue;
	private final boolean verifyErr;

	public static class Builder {
		// Allows tests to set process without sacrificing code coverage
		Function<Parameters, ExceptionSupplier<IOException, Process>> processStarter =
			p -> new ProcessBuilder(p.list())::start;
		Integer timeoutMs = 5000;
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
			this.timeoutMs = null;
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
		verifyTimeout(process, parameters);
		String stdOut = captureStdOut ? stdOut(process) : null;
		verifyErr(process);
		verifyExitValue(process);
		return stdOut;
	}

	private void verifyTimeout(Process process, Parameters parameters) throws IOException {
		if (timeoutMs != null && timeoutMs == 0) return;
		try {
			if (timeoutMs == null) process.waitFor();
			else if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS))
				throw new IOException("Failed to complete in " + timeoutMs + "ms: " + parameters);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
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
