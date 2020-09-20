package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.util.Objects;
import ceri.common.text.ToString;

public class TruncatedRadial3d<T extends Radial3d> implements Radial3d {
	private final T radial;
	private final double h0;
	private final double h;
	private final double v0;
	private final double v;

	public static <T extends Radial3d> TruncatedRadial3d<T> create(T radial, double h0, double h) {
		validateMinFp(h0, 0, "Height offset");
		validateRangeFp(h, 0, radial.height() - h0, "Height");
		return new TruncatedRadial3d<>(radial, h0 + .0, h + .0);
	}

	private TruncatedRadial3d(T radial, double h0, double h) {
		this.radial = radial;
		this.h0 = h0;
		this.h = h;
		v0 = radial.volumeFromHeight(h0);
		v = radial.volumeFromHeight(h0 + h) - v0;
	}

	public T wrapped() {
		return radial;
	}

	@Override
	public double gradientAtHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return radial.gradientAtHeight(h0 + h);
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
		return radial.volumeFromHeight(h0 + h) - v0;
	}

	@Override
	public double heightFromVolume(double v) {
		if (v < 0 || v > this.v) return Double.NaN;
		if (v == 0) return 0;
		if (v == this.v) return h;
		return radial.heightFromVolume(v + v0) - h0;
	}

	@Override
	public double radiusFromHeight(double h) {
		if (h < 0 || h > this.h) return Double.NaN;
		return radial.radiusFromHeight(h0 + h);
	}

	@Override
	public int hashCode() {
		return Objects.hash(radial, h0, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TruncatedRadial3d)) return false;
		TruncatedRadial3d<?> other = (TruncatedRadial3d<?>) obj;
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
