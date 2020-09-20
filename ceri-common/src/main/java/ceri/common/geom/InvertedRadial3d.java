package ceri.common.geom;

import java.util.Objects;
import ceri.common.text.ToString;

public class InvertedRadial3d<T extends Radial3d> implements Radial3d {
	private final T radial;
	private final double h;
	private final double v;

	public static <T extends Radial3d> InvertedRadial3d<T> create(T radial) {
		return new InvertedRadial3d<>(radial);
	}

	private InvertedRadial3d(T radial) {
		this.radial = radial;
		h = radial.height();
		v = radial.volume();
	}

	public T wrapped() {
		return radial;
	}

	@Override
	public double gradientAtHeight(double h) {
		double m = radial.gradientAtHeight(this.h - h);
		if (m == Double.NEGATIVE_INFINITY) return m;
		return -m;
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
		return v - radial.volumeFromHeight(this.h - h);
	}

	@Override
	public double heightFromVolume(double v) {
		return h - radial.heightFromVolume(this.v - v);
	}

	@Override
	public double radiusFromHeight(double h) {
		return radial.radiusFromHeight(this.h - h);
	}

	@Override
	public int hashCode() {
		return Objects.hash(radial);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof InvertedRadial3d)) return false;
		InvertedRadial3d<?> other = (InvertedRadial3d<?>) obj;
		if (!Objects.equals(radial, other.radial)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, radial);
	}

}
