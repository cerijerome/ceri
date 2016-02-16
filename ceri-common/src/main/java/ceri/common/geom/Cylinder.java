package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Cylinder {
	public static final Cylinder NULL = new Cylinder(0, 0);
	public final double r;
	public final double h;

	public static Cylinder create(double r, double h) {
		if (r == 0 || h == 0) return NULL;
		validateMin(r, 0, "Radius");
		validateMin(h, 0, "Height");
		return new Cylinder(r, h);
	}

	private Cylinder(double r, double h) {
		this.r = r;
		this.h = h;
	}

	/**
	 * Calculates h from given volume.
	 */
	public double hFromVolume(double v) {
		if (isNull()) return 0;
		if (v <= 0) return 0;
		return Math.min(h, v / (Math.PI * r * r));
	}

	/**
	 * Calculates the volume between planes perpendicular to the h-axis at h0 and h1.
	 */
	public double volumeBetweenH(double h0, double h1) {
		return volume(h1) - volume(h0);
	}

	/**
	 * Calculates the cone volume.
	 */
	public double volume() {
		if (isNull()) return 0;
		return volume(r, h);
	}

	/**
	 * Calculates the volume of a cone.
	 */
	public static double volume(double r, double h) {
		return Math.PI * r * r * h;
	}

	/**
	 * Is this a null cylinder?
	 */
	public boolean isNull() {
		return r == 0;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cylinder)) return false;
		Cylinder other = (Cylinder) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, h).toString();
	}

	private double volume(double h) {
		if (isNull()) return 0;
		if (h <= 0) return 0;
		if (h > this.h) return volume();
		return volume(r, h);
	}

}
