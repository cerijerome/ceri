package ceri.common.math;

import ceri.common.math.Bound.Type;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Interval<T extends Comparable<T>> {
	public final Bound<T> lower;
	public final Bound<T> upper;

	public static <T extends Comparable<T>> Interval<T> unbound() {
		return of(null, null);
	}

	public static <T extends Comparable<T>> Interval<T> point(T value) {
		return inclusive(value, value);
	}

	public static <T extends Comparable<T>> Interval<T> inclusive(T lower, T upper) {
		return of(Bound.inclusive(lower), Bound.inclusive(upper));
	}

	public static <T extends Comparable<T>> Interval<T> exclusive(T lower, T upper) {
		return of(Bound.exclusive(lower), Bound.exclusive(upper));
	}

	public static <T extends Comparable<T>> Interval<T> lower(Bound<T> lower) {
		return of(lower, null);
	}

	public static <T extends Comparable<T>> Interval<T> upper(Bound<T> upper) {
		return of(null, upper);
	}

	public static <T extends Comparable<T>> Interval<T> of(Bound<T> lower, Bound<T> upper) {
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

	public boolean contains(T value) {
		return lower.lowerFor(value) && upper.upperFor(value);
	}

	public boolean isEmpty() {
		if (lower.isUnbound() || upper.isUnbound()) return false;
		int compare = lower.value.compareTo(upper.value);
		if (compare < 0) return false;
		if (compare > 0) return true;
		return lower.type != Type.inclusive || upper.type != Type.inclusive;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(lower, upper);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Interval)) return false;
		Interval<?> other = (Interval<?>) obj;
		if (!EqualsUtil.equals(lower, other.lower)) return false;
		if (!EqualsUtil.equals(upper, other.upper)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, lower, upper).toString();
	}

}
