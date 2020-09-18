package ceri.common.process;

import static ceri.common.process.ProcessUtil.stdErr;
import static ceri.common.process.ProcessUtil.stdOut;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.text.StringUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Processor {
	public static final Processor DEFAULT = builder().build();
	public static final Processor LONG_RUNNING = builder().noTimeout().captureStdOut(false).build();
	public static final Processor IGNORE_EXIT_VALUE =
		Processor.builder().verifyExitValue(false).build();
	private final Integer timeoutMs;
	private final boolean captureStdOut;
	private final boolean verifyExitValue;
	private final boolean verifyErr;

	public static class Builder {
		Integer timeoutMs = 5000;
		boolean captureStdOut = true;
		boolean verifyExitValue = true;
		boolean verifyErr = true;

		Builder() {}

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
		return new Builder().timeoutMs(processor.timeoutMs)
			.verifyExitValue(processor.verifyExitValue).verifyErr(processor.verifyErr);
	}

	public static Builder builder() {
		return new Builder();
	}

	Processor(Builder builder) {
		timeoutMs = builder.timeoutMs;
		captureStdOut = builder.captureStdOut;
		verifyExitValue = builder.verifyExitValue;
		verifyErr = builder.verifyErr;
	}

	public String exec(String... parameters) throws IOException {
		return exec(Parameters.ofAll(parameters));
	}

	public String exec(Parameters parameters) throws IOException {
		return exec(new ProcessBuilder(parameters.list()));
	}

	public String exec(ProcessBuilder builder) throws IOException {
		return exec(ProcessCommand.of(builder));
	}

	public String exec(ProcessCommand command) throws IOException {
		if (command.command().isEmpty()) return null;
		Process process = null;
		try {
			process = command.start();
			return exec(process, command);
		} finally {
			if (process != null) process.destroyForcibly();
		}
	}

	private String exec(Process process, ProcessCommand command) throws IOException {
		verifyTimeout(process, command);
		String stdOut = captureStdOut ? stdOut(process) : null;
		verifyErr(process);
		verifyExitValue(process);
		return stdOut;
	}

	private void verifyTimeout(Process process, ProcessCommand command) throws IOException {
		if (timeoutMs != null && timeoutMs == 0) return;
		try {
			if (timeoutMs == null) process.waitFor();
			else if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) throw new IOException(
				"Failed to complete in " + timeoutMs + "ms: " + command.asString());
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	private void verifyErr(Process process) throws IOException {
		if (!verifyErr) return;
		String err = stdErr(process);
		if (!StringUtil.isBlank(err)) throw new IOException(err.trim());
	}

	private void verifyExitValue(Process process) throws IOException {
		if (!verifyExitValue) return;
		int exitValue = process.exitValue();
		if (exitValue != 0) throw new IOException("Exit value: " + exitValue);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(timeoutMs, captureStdOut, verifyExitValue, verifyErr);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Processor)) return false;
		Processor other = (Processor) obj;
		if (!EqualsUtil.equals(timeoutMs, other.timeoutMs)) return false;
		if (captureStdOut != other.captureStdOut) return false;
		if (verifyExitValue != other.verifyExitValue) return false;
		if (verifyErr != other.verifyErr) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, timeoutMs, captureStdOut, verifyExitValue, verifyErr).toString();
	}

}
