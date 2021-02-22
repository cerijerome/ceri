package ceri.common.color;

import java.awt.Color;
import java.util.Objects;

public class XyzColor implements ComponentColor<XyzColor> {
	public static final XyzColor CIE_E = of(1.0, 1.0, 1.0);
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

	public XybColor xyb() {
		double[] xyb = ColorSpaces.xyzToXyb(x, y, z);
		return XybColor.of(a, xyb[0], xyb[1], xyb[2]);
	}

	public int argb() {
		return ColorUtil.alphaArgb(ColorUtil.value(a), ColorSpaces.xyzToRgb(x, y, z));
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_ALPHA;
	}

	@Override
	public XyzColor normalize() {
		XybColor xyb = xyb();
		XybColor normalXyb = xyb.normalize();
		return xyb == normalXyb ? this : normalXyb.xyz();
	}

	@Override
	public XyzColor limit() {
		XybColor xyb = xyb();
		XybColor limitXyb = xyb.limit();
		return xyb == limitXyb ? this : limitXyb.xyz();
	}

	@Override
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
