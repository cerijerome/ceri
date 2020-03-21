package ceri.common.validation;

import static ceri.common.data.UnsignedOctetType._general;
import ceri.common.data.UnsignedOctetType;

public class InvalidUnsignedRangeException extends IllegalArgumentException {
	private static final long serialVersionUID = 2683785652371313243L;
	private static final String NO_NAME = "Value";
	public final Long min;
	public final Long max;
	public final long value;
	public final UnsignedOctetType type;

	public static InvalidUnsignedRangeException equal(long value, long expected) {
		return equal(value, expected, NO_NAME);
	}

	public static InvalidUnsignedRangeException equal(long value, long expected, String name) {
		return new InvalidUnsignedRangeException(_general, value, expected, expected, name);
	}

	public static InvalidUnsignedRangeException min(long value, long min) {
		return min(value, min, NO_NAME);
	}

	public static InvalidUnsignedRangeException min(long value, long min, String name) {
		return new InvalidUnsignedRangeException(_general, value, min, null, name);
	}

	public static InvalidUnsignedRangeException max(long value, long max) {
		return max(value, max, NO_NAME);
	}

	public static InvalidUnsignedRangeException max(long value, long max, String name) {
		return new InvalidUnsignedRangeException(_general, value, null, max, name);
	}

	public static InvalidUnsignedRangeException range(long value, long min, long max) {
		return range(value, min, max, NO_NAME);
	}

	public static InvalidUnsignedRangeException range(long value, long min, long max, String name) {
		return new InvalidUnsignedRangeException(_general, value, min, max, name);
	}

	public static InvalidUnsignedRangeException of(long value, Long min, Long max) {
		return of(value, min, max, null);
	}

	public static InvalidUnsignedRangeException of(long value, Long min, Long max, String name) {
		return of(_general, value, min, max, name);
	}

	public static InvalidUnsignedRangeException of(UnsignedOctetType type, long value, Long min,
		Long max) {
		return of(type, value, min, max, null);
	}

	public static InvalidUnsignedRangeException of(UnsignedOctetType type, long value, Long min,
		Long max, String name) {
		return new InvalidUnsignedRangeException(type, value, min, max, name);
	}

	private InvalidUnsignedRangeException(UnsignedOctetType type, long value, Long min, Long max,
		String name) {
		super(message(type, value, min, max, name));
		this.min = min;
		this.max = max;
		this.value = value;
		this.type = type;
	}

	private static String message(UnsignedOctetType type, long value, Long min, Long max,
		String name) {
		StringBuilder b = new StringBuilder();
		b.append(name).append(" must be ");
		if (min == max) b.append(type.format(max));
		else if (min == null) b.append("<= ").append(type.format(max));
		else if (max == null) b.append(">= ").append(type.format(min));
		else if (min.equals(max)) b.append(type.format(max));
		else b.append(type.format(min)).append('-').append(type.format(max));
		b.append(": ").append(type.format(value));
		return b.toString();
	}

}
