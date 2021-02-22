package ceri.common.color;

import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.geom.Point2d;
import ceri.common.math.MathUtil;

/**
 * Represents xyY color, Y = brightness (b).
 */
public class XybColor implements ComponentColor<XybColor> {
	public static final double MAX_VALUE = 1.0;
	// CIE standard illuminants https://en.wikipedia.org/wiki/Standard_illuminant#White_point
	public static final XybColor CIE_A = of(0.44757, 0.40745, 1.0);
	public static final XybColor CIE_B = of(0.34842, 0.35161, 1.0);
	public static final XybColor CIE_C = of(0.31006, 0.31616, 1.0);
	public static final XybColor CIE_D50 = of(0.34567, 0.35850, 1.0);
	public static final XybColor CIE_D55 = of(0.33242, 0.34743, 1.0);
	public static final XybColor CIE_D65 = of(0.31271, 0.32902, 1.0);
	public static final XybColor CIE_D75 = of(0.29902, 0.31485, 1.0);
	public static final XybColor CIE_D93 = of(0.28315, 0.29711, 1.0);
	public static final XybColor CIE_E = XyzColor.CIE_E.xyb();
	public static final Point2d CENTER = CIE_E.xy();
	public final double a;
	public final double x;
	public final double y;
	public final double b;

	public static XybColor from(int argb) {
		double[] xyb = ColorSpaces.rgbToXyb(argb);
		return of(ColorUtil.ratio(ColorUtil.a(argb)), xyb[0], xyb[1], xyb[2]);
	}

	public static XybColor full(Point2d xy) {
		return full(xy.x, xy.y);
	}

	public static XybColor full(double x, double y) {
		return of(x, y, MAX_VALUE);
	}

	public static XybColor of(Point2d xy, double b) {
		return of(xy.x, xy.y, b);
	}

	public static XybColor of(double x, double y, double b) {
		return of(MAX_VALUE, x, y, b);
	}

	public static XybColor of(double a, Point2d xy, double b) {
		return of(a, xy.x, xy.y, b);
	}

	public static XybColor of(double a, double x, double y, double b) {
		return new XybColor(x, y, b, a);
	}

	private XybColor(double x, double y, double b, double a) {
		this.x = x;
		this.y = y;
		this.b = b;
		this.a = a;
	}

	public double[] values() {
		return new double[] { x, y, b };
	}

	public Point2d xy() {
		return Point2d.of(x, y);
	}

	public XyzColor xyz() {
		double[] xyz = ColorSpaces.xybToXyz(x, y, b);
		return XyzColor.of(a, xyz[0], xyz[1], xyz[2]);
	}

	public int argb() {
		return ColorUtil.alphaArgb(ColorUtil.value(a), ColorSpaces.xybToRgb(x, y, b));
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public XybColor normalize() {
		double a = limit(this.a);
		Point2d xy = normalize(x, y);
		double b = limit(this.b);
		if (a == this.a && xy.x == this.x && xy.y == this.y && b == this.b) return this;
		return of(a, xy, b);
	}

	public XybColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, x, y, b * ratio);
	}

	private Point2d normalize(double x, double y) {
		// normalize around CIE_E
		double factor = MathUtil.max(MAX_VALUE, //
			(x - CENTER.x) / (MAX_VALUE - CENTER.x), (CENTER.x - x) / CENTER.x,
			(y - CENTER.y) / (MAX_VALUE - CENTER.y), (CENTER.y - y) / CENTER.y);
		if (factor == MAX_VALUE) return Point2d.of(x, y);
		x = ((x - CENTER.x) / factor) + CENTER.x;
		y = ((y - CENTER.y) / factor) + CENTER.y;
		return Point2d.of(x, y);
	}

	@Override
	public XybColor limit() {
		double a = limit(this.a);
		double x = limit(this.x);
		double y = limit(this.y);
		double b = limit(this.b);
		if (a == this.a && x == this.x && y == this.y && b == this.b) return this;
		return of(a, x, y, b);
	}

	@Override
	public void verify() {
		validate(a, "alpha");
		validate(x, "x");
		validate(y, "y");
		validate(b, "brightness");
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, x, y, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof XybColor)) return false;
		XybColor other = (XybColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(x, other.x)) return false;
		if (!Objects.equals(y, other.y)) return false;
		if (!Objects.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,b=%.5f)", a, x, y, b);
	}
}
