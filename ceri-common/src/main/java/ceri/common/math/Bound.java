package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Comparator;
import ceri.common.comparator.Comparators;
import ceri.common.util.Align;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Bound<T> {
	private static final Bound<?> UNBOUND = new Bound<>(null, Type.exclusive, null);
	private static final String INFINITY = "\u221e";
	public final T value;
	public final Type type;
	private final Comparator<T> comparator;

	public enum Type {
		inclusive("[", "]"),
		exclusive("(", ")");

		public final String left;
		public final String right;

		private Type(String left, String right) {
			this.left = left;
			this.right = right;
		}
	}

	public static <T> Bound<T> unbound() {
		return BasicUtil.uncheckedCast(UNBOUND);
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
		if (value == null) return unbound();
		return new Bound<>(value, type, Comparators.comparable());
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

	public Integer compareTo(T value) {
		if (value == null || isUnbound()) return null;
		return comparator.compare(this.value, value);
	}

	public boolean isEqualTo(T value) {
		if (value == null && isUnbound()) return true;
		Integer i = compareTo(value);
		return i != null && i == 0;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(value, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Bound)) return false;
		Bound<?> other = (Bound<?>) obj;
		if (!EqualsUtil.equals(value, other.value)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
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
