package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import java.util.Objects;
import ceri.common.text.ToString;

public class Cylinder3d implements Radial3d {
	public static final Cylinder3d NULL = new Cylinder3d(0, 0);
	private final double r;
	private final double h;
	private final double v;

	public static Cylinder3d create(double r, double h) {
		if (r == 0 && h == 0) return NULL;
		validateMinFp(r, 0, "Radius");
		validateMinFp(h, 0, "Height");
		return new Cylinder3d(r + .0, h + .0);
	}

	private Cylinder3d(double r, double h) {
		this.r = r;
		this.h = h;
		v = volume(r, h);
	}

	public double radius() {
		return r;
	}

	@Override
	public double height() {
		return h;
	}

	@Override
	public double volume() {
		return v;
	}

	@Override
	public double volumeFromHeight(double h) {
		if (h <= 0) return 0;
		if (h >= this.h) return v;
		return volume(r, h);
	}

	@Override
	public double gradientAtHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double heightFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return h;
		return Math.min(h, v / (Math.PI * r * r));
	}

	@Override
	public double radiusFromHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return r;
	}

	/**
	 * Calculates the volume of a cylinder.
	 */
	public static double volume(double r, double h) {
		return Math.PI * r * r * h;
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cylinder3d other)) return false;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, r, h);
	}

}
