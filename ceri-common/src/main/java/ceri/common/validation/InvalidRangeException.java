package ceri.common.validation;

import ceri.common.text.StringUtil;

@Deprecated
class InvalidRangeException extends IllegalArgumentException {
	private static final long serialVersionUID = 2683785652371313243L;
	private static final String NO_NAME = "Value";
	public final Long min;
	public final Long max;
	public final long value;
	
	public static InvalidRangeException equal(long value, long expected, String name) {
		return new InvalidRangeException(value, expected, expected, name);
	}

	public static InvalidRangeException min(long value, long min, String name) {
		return new InvalidRangeException(value, min, null, name);
	}

	public static InvalidRangeException max(long value, long max, String name) {
		return new InvalidRangeException(value, null, max, name);
	}

	public static InvalidRangeException range(long value, long min, long max, String name) {
		return new InvalidRangeException(value, min, max, name);
	}

	private InvalidRangeException(long value, Long min, Long max, String name) {
		super(message(value, min, max, name));
		this.min = min;
		this.max = max;
		this.value = value;
	}

	private static String message(long value, Long min, Long max, String name) {
		StringBuilder b = new StringBuilder();
		b.append(name).append(" must be ");
		if (min == max) b.append(max);
		else if (min == null) b.append("<= ").append(max);
		else if (max == null) b.append(">= ").append(min);
		else if (min.equals(max)) b.append(max);
		else b.append(min).append('-').append(max);
		b.append(": ").append(value);
		return b.toString();
	}

}
