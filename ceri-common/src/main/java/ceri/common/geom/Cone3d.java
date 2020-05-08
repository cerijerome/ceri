package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinD;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * A cone with its apex at h = 0.
 */
public class Cone3d implements Radial3d {
	public static final Cone3d NULL = new Cone3d(0, 0);
	private final double r;
	private final double h;
	private final double v;

	public static Cone3d create(double r, double h) {
		if (r == 0 && h == 0) return NULL;
		validateMinD(r, 0, "Radius");
		validateMinD(h, 0, "Height");
		return new Cone3d(r + .0, h + .0);
	}

	private Cone3d(double r, double h) {
		this.r = r;
		this.h = h;
		v = volume(r, h);
	}

	/**
	 * Gradient of the cone.
	 */
	public double gradient() {
		return h / r;
	}

	@Override
	public double gradientAtHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return gradient();
	}

	@Override
	public double height() {
		return h;
	}

	public double radius() {
		return r;
	}

	@Override
	public double heightFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return h;
		return Math.cbrt(3.0 * h * h * v / (Math.PI * r * r));
	}

	@Override
	public double volumeFromHeight(double h) {
		if (h <= 0) return 0;
		if (h >= this.h) return v;
		return volume(radiusFromHeight(h), h);
	}

	@Override
	public double radiusFromHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		if (h == 0) return 0;
		if (h == this.h) return r;
		return this.r * h / this.h;
	}

	public double heightFromRadius(double r) {
		if (r < 0 || r > this.r) return Double.NaN;
		if (r == 0) return 0;
		if (r == this.r) return h;
		return this.h * r / this.r;
	}

	@Override
	public double volume() {
		return v;
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
		if (!(obj instanceof Cone3d)) return false;
		Cone3d other = (Cone3d) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r, h).toString();
	}

}
