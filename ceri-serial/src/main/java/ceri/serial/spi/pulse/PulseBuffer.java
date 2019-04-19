package ceri.serial.spi.pulse;

import java.nio.ByteBuffer;
import ceri.common.collection.ByteReceiver;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.ByteUtil;

/**
 * Buffer mapping data bits to pulses shaped with storage bits.
 */
public class PulseBuffer implements ByteReceiver {
	public final PulseCycle cycle;
	private final int dataSize;
	private final byte[] buffer;

	PulseBuffer(PulseCycle cycle, int dataSize) {
		this.cycle = cycle;
		this.dataSize = dataSize;
		buffer = new byte[cycle.storageBytes(dataSize)];
		initBuffer(dataSize);
	}

	/**
	 * Returns the data length.
	 */
	@Override
	public int length() {
		return dataSize;
	}

	/**
	 * Returns the storage size to hold the data converted to pulses. 
	 */
	public int storageSize() {
		return buffer.length;
	}

	/**
	 * Determines the frequency multiplier for the storage pulse bits.
	 */
	public int pulseBits() {
		return cycle.pulseBits;
	}

	@Override
	public void set(int pos, int value) {
		int bit = pos * Byte.SIZE;
		for (int i = 0; i < Byte.SIZE; i++)
			setDataBit(bit + Byte.SIZE - i - 1, (value & (1 << i)) != 0);
	}

	private void initBuffer(int dataBytes) {
		for (int i = 0; i < dataBytes * Byte.SIZE; i++)
			setStorageBits(cycle.t0Pos(i), cycle.t0Bits(i), true);
	}

	private void setDataBit(int dataBit, boolean on) {
		int pos = cycle.t0Pos(dataBit);
		int t0Bits = cycle.t0Bits(dataBit);
		int t1Bits = cycle.t1Bits(dataBit);
		setStorageBits(pos + t0Bits, t1Bits - t0Bits, on);
	}

	public void writePulseTo(ByteBuffer out) {
		if (out == null) return;
		out.clear();
		out.put(buffer);
	}

	public ImmutableByteArray buffer() {
		return ImmutableByteArray.wrap(buffer);
	}

	private void setStorageBits(int pos, int count, boolean on) {
		if (count == 0) return;
		int offset = pos / Byte.SIZE;
		int startBit = pos % Byte.SIZE;
		while (count > 0 && offset < buffer.length) {
			int len = Math.min(count, Byte.SIZE - startBit);
			setStorageByte(offset++, startBit, len, on);
			startBit = 0;
			count -= len;
		}
	}

	private void setStorageByte(int offset, int startBit, int len, boolean on) {
		int mask = ByteUtil.maskInt(Byte.SIZE - startBit - len, len); // reverse it
		if (on) buffer[offset] |= mask;
		else buffer[offset] &= ~mask;
	}

}
