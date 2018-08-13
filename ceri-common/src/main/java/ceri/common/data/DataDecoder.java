package ceri.common.data;

import static ceri.common.data.ByteUtil.toHex;
import static ceri.common.text.StringUtil.escape;
import static ceri.common.util.BasicUtil.exceptionf;
import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import ceri.common.collection.ImmutableByteArray;

public class DataDecoder {
	private final ImmutableByteArray data;
	private int offset = 0;

	public static DataDecoder of(ImmutableByteArray data, int offset) {
		return of(data.slice(offset));
	}

	public static DataDecoder of(ImmutableByteArray data, int offset, int length) {
		return of(data.slice(offset, length));
	}

	public static DataDecoder of(ImmutableByteArray data) {
		validateNotNull(data);
		return new DataDecoder(data);
	}

	private DataDecoder(ImmutableByteArray data) {
		this.data = data;
	}

	public ImmutableByteArray slice() {
		return slice(remaining());
	}

	public ImmutableByteArray slice(int length) {
		ImmutableByteArray data = this.data.slice(offset, length);
		offset(data.length);
		return data;
	}

	public DataDecoder skip(int count) {
		offset(count);
		return this;
	}

	public DataDecoder rewind(int count) {
		validateMin(offset - count, 0);
		offset -= count;
		return this;
	}

	public int offset() {
		return offset;
	}

	public int remaining() {
		return data.length - offset;
	}

	public int total() {
		return data.length;
	}

	public int decodeByte() {
		return data.at(offset(Byte.BYTES)) & ByteUtil.BYTE_MASK;
	}

	public int decodeShortMsb() {
		return (int) ByteUtil.fromBigEndian(data, offset(Short.BYTES), Short.BYTES);
	}

	public int decodeShortLsb() {
		return (int) ByteUtil.fromLittleEndian(data, offset(Short.BYTES), Short.BYTES);
	}

	public int decodeIntMsb() {
		return (int) ByteUtil.fromBigEndian(data, offset(Integer.BYTES), Integer.BYTES);
	}

	public int decodeIntLsb() {
		return (int) ByteUtil.fromLittleEndian(data, offset(Integer.BYTES), Integer.BYTES);
	}

	public String decodeAscii(int length) {
		return ByteUtil.fromAscii(data, offset(length), length);
	}

	public DataDecoder validateAscii(String value) {
		ImmutableByteArray data = ByteUtil.toAscii(value);
		if (this.data.equals(offset, data)) return skip(data.length);
		String actual = ByteUtil.fromAscii(this.data, offset, data.length);
		throw exceptionf("Expected %s: %s", escape(value), escape(actual));
	}

	public DataDecoder validateAscii(ImmutableByteArray ascii) {
		return validateAscii(ascii, 0);
	}

	public DataDecoder validateAscii(ImmutableByteArray ascii, int offset) {
		return validateAscii(ascii, offset, ascii.length - offset);
	}

	public DataDecoder validateAscii(ImmutableByteArray ascii, int offset, int length) {
		if (data.equals(this.offset, ascii, offset, length)) return skip(length);
		String actual = ByteUtil.fromAscii(data, this.offset, length);
		String expected = ByteUtil.fromAscii(ascii, offset, length);
		throw exceptionf("Expected %s: %s", escape(expected), escape(actual));
	}

	public DataDecoder validate(ImmutableByteArray data) {
		return validate(data, 0);
	}

	public DataDecoder validate(ImmutableByteArray data, int offset) {
		return validate(data, offset, data.length - offset);
	}

	public DataDecoder validate(ImmutableByteArray data, int offset, int length) {
		if (this.data.equals(this.offset, data, offset, length)) return skip(length);
		throw exceptionf("Expected %s: %s", toHex(data.slice(offset, length)),
			toHex(this.data.slice(this.offset, length)));
	}

	private int offset(int count) {
		validateMax(offset + count, data.length);
		int old = offset;
		offset += count;
		return old;
	}

}
