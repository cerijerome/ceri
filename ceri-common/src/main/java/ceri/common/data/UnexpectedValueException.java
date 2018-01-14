package ceri.common.data;

import ceri.common.util.BasicUtil;

/**
 * Exception when expecting a specific value.
 */
public class UnexpectedValueException extends IllegalArgumentException {
	private static final long serialVersionUID = -1397888268813085217L;
	public final int actual;
	public final Integer expected;

	public static UnexpectedValueException forByte(int actual) {
		return forByte(actual, null);
	}

	public static UnexpectedValueException forByte(int actual, String name) {
		return new UnexpectedValueException(null, actual & 0xff, name);
	}

	public static UnexpectedValueException forByte(int expected, int actual) {
		return forByte(expected, actual, null);
	}

	public static UnexpectedValueException forByte(int expected, int actual, String name) {
		return new UnexpectedValueException(expected & 0xff, actual & 0xff, name);
	}

	public static UnexpectedValueException forShort(int actual) {
		return forShort(actual, null);
	}

	public static UnexpectedValueException forShort(int actual, String name) {
		return new UnexpectedValueException(null, actual & 0xffff, name);
	}

	public static UnexpectedValueException forShort(int expected, int actual) {
		return forShort(expected, actual, null);
	}

	public static UnexpectedValueException forShort(int expected, int actual, String name) {
		return new UnexpectedValueException(expected & 0xffff, actual & 0xffff, name);
	}

	public static UnexpectedValueException forInt(int actual) {
		return forInt(actual, null);
	}

	public static UnexpectedValueException forInt(int actual, String name) {
		return new UnexpectedValueException(null, actual, name);
	}

	public static UnexpectedValueException forInt(int expected, int actual) {
		return forInt(expected, actual, null);
	}

	public static UnexpectedValueException forInt(int expected, int actual, String name) {
		return new UnexpectedValueException(expected, actual, name);
	}

	private UnexpectedValueException(Integer expected, int actual, String name) {
		super(description(expected, actual, name));
		this.expected = expected;
		this.actual = actual;
	}

	private static String description(Integer expected, int actual, String name) {
		name = BasicUtil.isEmpty(name) ? "" : " " + name;
		if (expected == null) return "Unexpected" + name + " value: 0x" + hex(actual);
		return "Expected" + name + " value 0x" + hex(expected) + ": 0x" + hex(actual);
	}

	private static String hex(int value) {
		return Integer.toUnsignedString(value & 0xffffffff, 16);
	}

}
