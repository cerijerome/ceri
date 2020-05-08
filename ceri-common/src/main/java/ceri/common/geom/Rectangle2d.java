package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinD;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Rectangle2d {
	public static final Rectangle2d ZERO = new Rectangle2d(0, 0, 0, 0);
	public final double x;
	public final double y;
	public final double w;
	public final double h;

	public static Rectangle2d of(Point2d position, Dimension2d size) {
		return of(position.x, position.y, size.w, size.h);
	}

	public static Rectangle2d of(double x, double y, double w, double h) {
		validateMinD(w, 0, "Width");
		validateMinD(h, 0, "Height");
		if (ZERO.equals(x, y, w, h)) return null;
		return new Rectangle2d(x + .0, y + .0, w + .0, h + .0);
	}

	private Rectangle2d(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Dimension2d size() {
		return Dimension2d.of(w, h);
	}

	public Point2d position() {
		return Point2d.of(x, y);
	}

	public Point2d corner() {
		return Point2d.of(x + w, y + h);
	}

	public double area() {
		return w * h;
	}

	private boolean equals(double x, double y, double w, double h) {
		return this.x == x && this.y == y && this.w == w && this.h == h;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(x, y, w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Rectangle2d)) return false;
		Rectangle2d other = (Rectangle2d) obj;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		if (!EqualsUtil.equals(w, other.w)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + " + " + w + ", " + y + " + " + h + ")";
	}

}
