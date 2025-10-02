package ceri.common.data;

import ceri.common.except.Exceptions;
import ceri.common.util.Validate;

/**
 * Use to generate a CRC value using a CRC algorithm. TODO: convert to ByteReceiver?
 */
public class Crc {
	public final CrcAlgorithm algorithm;
	private long crc;

	Crc(CrcAlgorithm algorithm) {
		this.algorithm = algorithm;
		reset();
	}

	public byte crcByte() {
		return (byte) crc();
	}

	public short crcShort() {
		return (short) crc();
	}

	public int crcInt() {
		return (int) crc();
	}

	public long crc() {
		return algorithm.complete(crc);
	}

	public void verify(long value) {
		value = algorithm.mask(value);
		long crc = crc();
		if (value == crc) return;
		throw Exceptions.illegalArg("Expected CRC 0x%x: 0x%x", value, crc);
	}

	public boolean isValid(long value) {
		return value == crc();
	}

	public Crc reset() {
		crc = algorithm.init;
		return this;
	}

	public Crc add(int... data) {
		for (int d : data)
			crc = algorithm.apply(crc, (byte) d);
		return this;
	}

	public Crc add(byte... data) {
		return add(data, 0);
	}

	public Crc add(byte[] data, int offset) {
		return add(data, offset, data.length - offset);
	}

	public Crc add(byte[] data, int offset, int length) {
		Validate.slice(data.length, offset, length);
		for (int i = offset; i < offset + length; i++)
			crc = algorithm.apply(crc, data[i]);
		return this;
	}

	public Crc add(ByteReader r, int length) {
		while (length-- > 0)
			crc = algorithm.apply(crc, r.readByte());
		return this;
	}

	public Crc add(ByteProvider data) {
		return add(data, 0);
	}

	public Crc add(ByteProvider data, int offset) {
		return add(data, offset, data.length() - offset);
	}

	public Crc add(ByteProvider data, int offset, int length) {
		Validate.slice(data.length(), offset, length);
		for (int i = offset; i < offset + length; i++)
			crc = algorithm.apply(crc, data.getByte(i));
		return this;
	}
}
