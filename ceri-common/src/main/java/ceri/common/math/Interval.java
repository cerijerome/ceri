package ceri.common.math;

import java.util.Comparator;
import java.util.Objects;
import ceri.common.math.Bound.Type;
import ceri.common.util.Align;

public class Interval<T> {
	public final Bound<T> lower;
	public final Bound<T> upper;

	public static <T> Interval<T> unbound() {
		return of(null, null);
	}

	public static <T extends Comparable<T>> Interval<T> point(T value) {
		return inclusive(value, value);
	}

	public static <T> Interval<T> point(T value, Comparator<T> comparator) {
		return inclusive(value, value, comparator);
	}

	public static <T extends Comparable<T>> Interval<T> inclusive(T lower, T upper) {
		return of(Bound.inclusive(lower), Bound.inclusive(upper));
	}

	public static <T> Interval<T> inclusive(T lower, T upper, Comparator<T> comparator) {
		return of(Bound.inclusive(lower, comparator), Bound.inclusive(upper, comparator));
	}

	public static <T extends Comparable<T>> Interval<T> exclusive(T lower, T upper) {
		return of(Bound.exclusive(lower), Bound.exclusive(upper));
	}

	public static <T> Interval<T> exclusive(T lower, T upper, Comparator<T> comparator) {
		return of(Bound.exclusive(lower, comparator), Bound.exclusive(upper, comparator));
	}

	public static <T> Interval<T> lower(Bound<T> lower) {
		return of(lower, null);
	}

	public static <T> Interval<T> upper(Bound<T> upper) {
		return of(null, upper);
	}

	public static <T> Interval<T> of(Bound<T> lower, Bound<T> upper) {
		if (lower == null) lower = Bound.unbound();
		if (upper == null) upper = Bound.unbound();
		return new Interval<>(lower, upper);
	}

	private Interval(Bound<T> lower, Bound<T> upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public boolean isUnbound() {
		return lower.isUnbound() && upper.isUnbound();
	}

	public boolean isInfinite() {
		return lower.isUnbound() || upper.isUnbound();
	}

	public boolean isPoint() {
		return !isInfinite() && lower.valueEquals(upper.value);
	}

	public boolean contains(T value) {
		return lower.lowerFor(value) && upper.upperFor(value);
	}

	public boolean isEmpty() {
		if (lower.isUnbound() || upper.isUnbound()) return false;
		int compare = lower.valueCompare(upper.value);
		if (compare < 0) return false;
		if (compare > 0) return true;
		return lower.type != Type.inclusive || upper.type != Type.inclusive;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lower, upper);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Interval<?> other)) return false;
		if (!Objects.equals(lower, other.lower)) return false;
		if (!Objects.equals(upper, other.upper)) return false;
		return true;
	}

	@Override
	public String toString() {
		if (isUnbound()) return lower.toString();
		if (isPoint()) return lower.toString(Align.H.left) + upper.type.right;
		return lower.toString(Align.H.left) + ", " + upper.toString(Align.H.right);
	}
}
