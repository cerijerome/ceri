package ceri.common.validation;

public class InvalidDoubleRangeException extends IllegalArgumentException {
	private static final long serialVersionUID = 3544099867489070075L;
	private static final String NO_NAME = "Value";
	public final double min;
	public final double max;
	public final double value;

	public static InvalidDoubleRangeException equal(double value, double expected) {
		return equal(value, expected, NO_NAME);
	}
	
	public static InvalidDoubleRangeException equal(double value, double expected, String name) {
		return new InvalidDoubleRangeException(value, expected, expected, name);
	}
	
	public static InvalidDoubleRangeException min(double value, double min) {
		return min(value, min, NO_NAME);
	}

	public static InvalidDoubleRangeException min(double value, double min, String name) {
		return new InvalidDoubleRangeException(value, min, null, name);
	}

	public static InvalidDoubleRangeException max(double value, double max) {
		return max(value, max);
	}

	public static InvalidDoubleRangeException max(double value, double max, String name) {
		return new InvalidDoubleRangeException(value, null, max, name);
	}

	public static InvalidDoubleRangeException range(double value, double min, double max) {
		return range(value, min, max, NO_NAME);
	}
	
	public static InvalidDoubleRangeException range(double value, double min, double max, String name) {
		return new InvalidDoubleRangeException(value, min, max, name);
	}
	
	private InvalidDoubleRangeException(double value, Double min, Double max, String name) {
		super(message(value, min, max, name));
		this.min = min;
		this.max = max;
		this.value = value;
	}

	private static String message(double value, Double min, Double max, String name) {
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
