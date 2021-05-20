package ceri.serial.spi.pulse;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Objects;
import ceri.common.text.ToString;

public class SpiPulseConfig {
	public static final SpiPulseConfig NULL = of(0);
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
		validateMin(size, 0);
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

	public boolean isNull() {
		return size == 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(cycle, size, delayMicros, resetDelayMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SpiPulseConfig)) return false;
		SpiPulseConfig other = (SpiPulseConfig) obj;
		if (!Objects.equals(cycle, other.cycle)) return false;
		if (size != other.size) return false;
		if (delayMicros != other.delayMicros) return false;
		if (resetDelayMs != other.resetDelayMs) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, cycle, size, delayMicros, resetDelayMs);
	}

}
