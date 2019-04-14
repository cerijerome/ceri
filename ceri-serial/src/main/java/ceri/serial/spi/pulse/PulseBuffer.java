package ceri.serial.spi.pulse;

import java.nio.ByteBuffer;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.ByteUtil;

/**
 * Buffer mapping data bits to pulses shaped with storage bits.
 */
public class PulseBuffer {
	public final PulseCycle cycle;
	private final int dataSize;
	private final byte[] buffer;

	PulseBuffer(PulseCycle cycle, int dataSize) {
		this.cycle = cycle;
		this.dataSize = dataSize;
		buffer = new byte[cycle.storageBytes(dataSize)];
		initBuffer(dataSize);
	}

	public int dataSize() {
		return dataSize;
	}

	public int storageSize() {
		return buffer.length;
	}

	/**
	 * Determines the frequency multiplier for the storage pulse bits.
	 */
	public int pulseBits() {
		return cycle.pulseBits;
	}

	public void write(byte[] data) {
		write(data, 0);
	}

	public void write(byte[] data, int offset) {
		write(data, offset, data.length);
	}

	public void write(byte[] data, int offset, int len) {
		write(0, data, offset, len);
	}

	public void write(int srcOffset, byte[] data, int offset, int len) {
		ArrayUtil.validateSlice(data.length, srcOffset, len);
		ArrayUtil.validateSlice(dataSize(), offset, len);
		for (int i = 0; i < len; i++)
			setByte(offset + i, data[srcOffset + i]);
	}
	
	public void setByte(int offset, int value) {
		int bit = offset * Byte.SIZE;
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

	public void writeTo(ByteBuffer out) {
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
