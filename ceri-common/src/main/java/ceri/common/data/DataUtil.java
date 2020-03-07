package ceri.common.data;

import static ceri.common.data.ByteUtil.fromAscii;
import static ceri.common.data.ByteUtil.toHex;
import static ceri.common.text.StringUtil.escape;
import static ceri.common.validation.ValidationUtil.validateEqual;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.util.ExceptionUtil;
import ceri.common.validation.ValidationUtil;

public class DataUtil {

	private DataUtil() {}

	public static int encodeByte(int value, byte[] data, int offset) {
		data[offset++] = (byte) (value & ByteUtil.BYTE_MASK);
		return offset;
	}

	public static int encodeIntMsb(int value, byte[] data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, Integer.BYTES);
	}

	public static int encodeIntLsb(int value, byte[] data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, Integer.BYTES);
	}

	public static int encodeShortMsb(int value, byte[] data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, Short.BYTES);
	}

	public static int encodeShortLsb(int value, byte[] data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, Short.BYTES);
	}

	public static int encodeByte(int value, ByteReceiver data, int offset) {
		data.set(offset++, value);
		return offset;
	}

	public static int encodeIntMsb(int value, ByteReceiver data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, Integer.BYTES);
	}

	public static int encodeIntLsb(int value, ByteReceiver data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, Integer.BYTES);
	}

	public static int encodeShortMsb(int value, ByteReceiver data, int offset) {
		return ByteUtil.writeBigEndian(value, data, offset, Short.BYTES);
	}

	public static int encodeShortLsb(int value, ByteReceiver data, int offset) {
		return ByteUtil.writeLittleEndian(value, data, offset, Short.BYTES);
	}

	public static int decodeByte(byte[] data, int offset) {
		return data[offset] & ByteUtil.BYTE_MASK;
	}

	public static int decodeIntMsb(byte[] data, int offset) {
		return (int) ByteUtil.fromBigEndian(data, offset, Integer.BYTES);
	}

	public static int decodeIntLsb(byte[] data, int offset) {
		return (int) ByteUtil.fromLittleEndian(data, offset, Integer.BYTES);
	}

	public static int decodeShortMsb(byte[] data, int offset) {
		return (int) ByteUtil.fromBigEndian(data, offset, Short.BYTES);
	}

	public static int decodeShortLsb(byte[] data, int offset) {
		return (int) ByteUtil.fromLittleEndian(data, offset, Short.BYTES);
	}

	public static int decodeByte(ByteProvider data, int offset) {
		return data.get(offset) & ByteUtil.BYTE_MASK;
	}

	public static int decodeIntMsb(ByteProvider data, int offset) {
		return (int) ByteUtil.fromBigEndian(data, offset, Integer.BYTES);
	}

	public static int decodeIntLsb(ByteProvider data, int offset) {
		return (int) ByteUtil.fromLittleEndian(data, offset, Integer.BYTES);
	}

	public static int decodeShortMsb(ByteProvider data, int offset) {
		return (int) ByteUtil.fromBigEndian(data, offset, Short.BYTES);
	}

	public static int decodeShortLsb(ByteProvider data, int offset) {
		return (int) ByteUtil.fromLittleEndian(data, offset, Short.BYTES);
	}

	public static void validateByte(int expected, int actual) {
		validateByte(expected, actual, null);
	}

	public static void validateByte(int expected, int actual, String name) {
		if ((expected & 0xff) == (actual & 0xff)) return;
		throw UnexpectedValueException.forByte(expected, actual, name);
	}

	public static void validateShort(int expected, int actual) {
		validateShort(expected, actual, null);
	}

	public static void validateShort(int expected, int actual, String name) {
		if ((expected & 0xffff) == (actual & 0xffff)) return;
		throw UnexpectedValueException.forShort(expected, actual, name);
	}

	public static void validateInt(int expected, int actual) {
		validateInt(expected, actual, null);
	}

	public static void validateInt(int expected, int actual, String name) {
		if (expected == actual) return;
		throw UnexpectedValueException.forInt(expected, actual, name);
	}

	/**
	 * Validates value to make sure type is set.
	 */
	public static void validate(IntTypeValue<?> value) {
		validate(value, (String) null);
	}

	/**
	 * Validates value to make sure type is set.
	 */
	public static void validate(IntTypeValue<?> value, String name) {
		validate(value, null, name);
	}

	/**
	 * Validates value to make sure type is set and not equals to the invalid value.
	 */
	public static <T> void validate(IntTypeValue<T> value, T invalid) {
		validate(value, invalid, null);
	}

	public static <T> void validate(IntTypeValue<T> value, T invalid, String name) {
		validateNotNull(value);
		if (value.hasType() && (invalid == null || !invalid.equals(value.type()))) return;
		throw UnexpectedValueException.forInt(value.value(), name);
	}

	public static int validateAscii(String value, ByteProvider data) {
		return validateAscii(value, data, 0);
	}

	public static int validateAscii(String value, ByteProvider data, int offset) {
		ImmutableByteArray expected = ByteUtil.toAscii(value);
		if (expected.matches(data, offset, expected.length())) return offset + expected.length();
		throw ExceptionUtil.exceptionf("Expected %s: %s", escape(value), 
			escape(fromAscii(data, offset, expected.length())));
	}

	public static void validateAscii(Predicate<String> predicate, ByteProvider data) {
		validateAscii(predicate, data, 0);
	}

	public static void validateAscii(Predicate<String> predicate, ByteProvider data,
		int offset) {
		validateAscii(predicate, data, offset, data.length() - offset);
	}

	public static void validateAscii(Predicate<String> predicate, ByteProvider data,
		int offset, int length) {
		String actual = fromAscii(data, offset, length);
		ValidationUtil.validate(predicate, actual);
	}

	public static int validate(ByteProvider expected, ByteProvider data) {
		return validate(expected, data, 0);
	}

	public static int validate(ByteProvider expected, ByteProvider data, int offset) {
		if (expected.matches(data, offset, expected.length())) return offset + expected.length();
		throw ExceptionUtil.exceptionf("Expected %s: %s", toHex(expected, " "),
			toHex(data, offset, expected.length(), " "));
	}

	public static <T> T validate(IntFunction<T> fn, int value) {
		return validate(fn, value, null);
	}

	public static <T> T validate(IntFunction<T> fn, int value, String name) {
		T t = fn.apply(value);
		if (t != null) return t;
		throw UnexpectedValueException.forInt(value, name);
	}

	public static <T> void validate(IntFunction<T> fn, T expected, int value) {
		validate(fn, expected, value, null);
	}

	public static <T> void validate(IntFunction<T> fn, T expected, int value, String name) {
		T actual = validate(fn, value, name);
		validateEqual(actual, expected, name);
	}

	public static void validateUnsignedByteRange(int value) {
		validateRange(value, 0, ByteUtil.BYTE_MASK);
	}

	public static void validateUnsignedShortRange(int value) {
		validateRange(value, 0, ByteUtil.SHORT_MASK);
	}

}
