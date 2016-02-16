package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Rectangle {
	public static final Rectangle NULL = new Rectangle(0, 0, 0, 0);
	public final double x;
	public final double y;
	public final double w;
	public final double h;

	public Rectangle(Point2d position, Dimension2d size) {
		this(position.x, position.y, size.w, size.h);
	}

	public Rectangle(double x, double y, double w, double h) {
		validateMin(w, 0, "Width");
		validateMin(h, 0, "Height");
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Dimension2d size() {
		return new Dimension2d(w, h);
	}

	public Point2d position() {
		return new Point2d(x, y);
	}
	
	public Point2d corner() {
		return new Point2d(x + w, y + h);
	}

	public double area() {
		return w * h;
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(x, y, w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Rectangle)) return false;
		Rectangle other = (Rectangle) obj;
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
