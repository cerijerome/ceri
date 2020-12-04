package ceri.serial.spi.pulse;

import static ceri.common.math.MathUtil.ceilDiv;
import static ceri.common.util.BasicUtil.unused;
import java.util.Objects;

/**
 * Mapping logic of bits to pulse shapes.
 */
public abstract class PulseCycle {
	private final int cycleStorageBytes;
	private final int cycleStorageBits;
	private final int cycleDataBits;
	public final int pulseOffsetBits;
	public final int t0Bits;
	public final int t1Bits;
	public final int pulseBits; // bit ratio

	public enum Type {
		nbit,
		nbit9,
		nbit27;
	}

	protected PulseCycle(int cycleStorageBits, int pulseBits, int pulseOffsetBits, int t0Bits,
		int t1Bits) {
		this.cycleStorageBits = cycleStorageBits;
		this.cycleStorageBytes = cycleStorageBits / Byte.SIZE;
		this.cycleDataBits = cycleDataBits(cycleStorageBits, pulseBits, pulseOffsetBits, t1Bits);
		this.pulseBits = pulseBits;
		this.pulseOffsetBits = pulseOffsetBits;
		this.t0Bits = t0Bits;
		this.t1Bits = t1Bits;
	}

	public abstract Type type();

	/**
	 * The number of signal bits in a cycle
	 */
	public int cycleSignalBits() {
		return cycleStorageBits;
	}

	public int storageBytes(int dataBytes) {
		int dataBit = (dataBytes * Byte.SIZE) - 1;
		int pos = t0Pos(dataBit) + t1Bits(dataBit);
		return ceilDiv(pos, Byte.SIZE);
	}

	public PulseStats stats(int freqHz) {
		return new PulseStats(this, freqHz);
	}

	public PulseBuffer buffer(int dataSize) {
		return new PulseBuffer(this, dataSize);
	}

	public int t0Pos(int dataBit) {
		int cycles = cycle(dataBit);
		int subCycleBit = subCycleBit(dataBit);
		return offset(cycles) + (subCycleBit * pulseBits) + pulseOffsetBits;
	}

	public int t0Bits(int dataBit) {
		unused(dataBit);
		return t0Bits;
	}

	public int t1Bits(int dataBit) {
		unused(dataBit);
		return t1Bits;
	}

	public int cycle(int dataBit) {
		return dataBit / cycleDataBits;
	}

	public int subCycleBit(int dataBit) {
		return dataBit % cycleDataBits;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cycleStorageBits, pulseBits, pulseOffsetBits, t0Bits, t1Bits);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PulseCycle)) return false;
		PulseCycle other = (PulseCycle) obj;
		if (cycleStorageBits != other.cycleStorageBits) return false;
		if (pulseBits != other.pulseBits) return false;
		if (pulseOffsetBits != other.pulseOffsetBits) return false;
		if (t0Bits != other.t0Bits) return false;
		if (t1Bits != other.t1Bits) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d%s(%d+%d,%d:%d)", pulseBits, type(), cycleStorageBits,
			pulseOffsetBits, t0Bits, t1Bits);
	}

	private int offset(int cycles) {
		return cycles * cycleStorageBytes * Byte.SIZE;
	}

	private int cycleDataBits(int cycleStorageBits, int pulseBits, int pulseOffsetBits,
		int t1Bits) {
		int availableBits = cycleStorageBits - pulseOffsetBits;
		int cycleDataBits = availableBits / pulseBits;
		if (availableBits % pulseBits >= t1Bits) cycleDataBits++;
		return cycleDataBits;
	}

}
