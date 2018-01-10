package ceri.common.math;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class DoubleInterval {
	public static final DoubleInterval UNBOUND = new DoubleInterval(DoubleBound.UNBOUND,
		DoubleBound.UNBOUND);
	public final DoubleBound lower;
	public final DoubleBound upper;

	public static DoubleInterval point(double value) {
		return inclusive(value, value);
	}

	public static DoubleInterval inclusive(double lower, double upper) {
		return create(DoubleBound.inclusive(lower), DoubleBound.inclusive(upper));
	}

	public static DoubleInterval exclusive(double lower, double upper) {
		return create(DoubleBound.exclusive(lower), DoubleBound.exclusive(upper));
	}

	public static DoubleInterval lower(DoubleBound lower) {
		return create(lower, null);
	}

	public static DoubleInterval upper(DoubleBound upper) {
		return create(null, upper);
	}

	public static DoubleInterval create(DoubleBound lower, DoubleBound upper) {
		if (lower == null) lower = DoubleBound.UNBOUND;
		if (upper == null) upper = DoubleBound.UNBOUND;
		return new DoubleInterval(lower, upper);
	}

	private DoubleInterval(DoubleBound lower, DoubleBound upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public boolean unbound() {
		return lower.unbound() && upper.unbound();
	}

	public boolean contains(double value) {
		return lower.lowerFor(value) && upper.upperFor(value);
	}

	public boolean empty() {
		if (lower.unbound() || upper.unbound()) return false;
		int compare = lower.value.compareTo(upper.value);
		if (compare < 0) return false;
		if (compare > 0) return true;
		return lower.type != BoundType.inclusive || upper.type != BoundType.inclusive;
	}

	public double midPoint() {
		if (unbound()) return 0.0;
		if (lower.unbound()) return Double.NEGATIVE_INFINITY;
		if (upper.unbound()) return Double.POSITIVE_INFINITY;
		return (lower.value + upper.value) / 2;
	}

	public double width() {
		if (lower.unbound() || upper.unbound()) return Double.POSITIVE_INFINITY;
		return upper.value - lower.value;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(lower, upper);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DoubleInterval)) return false;
		DoubleInterval other = (DoubleInterval) obj;
		if (!EqualsUtil.equals(lower, other.lower)) return false;
		if (!EqualsUtil.equals(upper, other.upper)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, lower, upper).toString();
	}

}
