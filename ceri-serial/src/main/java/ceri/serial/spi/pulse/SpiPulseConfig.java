package ceri.serial.spi.pulse;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class SpiPulseConfig {
	public final PulseCycle cycle;
	public final int size;
	public final int delayMicros;
	public final int resetDelayMs;

	public static SpiPulseConfig of(int size) {
		return builder(size).build();
	}
	
	public static class Builder {
		final int size;
		PulseCycle cycle = PulseCycles.Std._4_9.cycle;
		int delayMicros = 50;
		int resetDelayMs = 3000;

		Builder(int size) {
			this.size = size;
		}

		public Builder cycle(PulseCycle cycle) {
			this.cycle = cycle;
			return this;
		}

		public Builder delayMicros(int delayMicros) {
			this.delayMicros = delayMicros;
			return this;
		}

		public Builder resetDelayMs(int resetDelayMs) {
			this.resetDelayMs = resetDelayMs;
			return this;
		}

		public SpiPulseConfig build() {
			return new SpiPulseConfig(this);
		}
	}

	public static Builder builder(int size) {
		return new Builder(size);
	}
	
	SpiPulseConfig(Builder builder) {
		cycle = builder.cycle;
		size = builder.size;
		delayMicros = builder.delayMicros;
		resetDelayMs = builder.resetDelayMs;
	}

	public PulseBuffer buffer() {
		return cycle.buffer(size);
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(cycle, size, delayMicros, resetDelayMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SpiPulseConfig)) return false;
		SpiPulseConfig other = (SpiPulseConfig) obj;
		if (!EqualsUtil.equals(cycle, other.cycle)) return false;
		if (size != other.size) return false;
		if (delayMicros != other.delayMicros) return false;
		if (resetDelayMs != other.resetDelayMs) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, cycle, size, delayMicros, resetDelayMs)
			.toString();
	}

}
