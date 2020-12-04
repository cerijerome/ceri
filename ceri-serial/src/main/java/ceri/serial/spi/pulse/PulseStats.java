package ceri.serial.spi.pulse;

import java.util.Objects;

/**
 * Timing statistics for a pulse cycle for given frequency.
 */
public class PulseStats {
	private static final double HZ_IN_NS = 1000000000.0;
	public final PulseCycle cycle;
	public final int freqHz;
	public final double bitNs;
	public final double pulseNs;
	public final double t0Ns;
	public final double t1Ns;

	PulseStats(PulseCycle cycle, int freqHz) {
		this.cycle = cycle;
		this.freqHz = freqHz;
		bitNs = HZ_IN_NS / freqHz;
		t0Ns = cycle.t0Bits * bitNs;
		t1Ns = cycle.t1Bits * bitNs;
		pulseNs = cycle.pulseBits * bitNs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cycle, freqHz);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PulseStats)) return false;
		PulseStats other = (PulseStats) obj;
		if (freqHz != other.freqHz) return false;
		if (!Objects.equals(cycle, other.cycle)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[t0=%.0fns, t1=%.0fns, pulse=%.0fns]", getClass().getSimpleName(),
			t0Ns, t1Ns, pulseNs);
	}
}
