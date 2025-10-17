package ceri.common.geom;

import ceri.common.test.AssertUtil;

public class GeomAssert {

	private GeomAssert() {}

	public static void approx(Point2d p, double x, double y) {
		AssertUtil.approx(p.x(), x, "x");
		AssertUtil.approx(p.y(), y, "y");
	}

	public static void approx(Ratio2d r, double x, double y) {
		AssertUtil.approx(r.x(), x, "x");
		AssertUtil.approx(r.y(), y, "y");
	}

	public static void approx(Size2d s, double w, double h) {
		AssertUtil.approx(s.w(), w, "w");
		AssertUtil.approx(s.h(), h, "h");
	}

	public static void approx(Polar2d p, double r, double phi) {
		AssertUtil.approx(p.r(), r, "r");
		AssertUtil.approx(p.phi(), phi, "phi");
	}

	public static void approx(Rectangle r, double x, double y, double w, double h) {
		AssertUtil.approx(r.x(), x, "x");
		AssertUtil.approx(r.y(), y, "y");
		AssertUtil.approx(r.w(), w, "w");
		AssertUtil.approx(r.h(), h, "h");
	}

	public static void approx(Line2d l, double fromX, double fromY, double toX, double toY) {
		AssertUtil.approx(l.from().x(), fromX, "from.x");
		AssertUtil.approx(l.from().y(), fromY, "from.y");
		AssertUtil.approx(l.to().x(), toX, "to.x");
		AssertUtil.approx(l.to().y(), toY, "to.y");
	}
	
	public static void approx(Line2d.Equation e, double a, double b, double c) {
		AssertUtil.approx(e.a(), a, "a");
		AssertUtil.approx(e.b(), b, "b");
		AssertUtil.approx(e.c(), c, "c");
	}
	
	public static void approx(Ellipse e, double a, double b) {
		AssertUtil.approx(e.a(), a, "a");
		AssertUtil.approx(e.b(), b, "b");
	}

	public static void approx(Cylinder c, double r, double h) {
		AssertUtil.approx(c.r(), r, "r");
		AssertUtil.approx(c.h(), h, "h");
	}
	
	public static void approx(Cone c, double r, double h) {
		AssertUtil.approx(c.r(), r, "r");
		AssertUtil.approx(c.h(), h, "h");
	}
}
