package ceri.common.geom;

import ceri.common.test.Assert;

public class GeomAssert {

	private GeomAssert() {}

	public static void approx(Point2d p, double x, double y) {
		Assert.approx(p.x(), x, "x");
		Assert.approx(p.y(), y, "y");
	}

	public static void approx(Ratio2d r, double x, double y) {
		Assert.approx(r.x(), x, "x");
		Assert.approx(r.y(), y, "y");
	}

	public static void approx(Size2d s, double w, double h) {
		Assert.approx(s.w(), w, "w");
		Assert.approx(s.h(), h, "h");
	}

	public static void approx(Polar2d p, double r, double phi) {
		Assert.approx(p.r(), r, "r");
		Assert.approx(p.phi(), phi, "phi");
	}

	public static void approx(Rectangle r, double x, double y, double w, double h) {
		Assert.approx(r.x(), x, "x");
		Assert.approx(r.y(), y, "y");
		Assert.approx(r.w(), w, "w");
		Assert.approx(r.h(), h, "h");
	}

	public static void approx(Line2d l, double fromX, double fromY, double toX, double toY) {
		Assert.approx(l.from().x(), fromX, "from.x");
		Assert.approx(l.from().y(), fromY, "from.y");
		Assert.approx(l.to().x(), toX, "to.x");
		Assert.approx(l.to().y(), toY, "to.y");
	}
	
	public static void approx(Line2d.Equation e, double a, double b, double c) {
		Assert.approx(e.a(), a, "a");
		Assert.approx(e.b(), b, "b");
		Assert.approx(e.c(), c, "c");
	}
	
	public static void approx(Ellipse e, double a, double b) {
		Assert.approx(e.a(), a, "a");
		Assert.approx(e.b(), b, "b");
	}

	public static void approx(Cylinder c, double r, double h) {
		Assert.approx(c.r(), r, "r");
		Assert.approx(c.h(), h, "h");
	}
	
	public static void approx(Cone c, double r, double h) {
		Assert.approx(c.r(), r, "r");
		Assert.approx(c.h(), h, "h");
	}
}
