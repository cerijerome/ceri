package ceri.serial.spi.pulse;

import static ceri.common.validation.ValidationUtil.validateMin;

public record SpiPulseConfig(PulseCycle cycle, int size, int delayMicros, int resetDelayMs) {

	public static final SpiPulseConfig NULL = of(0);

	public static SpiPulseConfig of(int size) {
		return builder(size).build();
	}

	public static class Builder {
		final int size;
		PulseCycle cycle = PulseCycle.Std._4_9.cycle;
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
			return new SpiPulseConfig(cycle, size, delayMicros, resetDelayMs);
		}
	}

	public static Builder builder(int size) {
		validateMin(size, 0);
		return new Builder(size);
	}

	public PulseBuffer buffer() {
		return cycle.buffer(size);
	}

	public boolean isNull() {
		return size == 0;
	}
}
