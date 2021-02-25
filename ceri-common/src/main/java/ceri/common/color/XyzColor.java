package ceri.common.color;

import java.awt.Color;
import java.util.Objects;
import ceri.common.color.ColorSpaces.Luv;

public class XyzColor {
	public static final XyzColor CIE_A = of(1.09850, 1.0, 0.35585);
	public static final XyzColor CIE_B = of(0.99072, 1.0, 0.85223);
	public static final XyzColor CIE_C = of(0.98074, 1.0, 1.18232);
	public static final XyzColor CIE_D50 = of(0.96422, 1.0, 0.82521);
	public static final XyzColor CIE_D55 = of(0.95682, 1.0, 0.92149);
	public static final XyzColor CIE_D65 = of(0.95047, 1.0, 1.08883);
	public static final XyzColor CIE_D75 = of(0.94972, 1.0, 1.22638);
	public static final XyzColor CIE_E = of(1.0, 1.0, 1.0);
	public static final XyzColor CIE_F2 = of(0.99186, 1.0, 0.67393);
	public static final XyzColor CIE_F7 = of(0.95041, 1.0, 1.08747);
	public static final XyzColor CIE_F11 = of(1.00962, 1.0, 0.64350);
	public static final double MAX_ALPHA = 1.0;
	public final double a;
	public final double x;
	public final double y;
	public final double z;

	public static XyzColor from(int argb) {
		double[] xyz = ColorSpaces.rgbToXyz(argb);
		return of(ColorUtil.ratio(ColorUtil.a(argb)), xyz[0], xyz[1], xyz[2]);
	}

	public static XyzColor of(double x, double y, double z) {
		return new XyzColor(MAX_ALPHA, x, y, z);
	}

	public static XyzColor of(double a, double x, double y, double z) {
		return new XyzColor(a, x, y, z);
	}

	private XyzColor(double a, double x, double y, double z) {
		this.a = a;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double[] values() {
		return new double[] { x, y, z };
	}

	public double[] xybValues() {
		return ColorSpaces.xyzToXyb(x, y, z);
	}

	public XybColor xyb() {
		double[] xyb = xybValues();
		return XybColor.of(a, xyb[0], xyb[1], xyb[2]);
	}

	public int argb() {
		return ColorUtil.alphaArgb(ColorUtil.value(a), ColorSpaces.xyzToRgb(x, y, z));
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Provides a L*u*v* converter using this color as the reference.
	 */
	public Luv luv() {
		return Luv.fromXyz(x, y, z);
	}

	/**
	 * Calculates L*u*v* values using the given L*u*v* reference converter.
	 */
	public double[] luvValues(Luv luv) {
		return luv.xyzToLuv(x, y, z);
	}

	public boolean hasAlpha() {
		return a < MAX_ALPHA;
	}

	public XyzColor normalize() {
		XybColor xyb = xyb();
		XybColor normalXyb = xyb.normalize();
		return xyb == normalXyb ? this : normalXyb.xyz();
	}

	public XyzColor limit() {
		XybColor xyb = xyb();
		XybColor limitXyb = xyb.limit();
		return xyb == limitXyb ? this : limitXyb.xyz();
	}

	public void verify() {
		xyb().verify();
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof XyzColor)) return false;
		XyzColor other = (XyzColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(x, other.x)) return false;
		if (!Objects.equals(y, other.y)) return false;
		if (!Objects.equals(z, other.z)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,z=%.5f)", a, x, y, z);
	}
}
