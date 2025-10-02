package ceri.common.geom;

import java.util.Objects;
import ceri.common.text.ToString;
import ceri.common.util.Validate;

/**
 * Describes an ellipsoid with equal radii on at least the non-vertical axes. A simplified rugby
 * ball on its end.
 */
public class Spheroid3d implements Radial3d {
	public static final Spheroid3d NULL = new Spheroid3d(0, 0);
	private final Ellipsoid3d ellipsoid;
	private final Ellipse ellipse;
	public final double c;
	public final double r;
	private final double v;

	public static Spheroid3d create(double r, double c) {
		if (r == 0.0 && c == 0.0) return NULL;
		Validate.finiteMin(r, 0.0);
		Validate.finiteMin(c, 0.0);
		return new Spheroid3d(r + 0.0, c + 0.0);
	}

	private Spheroid3d(double r, double c) {
		ellipsoid = Ellipsoid3d.create(r, r, c);
		ellipse = Ellipse.of(r, c);
		v = ellipsoid.volume();
		this.r = r;
		this.c = c;
	}

	@Override
	public double gradientAtHeight(double h) {
		return ellipse.gradientAtY(h - c);
	}

	@Override
	public double heightFromVolume(double v) {
		return ellipsoid.zFromVolume(v) + c;
	}

	@Override
	public double radiusFromHeight(double h) {
		return ellipse.xFromY(h - c);
	}

	@Override
	public double volumeFromHeight(double h) {
		return ellipsoid.volumeToZ(h - c);
	}

	@Override
	public double height() {
		return c * 2;
	}

	@Override
	public double volume() {
		return v;
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Spheroid3d other)) return false;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, r, c);
	}

}
