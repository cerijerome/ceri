package ceri.common.geom;

import ceri.common.util.Validate;

/**
 * Represents a rectangle with 2d coordinates and size.
 */
public record Rectangle(double x, double y, double w, double h) {
	public static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);

	/**
	 * Returns a validated instance.
	 */
	public static Rectangle square(Point2d position, double size) {
		return square(position.x(), position.y(), size);
	}

	/**
	 * Returns a validated instance.
	 */
	public static Rectangle square(double x, double y, double size) {
		return of(x, y, size, size);
	}

	/**
	 * Returns a validated instance.
	 */
	public static Rectangle of(Point2d position, Size2d size) {
		return of(position.x(), position.y(), size.w(), size.h());
	}

	/**
	 * Returns a validated instance.
	 */
	public static Rectangle of(double x, double y, double w, double h) {
		if (ZERO.equals(x, y, w, h)) return ZERO;
		return new Rectangle(x + 0.0, y + 0.0, w + 0.0, h + 0.0);
	}

	/**
	 * Constructor validation.
	 */
	public Rectangle {
		Validate.finite(x);
		Validate.finite(y);
		Validate.finiteMin(w, 0.0);
		Validate.finiteMin(h, 0.0);
	}

	/**
	 * Returns the width and height.
	 */
	public Size2d size() {
		return Size2d.of(w(), h());
	}

	/**
	 * Returns the x,y coordinates.
	 */
	public Point2d position() {
		return Point2d.of(x(), y());
	}

	/**
	 * Returns the opposite corner coordinate from x,y.
	 */
	public Point2d corner() {
		return Point2d.of(x() + w(), y() + h());
	}

	/**
	 * Calculates the area.
	 */
	public double area() {
		return Size2d.area(w(), h());
	}

	/**
	 * Returns the overlap with the given rectangle, or empty if no overlap.
	 */
	public Rectangle overlap(Rectangle other) {
		double x = Math.max(x(), other.x());
		double xw = Math.min(x() + w(), other.x() + other.w());
		double y = Math.max(y(), other.y());
		double yh = Math.min(y() + h(), other.y() + other.h());
		if (y > yh || x > xw) return Rectangle.ZERO;
		return create(x, y, xw - x, yh - y);
	}

	/**
	 * Returns true if this rectangle matches the coordinates and size.
	 */
	public boolean equals(Point2d position, Size2d size) {
		return equals(position.x(), position.y(), size.w(), size.h());
	}

	/**
	 * Returns true if this rectangle matches the coordinates and size.
	 */
	public boolean equals(double x, double y, double w, double h) {
		return x() == x && y() == y && w() == w && h() == h;
	}

	@Override
	public String toString() {
		return "(" + x() + " + " + w() + ", " + y() + " + " + h() + ")";
	}

	private Rectangle create(double x, double y, double w, double h) {
		if (x() == x && y() == y && w() == w && h() == h) return this;
		return of(x, y, w, h);
	}
}
