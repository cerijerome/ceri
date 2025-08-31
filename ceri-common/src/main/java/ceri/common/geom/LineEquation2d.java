package ceri.common.geom;

import java.util.Objects;
import ceri.common.text.Strings;

/**
 * Line formula ax + by + c = 0.
 */
public class LineEquation2d {
	public static final LineEquation2d NULL = new LineEquation2d(0, 0, 0);
	public static final LineEquation2d X_AXIS = of(Line2d.X_UNIT);
	public static final LineEquation2d Y_AXIS = of(Line2d.Y_UNIT);
	public final double a;
	public final double b;
	public final double c;

	public static LineEquation2d of(Line2d line) {
		return between(line.from, line.to);
	}

	public static LineEquation2d between(Point2d from, Point2d to) {
		if (from.equals(to)) return NULL;
		double dx = to.x - from.x + .0;
		double dy = to.y - from.y + .0;
		if (dx == 0) return of(1, 0, -from.x);
		if (dy == 0) return of(0, 1, -from.y);
		double b = -dx / dy;
		double c = -from.x - (b * from.y);
		return of(1, b, c);
	}

	public static LineEquation2d of(double a, double b, double c) {
		if (a == 0 && b == 0) return NULL;
		return new LineEquation2d(a + .0, b + .0, c + .0);
	}

	private LineEquation2d(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Point2d reflect(Point2d point) {
		if (isNull()) return point;
		return reflect(point.x, point.y);
	}

	public Point2d reflect(double x, double y) {
		if (isNull()) return Point2d.of(x, y);
		double aa = a * a;
		double bb = b * b;
		double newX = ((x * (bb - aa)) - (y * a * b * 2) - (a * c * 2)) / (aa + bb);
		double newY = ((y * (aa - bb)) - (x * a * b * 2) - (b * c * 2)) / (aa + bb);
		return Point2d.of(newX, newY);
	}

	public double angle() {
		if (isNull()) return Double.NaN;
		if (b <= 0) return Math.atan2(a, -b);
		return Math.atan2(-a, b);
	}

	public double gradient() {
		if (isNull()) return Double.NaN;
		if (a == 0) return 0;
		if (b == 0) return Double.POSITIVE_INFINITY;
		return -a / b;
	}

	public LineEquation2d normalize() {
		if (isNull()) return this;
		if (a != 0) return new LineEquation2d(1, 0.0 + b / a, 0.0 + c / a); // avoid -0.0
		return new LineEquation2d(0, 1, 0.0 + c / b); // avoid -0.0
	}

	public boolean isNull() {
		return a == 0 && b == 0;
	}

	public double distanceTo(Point2d point) {
		return distanceTo(point.x, point.y);
	}

	public double distanceTo(double x, double y) {
		if (isNull()) return Double.NaN;
		return Math.abs((a * x) + (b * y) + c) / Math.sqrt((a * a) + (b * b));
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LineEquation2d other)) return false;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(b, other.b)) return false;
		if (!Objects.equals(c, other.c)) return false;
		return true;
	}

	@Override
	public String toString() {
		if (isNull()) return "0 = 0";
		StringBuilder s = new StringBuilder();
		addTerm(s, a, "x");
		addTerm(s, b, "y");
		addTerm(s, c, "");
		return s.append(" = 0").toString();
	}

	private void addTerm(StringBuilder b, double d, String suffix) {
		if (d == 0) return;
		if (d < 0) b.append(b.length() == 0 ? "-" : " - ");
		if (d > 0) b.append(b.length() == 0 ? "" : " + ");
		if (suffix.isEmpty() || (d != 1 && d != -1)) b.append(Strings.compact(Math.abs(d)));
		b.append(suffix);
	}
}
