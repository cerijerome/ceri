package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Comparator;
import java.util.Objects;
import ceri.common.comparator.Comparators;
import ceri.common.util.Align;
import ceri.common.util.BasicUtil;

public class Bound<T> {
	private static final Bound<?> UNBOUND = new Bound<>(null, Type.exclusive, null);
	private static final String INFINITY = "\u221e";
	public final T value;
	public final Type type;
	private final Comparator<T> comparator;

	public enum Type {
		inclusive("[", "]", "<=", ">="),
		exclusive("(", ")", "<", ">");

		public final String left;
		public final String right;
		public final String upper;
		public final String lower;

		private Type(String left, String right, String upper, String lower) {
			this.left = left;
			this.right = right;
			this.upper = upper;
			this.lower = lower;
		}

		public boolean isUpper(int value, int limit) {
			return this == inclusive ? value <= limit : value < limit;
		}

		public boolean isUpper(long value, long limit) {
			return this == inclusive ? value <= limit : value < limit;
		}

		public boolean isUpper(float value, float limit) {
			return this == inclusive ? value <= limit : value < limit;
		}

		public boolean isUpper(double value, double limit) {
			return this == inclusive ? value <= limit : value < limit;
		}

		public boolean isLower(int value, int limit) {
			return this == inclusive ? limit <= value : limit < value;
		}

		public boolean isLower(long value, long limit) {
			return this == inclusive ? limit <= value : limit < value;
		}

		public boolean isLower(float value, float limit) {
			return this == inclusive ? limit <= value : limit < value;
		}

		public boolean isLower(double value, double limit) {
			return this == inclusive ? limit <= value : limit < value;
		}

	}

	public static <T> Bound<T> unbound() {
		return BasicUtil.unchecked(UNBOUND);
	}

	public static <T extends Comparable<T>> Bound<T> inclusive(T value) {
		return inclusive(value, Comparators.comparable());
	}

	public static <T> Bound<T> inclusive(T value, Comparator<T> comparator) {
		return of(value, Type.inclusive, comparator);
	}

	public static <T extends Comparable<T>> Bound<T> exclusive(T value) {
		return exclusive(value, Comparators.comparable());
	}

	public static <T> Bound<T> exclusive(T value, Comparator<T> comparator) {
		return of(value, Type.exclusive, comparator);
	}

	public static <T extends Comparable<T>> Bound<T> of(T value, Type type) {
		return of(value, type, Comparators.comparable());
	}

	public static <T> Bound<T> of(T value, Type type, Comparator<T> comparator) {
		if (value == null) return unbound();
		validateNotNull(comparator, "Comparator");
		return new Bound<>(value, type, comparator);
	}

	private Bound(T value, Type type, Comparator<T> comparator) {
		this.value = value;
		this.type = type;
		this.comparator = comparator;
	}

	public boolean isUnbound() {
		return value == null;
	}

	public boolean upperFor(T value) {
		if (value == null) return false;
		if (isUnbound()) return true;
		int compare = comparator.compare(this.value, value);
		return compare > 0 || (compare == 0 && type == Type.inclusive);
	}

	public boolean lowerFor(T value) {
		if (value == null) return false;
		if (isUnbound()) return true;
		int compare = comparator.compare(this.value, value);
		return compare < 0 || (compare == 0 && type == Type.inclusive);
	}

	public Integer valueCompare(T value) {
		if (value == null || isUnbound()) return null;
		return comparator.compare(this.value, value);
	}

	public boolean valueEquals(T value) {
		if (value == null && isUnbound()) return true;
		Integer i = valueCompare(value);
		return i != null && i == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Bound<?> other)) return false;
		if (!Objects.equals(value, other.value)) return false;
		if (!Objects.equals(type, other.type)) return false;
		return true;
	}

	public String toString(Align.H align) {
		return isUnbound() ? unbound(align) : value(align);
	}

	@Override
	public String toString() {
		return toString(null);
	}

	private String value(Align.H align) {
		if (align == Align.H.left) return type.left + value;
		if (align == Align.H.right) return value + type.right;
		return type.left + value + type.right;
	}

	private String unbound(Align.H align) {
		if (align == Align.H.left) return type.left + "-" + INFINITY;
		if (align == Align.H.right) return INFINITY + type.right;
		return type.left + INFINITY + type.right;
	}
}
