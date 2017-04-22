package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import ceri.common.collection.ImmutableByteArray;

public class DataUtil {

	private DataUtil() {}

	public static int encodeIntMsb(int value, byte[] data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, ByteUtil.INT_BYTES);
	}

	public static int encodeIntLsb(int value, byte[] data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, ByteUtil.INT_BYTES);
	}

	public static int encodeShortMsb(int value, byte[] data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, ByteUtil.SHORT_BYTES);
	}

	public static int encodeShortLsb(int value, byte[] data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, ByteUtil.SHORT_BYTES);
	}

	public static int decodeIntMsb(ImmutableByteArray data, int offset) {
		return ByteUtil.fromBigEndian(data, offset, ByteUtil.INT_BYTES);
	}

	public static int decodeIntLsb(ImmutableByteArray data, int offset) {
		return ByteUtil.fromLittleEndian(data, offset, ByteUtil.INT_BYTES);
	}

	public static int decodeShortMsb(ImmutableByteArray data, int offset) {
		return ByteUtil.fromBigEndian(data, offset, ByteUtil.SHORT_BYTES);
	}

	public static int decodeShortLsb(ImmutableByteArray data, int offset) {
		return ByteUtil.fromLittleEndian(data, offset, ByteUtil.SHORT_BYTES);
	}

	public static void validateByte(int expected, int actual) {
		validateByte(expected, actual, null);
	}
	
	public static void validateByte(int expected, int actual, String name) {
		if ((expected & 0xff) == (expected & 0xff)) return;
		throw UnexpectedValueException.forByte(expected, actual, name);
	}

	public static void validateShort(int expected, int actual) {
		validateShort(expected, actual, null);
	}
	
	public static void validateShort(int expected, int actual, String name) {
		if ((expected & 0xffff) == (expected & 0xffff)) return;
		throw UnexpectedValueException.forShort(expected, actual, name);
	}

	public static void validateInt(int expected, int actual) {
		validateInt(expected, actual, null);
	}

	public static void validateInt(int expected, int actual, String name) {
		if (expected == actual) return;
		throw UnexpectedValueException.forInt(expected, actual, name);
	}

	public static void validate(ByteTypeValue<?> value) {
		validate(value, null);
	}
	
	public static void validate(ByteTypeValue<?> value, String name) {
		validateNotNull(value);
		if (value.type != null) return;
		throw UnexpectedValueException.forByte(value.value, name);
	}

	public static void validate(ShortTypeValue<?> value) {
		validate(value, null);
	}

	public static void validate(ShortTypeValue<?> value, String name) {
		validateNotNull(value);
		if (value.type != null) return;
		throw UnexpectedValueException.forShort(value.value, name);
	}

	public static void validate(IntTypeValue<?> value) {
		validate(value, null);
	}

	public static void validate(IntTypeValue<?> value, String name) {
		validateNotNull(value);
		if (value.type != null) return;
		throw UnexpectedValueException.forInt(value.value, name);
	}

}
