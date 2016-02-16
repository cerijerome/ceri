package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Cone {
	public static final Cone NULL = new Cone(0, 0);
	public final double r;
	public final double h;

	public static Cone create(double r, double h) {
		if (r == 0 || h == 0) return NULL;
		validateMin(r, 0, "Radius");
		validateMin(h, 0, "Height");
		return new Cone(r, h);
	}

	private Cone(double r, double h) {
		this.r = r;
		this.h = h;
	}

	/**
	 * Angle of the cone.
	 */
	public double angle() {
		if (isNull()) return 0;
		return Math.atan2(h, r);
	}

	/**
	 * Gradient of the cone.
	 */
	public double gradient() {
		if (isNull()) return 0;
		return h / r;
	}

	/**
	 * Calculates h from given volume, with h starting at the apex.
	 */
	public double hFromVolume(double v) {
		if (isNull()) return 0;
		if (v <= 0) return 0;
		return Math.min(h, Math.cbrt(3.0 * h * h * v / (Math.PI * r * r)));
	}

	/**
	 * Calculates r from given h, with h starting at the apex.
	 */
	public double rFromH(double h) {
		if (isNull()) return 0;
		if (h <= 0 || h > this.h) return 0;
		return this.r * h / this.h;
	}

	/**
	 * Calculates h from given r, with h starting at the apex.
	 */
	public double hFromR(double r) {
		if (isNull()) return 0;
		if (r <= 0 || r > this.r) return 0;
		return this.h * r / this.r;
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
		return volume(r, h);
	}

	/**
	 * Is this a null cone?
	 */
	public boolean isNull() {
		return h == 0;
	}

	/**
	 * Calculates the volume of a cone.
	 */
	public static double volume(double r, double h) {
		return Math.PI * r * r * h / 3;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cone)) return false;
		Cone other = (Cone) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, h).toString();
	}

	private double volume(double h0) {
		if (isNull()) return 0;
		if (h0 <= 0) return 0;
		if (h0 > h) return volume();
		return volume(r * h0 / h, h0);
	}

}
