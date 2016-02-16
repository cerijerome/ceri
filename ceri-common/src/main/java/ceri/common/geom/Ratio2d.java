package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ratio2d {
	public static final Ratio2d ZERO = new Ratio2d(0, 0);
	public static final Ratio2d ONE = new Ratio2d(1, 1);
	public final double x;
	public final double y;

	public static Ratio2d create(double scale) {
		return new Ratio2d(scale, scale);
	}

	public Ratio2d(double x, double y) {
		validateMin(x, 0, "X ratio");
		validateMin(y, 0, "Y ratio");
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ratio2d)) return false;
		Ratio2d other = (Ratio2d) obj;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + " x " + y + ")";
	}

}
