package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.stream.DoubleStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.math.MathUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class TruncatedSpheroid {
	public static final TruncatedSpheroid NULL = new TruncatedSpheroid(0, 0, 0, 0);
	private final Ellipsoid ellipsoid;
	private final Ellipse ellipse;
	private final double V0;
	private final double h0;
	private final double V;
	public final double h;

	public static TruncatedSpheroid create(double r, double c, double h0, double h) {
		if (r == 0 || c == 0 || h == 0) return NULL;
		validateMin(r, 0, "Radius");
		validateMin(c, 0, "Axis c");
		validateRange(h0, -c, c, "Height offset");
		validateRange(h, 0, c - h0, "Height");
		return new TruncatedSpheroid(r, c, h0, h);
	}

	private TruncatedSpheroid(double r, double c, double h0, double h) {
		ellipsoid = Ellipsoid.create(r, r, c);
		ellipse = Ellipse.create(r, c);
		V0 = ellipsoid.volumeBetweenZ(-c, h0);
		V = ellipsoid.volumeBetweenZ(-c, h0 + h) - V0;
		this.h0 = h0;
		this.h = h;
	}

	/**
	 * Gradient of the spheroid.
	 */
	public double gradientFromH(double h) {
		if (isNull()) return 0;
		if (h < 0 || h > this.h) return Double.NaN;
		return ellipse.gradientFromY(h + h0);
	}

	/**
	 * Calculates h from given volume, with h starting from the base.
	 */
	public double hFromVolume(double v) {
		if (isNull()) return 0;
		if (v <= 0) return 0;
		if (v >= V) return h;
		return ellipsoid.zFromVolume(v + V0) - h0;
	}

	/**
	 * Calculates r from given h, with h starting from the base.
	 */
	public double rFromH(double h) {
		if (isNull()) return 0;
		if (h < 0 || h > this.h) return 0;
		return ellipse.xFromY(h + h0);
	}

	/**
	 * Calculates h from given r, with h starting from the base. May return up to two values.
	 */
	public double[] hFromR(double r) {
		if (isNull() || r < 0) return ArrayUtil.EMPTY_DOUBLE;
		double h = ellipse.yFromX(r);
		if (Double.isNaN(h)) return ArrayUtil.EMPTY_DOUBLE;
		double[] values = h == 0 ? new double[] { h } : new double[] { -h, h };
		return DoubleStream.of(values).map(d -> d - h0).filter(d -> (d >= 0 && d <= this.h))
			.toArray();
	}

	/**
	 * Calculates the volume between planes perpendicular to the h-axis at h0 and h1.
	 */
	public double volumeBetweenH(double h0, double h1) {
		if (isNull()) return 0;
		h0 = MathUtil.limit(h0, 0, h);
		h1 = MathUtil.limit(h1, 0, h);
		return ellipsoid.volumeBetweenZ(h0 + this.h0, h1 + this.h0);
	}

	/**
	 * Height of the shape.
	 */
	public double height() {
		return h;
	}

	/**
	 * Shape volume.
	 */
	public double volume() {
		return V;
	}

	/**
	 * Is this a null truncated spheroid?
	 */
	public boolean isNull() {
		return ellipsoid.isNull();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(ellipse.a, ellipse.b, h0, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TruncatedSpheroid)) return false;
		TruncatedSpheroid other = (TruncatedSpheroid) obj;
		if (!EqualsUtil.equals(ellipse.a, other.ellipse.a)) return false;
		if (!EqualsUtil.equals(ellipse.b, other.ellipse.b)) return false;
		if (!EqualsUtil.equals(h0, other.h0)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, ellipse.a, ellipse.b, h0, h).toString();
	}

}
