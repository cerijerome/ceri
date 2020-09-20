package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import java.util.Objects;
import ceri.common.text.ToString;

public class Polar2d {
	public static final Polar2d ZERO = new Polar2d(0, 0);
	public final double r;
	public final double phi;

	public static Polar2d from(Point2d point) {
		if (point == Point2d.ZERO) return ZERO;
		Line2d line = Line2d.of(Point2d.ZERO, point);
		return of(line.length(), line.angle());
	}

	public static Polar2d of(double r, double phi) {
		if (r == ZERO.r && phi == ZERO.phi) return ZERO;
		validateMinFp(r, 0, "Radius");
		return new Polar2d(r + .0, phi + .0);
	}

	private Polar2d(double r, double phi) {
		this.r = r;
		this.phi = phi;
	}

	public Point2d asPoint() {
		return GeometryUtil.offset(r, phi);
	}

	public Polar2d rotate(double angle) {
		return of(r, phi + angle);
	}

	public Polar2d reverse() {
		return of(r, -phi);
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, phi);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Polar2d)) return false;
		Polar2d other = (Polar2d) obj;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(phi, other.phi)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, r, phi);
	}

}
