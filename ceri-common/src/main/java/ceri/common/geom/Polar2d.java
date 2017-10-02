package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Polar2d {
	public static final Polar2d ZERO = new Polar2d(0, 0);
	public final double r;
	public final double phi;

	public static Polar2d from(Point2d point) {
		if (point.equals(Point2d.ZERO)) return ZERO;
		Line2d line = Line2d.create(Point2d.ZERO, point);
		return new Polar2d(line.length(), line.angle());
	}

	public static Polar2d create(double r, double phi) {
		validateMin(r, 0, "Radius");
		return new Polar2d(r, phi);
	}

	private Polar2d(double r, double phi) {
		this.r = r;
		this.phi = phi;
	}

	public Point2d asPoint() {
		return GeometryUtil.offset(r, phi);
	}

	public Polar2d rotate(double angle) {
		return new Polar2d(r, phi + angle);
	}

	public Polar2d reverse() {
		return new Polar2d(r, -phi);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r, phi);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Polar2d)) return false;
		Polar2d other = (Polar2d) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(phi, other.phi)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, phi).toString();
	}

}
