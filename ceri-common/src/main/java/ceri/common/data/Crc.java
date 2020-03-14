package ceri.common.data;

import java.nio.charset.StandardCharsets;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;

/**
 * <pre>
 * poly = powers as bits excluding highest
 * init = initial value
 * refin = true?: reverse bits of each input byte
 * refout = true: reverse bits of result
 * xorout = value to xor with result (after refout)
 * check = result of running ascii bytes "123456789"
 * </pre>
 */
public class Crc {
	static final int CACHE_SIZE = 1 << Byte.SIZE;
	static final ImmutableByteArray checkBytes =
		ImmutableByteArray.wrap("123456789".getBytes(StandardCharsets.ISO_8859_1));
	public static final CrcAlgorithm CRC16_XMODEM = CrcAlgorithm.of(16, 0x1021, 0, false);
	public static final CrcAlgorithm CRC8_SMBUS = CrcAlgorithm.of(8, 0x07, 0, false);
	private final CrcAlgorithm config;
	private long crc;

	public static void main(String[] args) {
		print(CRC8_SMBUS);
		print(CRC16_XMODEM);
	}

	private static void print(CrcAlgorithm crc) {
		System.out.printf("%s = 0x%x%n", crc, crc.check());
	}

	static interface EntryAccessor {
		long entry(int i);
	}

	Crc(CrcAlgorithm config) {
		this.config = config;
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
		return config.complete(crc);
	}
	
	public Crc reset() {
		crc = config.init;
		return this;
	}
	
	public Crc add(int... data) {
		for (int d : data)
			crc = config.apply(crc, (byte) d);
		return this;
	}

	public Crc add(byte... data) {
		return add(data, 0);
	}

	public Crc add(byte[] data, int offset) {
		return add(data, offset, data.length - offset);
	}

	public Crc add(byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		for (int i = offset; i < offset + length; i++)
			crc = config.apply(crc, data[i]);
		return this;
	}

	public Crc add(ByteProvider data) {
		return add(data, 0);
	}

	public Crc add(ByteProvider data, int offset) {
		return add(data, offset, data.length() - offset);
	}

	public Crc add(ByteProvider data, int offset, int length) {
		ArrayUtil.validateSlice(data.length(), offset, length);
		for (int i = offset; i < offset + length; i++)
			crc = config.apply(crc, data.get(i));
		return this;
	}
}
