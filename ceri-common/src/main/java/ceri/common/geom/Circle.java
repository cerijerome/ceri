package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Circle {
	public static final Circle NULL = new Circle(0);
	public final double r;

	public static Circle create(double r) {
		if (r == 0) return NULL;
		validateMin(r, 0, "Radius");
		return new Circle(r);
	}

	private Circle(double r) {
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

	/**
	 * Is this a null circle?
	 */
	public boolean isNull() {
		return r == 0;
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
