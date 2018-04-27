package ceri.common.color;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class XyzColor implements ComponentColor<XyzColor> {
	public static final XyzColor D50 = of(0.964212, 1.0, 0.825188);
	public static final XyzColor D55 = of(0.956797, 1.0, 0.921481);
	public static final XyzColor D65 = of(0.950429, 1.0, 1.088900);
	public static final XyzColor D75 = of(0.949722, 1.0, 1.226394);

	public static final XyzColor CIE_A = of(1.0985, 1.0, 0.3558);
	public static final XyzColor CIE_C = of(0.9807, 1.0, 1.1822);
	public static final XyzColor CIE_E = of(1.0, 1.0, 1.0);
	public static final XyzColor CIE_D50 = of(0.9642, 1.0, 0.8251);
	public static final XyzColor CIE_D55 = of(0.9568, 1.0, 0.9214);
	public static final XyzColor CIE_D65 = of(0.9504, 1.0, 1.0888);
	public static final XyzColor CIE_ICC = of(0.9642, 1.0, 0.8249);

	public static final double MAX_ALPHA = 1.0;
	public final double x;
	public final double y;
	public final double z;
	public final double a;

	public static XyzColor of(double x, double y, double z) {
		return new XyzColor(x, y, z, MAX_ALPHA);
	}

	public static XyzColor of(double x, double y, double z, double a) {
		return new XyzColor(x, y, z, a);
	}

	private XyzColor(double x, double y, double z, double a) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
	}

	public XybColor toXyb() {
		double sum = x + y + z;
		if (sum == 0.0) return XybColor.of(0, 0, 0, a);
		return XybColor.of(x / sum, y / sum, y, a);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_ALPHA;
	}

	@Override
	public XyzColor normalize() {
		XybColor xyb = toXyb();
		XybColor normalXyb = xyb.normalize();
		if (xyb == normalXyb) return this;
		return normalXyb.toXyz();
	}

	@Override
	public XyzColor limit() {
		XybColor xyb = toXyb();
		XybColor limitXyb = xyb.limit();
		if (xyb == limitXyb) return this;
		return limitXyb.toXyz();
	}

	@Override
	public void verify() {
		toXyb().verify();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(x, y, z, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof XyzColor)) return false;
		XyzColor other = (XyzColor) obj;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		if (!EqualsUtil.equals(z, other.z)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		return hasAlpha() ? String.format("(x=%.5f,y=%.5f,z=%.5f,a=%.5f)", x, y, z, a) :
			String.format("(x=%.5f,y=%.5f,z=%.5f)", x, y, z);
	}

}
