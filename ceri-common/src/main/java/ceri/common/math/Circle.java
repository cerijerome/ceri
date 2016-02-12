package ceri.common.math;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Circle {
	public final double r;

	public Circle(double r) {
		if (r <= 0) throw new IllegalArgumentException("Radius must be > 0: " + r);
		this.r = r;
	}

	public double area() {
		return area(r);
	}

	public double circumference() {
		return circumference(r);
	}

	public static double area(double r) {
		return Math.PI * r * r;
	}

	public static double circumference(double r) {
		return Math.PI * 2 * r;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Circle)) return false;
		Circle other = (Circle) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r).toString();
	}

}
