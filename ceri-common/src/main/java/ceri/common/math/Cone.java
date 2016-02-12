package ceri.common.math;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Cone {
	private final double r;
	private final double h;

	public Cone(double r, double h) {
		if (r <= 0) throw new IllegalArgumentException("Radius must be > 0: " + r);
		if (h <= 0) throw new IllegalArgumentException("Height must be > 0: " + h);
		this.r = r;
		this.h = h;
	}

	/**
	 * Calculates h from given volume.
	 */
	public double hFromVolume(double v) {
		return Math.cbrt(3.0 * h * h * v / (Math.PI * r * r));
	}
	
	/**
	 * Returns the volume between planes perpendicular to the h-axis at h0 and h1.
	 */
	public double volumeBetweenH(double h0, double h1) {
		return volume(h1) - volume(h0);
	}
	
	/**
	 * Returns the cone volume.
	 */
	public double volume() {
		return volume(r, h);
	}

	/**
	 * Returns the volume of a cone.
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
		if (h0 < 0) return Double.NaN;
		return volume(r * h0 / h, h0);
	}
	
}
