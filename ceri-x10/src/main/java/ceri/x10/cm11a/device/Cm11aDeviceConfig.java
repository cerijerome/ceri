package ceri.x10.cm11a.device;

import java.util.Objects;
import ceri.common.text.ToString;

public class Cm11aDeviceConfig {
	public static final Cm11aDeviceConfig DEFAULT = builder().build();
	public final int maxSendAttempts;
	public final int queuePollTimeoutMs;
	public final int readPollMs;
	public final int readTimeoutMs;
	public final int errorDelayMs;
	public final int queueSize;

	public static class Builder {
		int maxSendAttempts = 3;
		int queuePollTimeoutMs = 50;
		int readPollMs = 20;
		int readTimeoutMs = 3000;
		int errorDelayMs = 1000;
		int queueSize = 100;

		Builder() {}

		public Builder maxSendAttempts(int maxSendAttempts) {
			this.maxSendAttempts = maxSendAttempts;
			return this;
		}

		public Builder queuePollTimeoutMs(int queuePollTimeoutMs) {
			this.queuePollTimeoutMs = queuePollTimeoutMs;
			return this;
		}

		public Builder readPollMs(int readPollMs) {
			this.readPollMs = readPollMs;
			return this;
		}

		public Builder readTimeoutMs(int readTimeoutMs) {
			this.readTimeoutMs = readTimeoutMs;
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

		public Cm11aDeviceConfig build() {
			return new Cm11aDeviceConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Cm11aDeviceConfig(Builder builder) {
		maxSendAttempts = builder.maxSendAttempts;
		queuePollTimeoutMs = builder.queuePollTimeoutMs;
		readPollMs = builder.readPollMs;
		readTimeoutMs = builder.readTimeoutMs;
		errorDelayMs = builder.errorDelayMs;
		queueSize = builder.queueSize;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxSendAttempts, queuePollTimeoutMs, readPollMs, readTimeoutMs,
			errorDelayMs, queueSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cm11aDeviceConfig)) return false;
		Cm11aDeviceConfig other = (Cm11aDeviceConfig) obj;
		if (maxSendAttempts != other.maxSendAttempts) return false;
		if (queuePollTimeoutMs != other.queuePollTimeoutMs) return false;
		if (readPollMs != other.readPollMs) return false;
		if (readTimeoutMs != other.readTimeoutMs) return false;
		if (errorDelayMs != other.errorDelayMs) return false;
		if (queueSize != other.queueSize) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, maxSendAttempts, queuePollTimeoutMs, readPollMs,
			readTimeoutMs, errorDelayMs, queueSize);
	}

}
