package ceri.x10.cm17a.device;

import java.util.Objects;
import ceri.common.text.ToString;

/**
 * Handles all communication with the device. Reads commands from the input queue, and dispatches
 * notifications to the output queue.
 */
public class Cm17aDeviceConfig {
	public static final Cm17aDeviceConfig NULL = builder().commandIntervalMicros(0)
		.resetIntervalMicros(0).waitIntervalMicros(0).queuePollTimeoutMs(0).errorDelayMs(0).build();
	public static final Cm17aDeviceConfig DEFAULT = builder().build();
	public final int queuePollTimeoutMs;
	public final int waitIntervalMicros;
	public final int resetIntervalMicros;
	public final int commandIntervalMicros;
	public final int errorDelayMs;
	public final int queueSize;

	public static class Builder {
		int queuePollTimeoutMs = 10000;
		int waitIntervalMicros = 800;
		int resetIntervalMicros = 10000;
		int commandIntervalMicros = 1000;
		int errorDelayMs = 1000;
		int queueSize = 100;

		Builder() {}

		public Builder queuePollTimeoutMs(int queuePollTimeoutMs) {
			this.queuePollTimeoutMs = queuePollTimeoutMs;
			return this;
		}

		public Builder waitIntervalMicros(int waitIntervalMicros) {
			this.waitIntervalMicros = waitIntervalMicros;
			return this;
		}

		public Builder resetIntervalMicros(int resetIntervalMicros) {
			this.resetIntervalMicros = resetIntervalMicros;
			return this;
		}

		public Builder commandIntervalMicros(int commandIntervalMicros) {
			this.commandIntervalMicros = commandIntervalMicros;
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
		queuePollTimeoutMs = builder.queuePollTimeoutMs;
		waitIntervalMicros = builder.waitIntervalMicros;
		resetIntervalMicros = builder.resetIntervalMicros;
		commandIntervalMicros = builder.commandIntervalMicros;
		errorDelayMs = builder.errorDelayMs;
		queueSize = builder.queueSize;
	}

	@Override
	public int hashCode() {
		return Objects.hash(queuePollTimeoutMs, waitIntervalMicros, resetIntervalMicros,
			commandIntervalMicros, errorDelayMs, queueSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cm17aDeviceConfig)) return false;
		Cm17aDeviceConfig other = (Cm17aDeviceConfig) obj;
		if (queuePollTimeoutMs != other.queuePollTimeoutMs) return false;
		if (waitIntervalMicros != other.waitIntervalMicros) return false;
		if (resetIntervalMicros != other.resetIntervalMicros) return false;
		if (commandIntervalMicros != other.commandIntervalMicros) return false;
		if (errorDelayMs != other.errorDelayMs) return false;
		if (queueSize != other.queueSize) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, queuePollTimeoutMs, waitIntervalMicros, resetIntervalMicros,
			commandIntervalMicros, errorDelayMs, queueSize);
	}
}
