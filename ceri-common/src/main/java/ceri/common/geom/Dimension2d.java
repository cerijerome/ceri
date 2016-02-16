package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Dimension2d {
	public static final Dimension2d NULL = new Dimension2d(0, 0);
	public final double w;
	public final double h;

	public Dimension2d(double w, double h) {
		validateMin(w, 0, "Width");
		validateMin(h, 0, "Height");
		this.w = w;
		this.h = h;
	}

	public boolean isNull() {
		return w == 0 && h == 0;
	}
	
	public Dimension2d resize(double ratio) {
		return new Dimension2d(w * ratio, h * ratio);
	}
	
	public Dimension2d resize(Ratio2d ratio) {
		return new Dimension2d(w * ratio.x, h * ratio.y);
	}

	public double aspectRatio() {
		if (Double.doubleToRawLongBits(w) == Double.doubleToRawLongBits(h)) return 1;
		if (w == 0) return 0;
		if (h == 0) return Double.POSITIVE_INFINITY;
		return w / h;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Dimension2d)) return false;
		Dimension2d other = (Dimension2d) obj;
		if (!EqualsUtil.equals(w, other.w)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + w + " x " + h + ")";
	}

}
