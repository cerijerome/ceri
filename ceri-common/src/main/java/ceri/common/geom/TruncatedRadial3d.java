package ceri.common.geom;

import java.util.Objects;
import ceri.common.text.ToString;
import ceri.common.util.Validate;

/**
 * A 3d radial shape that truncates a wrapped 3d radial shape.   
 */
public class TruncatedRadial3d<T extends Radial3d> implements Radial3d {
	private final T radial;
	private final double h0; // height offset
	private final double h; // height
	private final double v0; // volume offset
	private final double v; // volume

	/**
	 * Returns an instance that truncates the given shape between the height range.
	 */
	public static <T extends Radial3d> TruncatedRadial3d<T> of(T radial, double h0, double h) {
		Validate.finiteMin(h0, 0);
		Validate.range(h, 0, radial.h() - h0);
		return new TruncatedRadial3d<>(radial, h0 + .0, h + .0);
	}

	private TruncatedRadial3d(T radial, double h0, double h) {
		this.radial = radial;
		this.h0 = h0;
		this.h = h;
		v0 = radial.volumeFromH(h0);
		v = radial.volumeFromH(h0 + h) - v0;
	}

	/**
	 * Returns the wrapped shape.
	 */
	public T wrapped() {
		return radial;
	}

	@Override
	public double gradientAtH(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return radial.gradientAtH(h0 + h);
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
		if (h <= 0) return 0;
		if (h >= this.h) return v;
		return radial.volumeFromH(h0 + h) - v0;
	}

	@Override
	public double hFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return h;
		return radial.hFromVolume(v + v0) - h0;
	}

	@Override
	public double radiusFromH(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return radial.radiusFromH(h0 + h);
	}

	@Override
	public int hashCode() {
		return Objects.hash(radial, h0, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TruncatedRadial3d<?> other)) return false;
		if (!Objects.equals(radial, other.radial)) return false;
		if (!Objects.equals(h0, other.h0)) return false;
		if (!Objects.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, h0, h, radial);
	}
}
