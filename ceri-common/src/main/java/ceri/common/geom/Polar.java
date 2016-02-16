package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.*;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Polar {
	public static final Polar ZERO = new Polar(0, 0);
	public final double r;
	public final double phi;

	public static Polar from(Point2d point) {
		if (point.equals(Point2d.ZERO)) return ZERO;
		Line2d line = Line2d.create(Point2d.ZERO, point);
		return new Polar(line.length(), line.angle());
	}
	
	public static Polar create(double r, double phi) {
		validateMin(r, 0, "Radius");
		return new Polar(r, phi);
	}
	
	private Polar(double r, double phi) {
		this.r = r;
		this.phi = phi;
	}

	public Point2d asPoint() {
		return new Point2d(r * Math.cos(phi), r * Math.sin(phi));
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(r, phi);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Polar)) return false;
		Polar other = (Polar) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(phi, other.phi)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, phi).toString();
	}

}
