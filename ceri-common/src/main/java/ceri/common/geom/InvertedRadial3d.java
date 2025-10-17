package ceri.common.geom;

import java.util.Objects;
import ceri.common.text.ToString;

/**
 * A wrapper that inverts a 3d radial shape.
 */
public class InvertedRadial3d<T extends Radial3d> implements Radial3d {
	private final T radial;
	private final double h;
	private final double v;

	/**
	 * Returns a new wrapper instance.
	 */
	public static <T extends Radial3d> InvertedRadial3d<T> of(T radial) {
		return new InvertedRadial3d<>(radial);
	}

	private InvertedRadial3d(T radial) {
		this.radial = radial;
		h = radial.h();
		v = radial.volume();
	}

	/**
	 * Returns the wrapped radial shape.
	 */
	public T wrapped() {
		return radial;
	}

	@Override
	public double gradientAtH(double h) {
		double m = radial.gradientAtH(this.h - h);
		if (m == Double.NEGATIVE_INFINITY) return m;
		return -m;
	}

	@Override
	public double h() {
		return h;
	}

	@Override
	public double volume() {
		return v;
	}

	@Override
	public double volumeFromH(double h) {
		return v - radial.volumeFromH(this.h - h);
	}

	@Override
	public double hFromVolume(double v) {
		return h - radial.hFromVolume(this.v - v);
	}

	@Override
	public double radiusFromH(double h) {
		return radial.radiusFromH(this.h - h);
	}

	@Override
	public int hashCode() {
		return Objects.hash(radial);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof InvertedRadial3d<?> other) && Objects.equals(radial, other.radial);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, radial);
	}
}
