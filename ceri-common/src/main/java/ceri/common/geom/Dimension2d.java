package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import java.util.Objects;

public class Dimension2d {
	public static final Dimension2d ZERO = new Dimension2d(0, 0);
	public final double w;
	public final double h;

	public static Dimension2d of(double w, double h) {
		validateMinFp(w, 0, "Width");
		validateMinFp(h, 0, "Height");
		if (w == 0 && h == 0) return ZERO;
		return new Dimension2d(w + .0, h + .0);
	}

	private Dimension2d(double w, double h) {
		this.w = w;
		this.h = h;
	}

	public boolean isZero() {
		return w == 0 && h == 0;
	}

	public Dimension2d resize(double ratio) {
		return Dimension2d.of(w * ratio, h * ratio);
	}

	public Dimension2d resize(Ratio2d ratio) {
		return Dimension2d.of(w * ratio.x, h * ratio.y);
	}

	public double aspectRatio() {
		if (Double.doubleToRawLongBits(w) == Double.doubleToRawLongBits(h)) return 1;
		if (w == 0) return 0;
		if (h == 0) return Double.POSITIVE_INFINITY;
		return w / h;
	}

	@Override
	public int hashCode() {
		return Objects.hash(w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Dimension2d other)) return false;
		if (!Objects.equals(w, other.w)) return false;
		if (!Objects.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + w + " x " + h + ")";
	}

}
