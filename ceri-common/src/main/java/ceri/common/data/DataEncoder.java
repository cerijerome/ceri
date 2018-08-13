package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;

public class DataEncoder {
	private final byte[] data;
	private int start;
	private int length;
	private int offset = 0;

	public static DataEncoder of(int size) {
		return of(new byte[size]);
	}

	public static DataEncoder of(byte[] data) {
		return of(data, 0);
	}

	public static DataEncoder of(byte[] data, int offset) {
		return of(data, offset, data.length - offset);
	}

	public static DataEncoder of(byte[] data, int offset, int length) {
		validateNotNull(data);
		ArrayUtil.validateSlice(data.length, offset, length);
		return new DataEncoder(data, offset, length);
	}

	private DataEncoder(byte[] data, int start, int length) {
		this.data = data;
		this.start = start;
		this.length = length;
	}

	public DataEncoder skip(int count) {
		offset(count);
		return this;
	}

	public DataEncoder rewind(int count) {
		validateMin(offset - count, 0);
		offset -= count;
		return this;
	}

	public int offset() {
		return offset;
	}

	public int remaining() {
		return length - offset;
	}

	public int total() {
		return length;
	}

	public DataEncoder encodeByte(int value) {
		data[position(Byte.BYTES)] = (byte) (value & ByteUtil.BYTE_MASK);
		return this;
	}

	public DataEncoder encodeShortMsb(int value) {
		ByteUtil.writeBigEndian(value, data, position(Short.BYTES), Short.BYTES);
		return this;
	}

	public DataEncoder encodeShortLsb(int value) {
		ByteUtil.writeLittleEndian(value, data, position(Short.BYTES), Short.BYTES);
		return this;
	}

	public DataEncoder encodeIntMsb(int value) {
		ByteUtil.writeBigEndian(value, data, position(Integer.BYTES), Integer.BYTES);
		return this;
	}

	public DataEncoder decodeIntLsb(int value) {
		ByteUtil.writeLittleEndian(value, data, position(Integer.BYTES), Integer.BYTES);
		return this;
	}

	public DataEncoder encodeAscii(String value) {
		return copy(ByteUtil.toAscii(value));
	}

	public DataEncoder copy(ImmutableByteArray data) {
		data.copyTo(this.data, position(data.length));
		return this;
	}

	private int position(int count) {
		return start + offset(count);
	}

	private int offset(int count) {
		validateMax(offset + count, length);
		int old = offset;
		offset += count;
		return old;
	}

}
