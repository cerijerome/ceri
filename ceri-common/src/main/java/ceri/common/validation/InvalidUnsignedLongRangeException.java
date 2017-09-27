package ceri.common.validation;

public class InvalidUnsignedLongRangeException extends IllegalArgumentException {
	private static final long serialVersionUID = 2683785652371313243L;
	private static final String NO_NAME = "Value";
	private static final int HEX = 16;
	public final Long min;
	public final Long max;
	public final long value;

	public static InvalidUnsignedLongRangeException equal(long value, long expected) {
		return equal(value, expected, NO_NAME);
	}

	public static InvalidUnsignedLongRangeException equal(long value, long expected, String name) {
		return new InvalidUnsignedLongRangeException(value, expected, expected, name);
	}

	public static InvalidUnsignedLongRangeException min(long value, long min) {
		return min(value, min, NO_NAME);
	}

	public static InvalidUnsignedLongRangeException min(long value, long min, String name) {
		return new InvalidUnsignedLongRangeException(value, min, null, name);
	}

	public static InvalidUnsignedLongRangeException max(long value, long max) {
		return max(value, max, NO_NAME);
	}

	public static InvalidUnsignedLongRangeException max(long value, long max, String name) {
		return new InvalidUnsignedLongRangeException(value, null, max, name);
	}

	public static InvalidUnsignedLongRangeException range(long value, long min, long max) {
		return range(value, min, max, NO_NAME);
	}

	public static InvalidUnsignedLongRangeException range(long value, long min, long max,
		String name) {
		return new InvalidUnsignedLongRangeException(value, min, max, name);
	}

	private InvalidUnsignedLongRangeException(long value, Long min, Long max, String name) {
		super(message(value, min, max, name));
		this.min = min;
		this.max = max;
		this.value = value;
	}

	private static String message(long value, Long min, Long max, String name) {
		StringBuilder b = new StringBuilder();
		b.append(name).append(" must be ");
		if (min == max) b.append(hex(max));
		else if (min == null) b.append("<= ").append(hex(max));
		else if (max == null) b.append(">= ").append(hex(min));
		else if (min.equals(max)) b.append(hex(max));
		else b.append(hex(min)).append('-').append(hex(max));
		b.append(": ").append(hex(value));
		return b.toString();
	}

	private static String hex(long value) {
		if (value >= 0 && value < 10) return String.valueOf(value);
		return "0x" + Long.toUnsignedString(value, HEX);
	}

}
