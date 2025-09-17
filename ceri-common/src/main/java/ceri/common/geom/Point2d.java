package ceri.common.geom;

import java.util.Objects;
import ceri.common.util.Validate;

public class Point2d {
	public static final Point2d NULL = new Point2d(Double.NaN, Double.NaN);
	public static final Point2d ZERO = new Point2d(0, 0);
	public static final Point2d X_UNIT = new Point2d(1, 0);
	public static final Point2d Y_UNIT = new Point2d(0, 1);
	public final double x;
	public final double y;

	public static Point2d of(double x, double y) {
		Validate.validate(!Double.isNaN(x), "x");
		Validate.validate(!Double.isNaN(y), "y");
		if (ZERO.equals(x, y)) return ZERO;
		if (X_UNIT.equals(x, y)) return X_UNIT;
		if (Y_UNIT.equals(x, y)) return Y_UNIT;
		return new Point2d(x + .0, y + .0);
	}

	private Point2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public boolean isNull() {
		return Double.isNaN(x);
	}

	public Point2d reverse() {
		return Point2d.of(-x, -y);
	}

	public Point2d translate(Point2d offset) {
		return translate(offset.x, offset.y);
	}

	public Point2d translate(double x, double y) {
		return Point2d.of(this.x + x, this.y + y);
	}

	public Point2d to(Point2d end) {
		return Point2d.of(end.x - x, end.y - y);
	}

	public Point2d from(Point2d start) {
		return start.to(this);
	}

	public Point2d scale(Ratio2d scale) {
		// +0.0 converts any -0.0 to 0.0
		return Point2d.of(x * scale.x + 0.0, y * scale.y + 0.0);
	}

	public double distance() {
		return Math.sqrt(quadrance());
	}

	public double quadrance() {
		return x * x + y * y;
	}

	public double distanceTo(Point2d end) {
		return to(end).distance();
	}

	private boolean equals(double x, double y) {
		return this.x == x && this.y == y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Point2d other)) return false;
		if (!Objects.equals(x, other.x)) return false;
		if (!Objects.equals(y, other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
