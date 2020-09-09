package ceri.x10.cm17a.device;

import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Cm17aDeviceConfig {
	public static final Cm17aDeviceConfig DEFAULT = builder().build();
	public final int pollTimeoutMs;
	public final int waitIntervalMs;
	public final int resetIntervalMs;
	public final int commandIntervalMs;
	public final int errorDelayMs;
	public final int queueSize;

	public static class Builder {
		int pollTimeoutMs = 10000;
		int waitIntervalMs = 1;
		int resetIntervalMs = 10;
		int commandIntervalMs = 1000;
		int errorDelayMs = 1000;
		int queueSize = 100;

		Builder() {}

		public Builder pollTimeoutMs(int pollTimeoutMs) {
			this.pollTimeoutMs = pollTimeoutMs;
			return this;
		}

		public Builder waitIntervalMs(int waitIntervalMs) {
			this.waitIntervalMs = waitIntervalMs;
			return this;
		}

		public Builder resetIntervalMs(int resetIntervalMs) {
			this.resetIntervalMs = resetIntervalMs;
			return this;
		}

		public Builder commandIntervalMs(int commandIntervalMs) {
			this.commandIntervalMs = commandIntervalMs;
			return this;
		}

		public Builder errorDelayMs(int errorDelayMs) {
			this.errorDelayMs = errorDelayMs;
			return this;
		}

		public Builder queueSize(int queueSize) {
			this.queueSize = queueSize;
			return this;
		}

		public Cm17aDeviceConfig build() {
			return new Cm17aDeviceConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Cm17aDeviceConfig(Builder builder) {
		pollTimeoutMs = builder.pollTimeoutMs;
		waitIntervalMs = builder.waitIntervalMs;
		resetIntervalMs = builder.resetIntervalMs;
		commandIntervalMs = builder.commandIntervalMs;
		errorDelayMs = builder.errorDelayMs;
		queueSize = builder.queueSize;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(pollTimeoutMs, waitIntervalMs, resetIntervalMs, commandIntervalMs,
			errorDelayMs, queueSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cm17aDeviceConfig)) return false;
		Cm17aDeviceConfig other = (Cm17aDeviceConfig) obj;
		if (pollTimeoutMs != other.pollTimeoutMs) return false;
		if (waitIntervalMs != other.waitIntervalMs) return false;
		if (resetIntervalMs != other.resetIntervalMs) return false;
		if (commandIntervalMs != other.commandIntervalMs) return false;
		if (errorDelayMs != other.errorDelayMs) return false;
		if (queueSize != other.queueSize) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, pollTimeoutMs, waitIntervalMs, resetIntervalMs,
			commandIntervalMs, errorDelayMs, queueSize).toString();
	}
}
