package ceri.common.geom;

import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.text.Strings;
import ceri.common.util.Validate;

/**
 * Represents a 2d line from a start point to an end point.
 */
public record Line2d(Point2d from, Point2d to) {
	public static final Line2d ZERO = new Line2d(Point2d.ZERO, Point2d.ZERO);
	public static final Line2d X_UNIT = new Line2d(Point2d.ZERO, Point2d.X_UNIT);
	public static final Line2d Y_UNIT = new Line2d(Point2d.ZERO, Point2d.Y_UNIT);

	/**
	 * Represents a line formula ax + by + c = 0.
	 */
	public record Equation(double a, double b, double c) {

		public static final Equation NULL = new Equation(0, 0, 0);
		public static final Equation X_AXIS = Line2d.X_UNIT.equation();
		public static final Equation Y_AXIS = Line2d.Y_UNIT.equation();

		/**
		 * Returns a validated instance from two points on the line.
		 */
		public static Equation between(Point2d from, Point2d to) {
			return between(from.x(), from.y(), to.x(), to.y());
		}

		/**
		 * Returns a validated instance from two points on the line.
		 */
		public static Equation between(double fromX, double fromY, double toX, double toY) {
			double dx = toX - fromX + 0.0;
			double dy = toY - fromY + 0.0;
			if (dx == 0.0 && dy == 0.0) return NULL;
			if (dx == 0.0) return of(1.0, 0.0, -fromX);
			if (dy == 0.0) return of(0.0, 1.0, -fromY);
			double b = -dx / dy;
			double c = -fromX - (b * fromY);
			return of(1.0, b, c);
		}

		/**
		 * Returns a validated instance.
		 */
		public static Equation of(double a, double b, double c) {
			Validate.finite(a);
			Validate.finite(b);
			Validate.finite(c);
			if (a == 0.0 && b == 0.0) return NULL;
			return new Equation(a + 0.0, b + 0.0, c + 0.0);
		}

		/**
		 * Returns true if all coefficients are 0.
		 */
		public boolean isNull() {
			return isConst() && c() == 0.0;
		}

		/**
		 * Returns true if the line has no x or y coefficients.
		 */
		public boolean isConst() {
			return a() == 0.0 && b() == 0.0;
		}

		/**
		 * Reflects the given point in the line.
		 */
		public Point2d reflect(Point2d point) {
			if (isNull()) return point;
			return reflect(point.x(), point.y());
		}

		/**
		 * Reflects the given point in the line.
		 */
		public Point2d reflect(double x, double y) {
			if (isNull()) return Point2d.of(x, y);
			double aa = a() * a();
			double bb = b() * b();
			double newX = ((x * (bb - aa)) - (y * a() * b() * 2) - (a() * c() * 2)) / (aa + bb);
			double newY = ((y * (aa - bb)) - (x * a() * b() * 2) - (b() * c() * 2)) / (aa + bb);
			return Point2d.of(newX, newY);
		}

		/**
		 * Returns the angle of the gradient.
		 */
		public double angle() {
			if (isNull()) return Double.NaN;
			if (b() <= 0.0) return Math.atan2(a(), -b());
			return Math.atan2(-a(), b());
		}

		/**
		 * Returns the gradient of the line.
		 */
		public double gradient() {
			if (isNull()) return Double.NaN;
			if (a() == 0.0) return 0.0;
			if (b() == 0.0) return Double.POSITIVE_INFINITY;
			return -a() / b();
		}

		/**
		 * Normalizes the equation to a = 1, or b = 1 if a = 0.
		 */
		public Equation normalize() {
			if (isNull()) return this;
			if (a() != 0.0) return new Equation(1, 0.0 + b() / a(), 0.0 + c() / a()); // avoid -0.0
			return new Equation(0.0, 1.0, 0.0 + c() / b()); // avoid -0.0
		}

		/**
		 * Calculates the distance from this line to the point.
		 */
		public double distanceTo(Point2d point) {
			return distanceTo(point.x(), point.y());
		}

		/**
		 * Calculates the distance from this line to the point.
		 */
		public double distanceTo(double x, double y) {
			if (isNull()) return Double.NaN;
			return Math.abs((a() * x) + (b() * y) + c()) / Math.sqrt((a() * a()) + (b() * b()));
		}

		@Override
		public String toString() {
			if (isNull()) return "0 = 0";
			var s = new StringBuilder();
			addTerm(s, a(), "x");
			addTerm(s, b(), "y");
			addTerm(s, c(), "");
			return s.append(" = 0").toString();
		}

		private void addTerm(StringBuilder b, double d, String suffix) {
			if (d == 0.0) return;
			if (d < 0.0) b.append(b.length() == 0 ? "-" : " - ");
			if (d > 0.0) b.append(b.length() == 0 ? "" : " + ");
			if (suffix.isEmpty() || (d != 1.0 && d != -1.0)) b.append(Strings.compact(Math.abs(d)));
			b.append(suffix);
		}
	}

	/**
	 * Returns a validated instance from the origin to the point.
	 */
	public static Line2d of(double x, double y) {
		return of(Point2d.of(x, y));
	}

	/**
	 * Returns a validated instance from the origin to the point.
	 */
	public static Line2d of(Point2d to) {
		return of(Point2d.ZERO, to);
	}

	/**
	 * Returns a validated instance from one point to another.
	 */
	public static Line2d of(double fromX, double fromY, double toX, double toY) {
		return of(Point2d.of(fromX, fromY), Point2d.of(toX, toY));
	}

	/**
	 * Returns a validated instance from one point to another.
	 */
	public static Line2d of(Point2d from, Point2d to) {
		if (ZERO.equals(from, to)) return ZERO;
		if (X_UNIT.equals(from, to)) return X_UNIT;
		if (Y_UNIT.equals(from, to)) return Y_UNIT;
		return new Line2d(from, to);
	}

	/**
	 * Returns true if the line starts and ends at the origin.
	 */
	public boolean isZero() {
		return equals(ZERO);
	}

	/**
	 * Returns the vector between points.
	 */
	public Point2d vector() {
		return Point2d.of(vectorX(), vectorY());
	}

	/**
	 * Reflects the given point in the extended line.
	 */
	public Point2d reflect(Point2d point) {
		return equation().reflect(point);
	}

	/**
	 * Reflects the given point in the extended line.
	 */
	public Point2d reflect(double x, double y) {
		return equation().reflect(x, y);
	}

	/**
	 * Translates the line by the given offset.
	 */
	public Line2d translate(Point2d offset) {
		return translate(offset.x(), offset.y());
	}

	/**
	 * Translates the line by the given offset.
	 */
	public Line2d translate(double x, double y) {
		return create(from().translate(x, y), to().translate(x, y));
	}

	/**
	 * Scales the line by given ratios.
	 */
	public Line2d scale(Ratio2d scale) {
		return scale(scale.x(), scale.y());
	}

	/**
	 * Scales the line by given ratios.
	 */
	public Line2d scale(double x, double y) {
		return create(from().scale(x, y), to().scale(x, y));
	}

	/**
	 * Calculates the angle of the gradient relative to the x-axis.
	 */
	public double angle() {
		return vectorCalc((x, y) -> Math.atan2(y, x));
	}

	/**
	 * Calculates the gradient of the line.
	 */
	public double gradient() {
		return vectorCalc((x, y) -> y / x);
	}

	/**
	 * Calculates the length of the line.
	 */
	public double length() {
		return vectorCalc(Math::hypot);
	}

	/**
	 * Calculates the squared length of the line.
	 */
	public double quadrance() {
		return vectorCalc((x, y) -> (x * x) + (y * y));
	}

	/**
	 * Calculates the shortest distance from this line segment to the point.
	 */
	public double distanceTo(Point2d point) {
		return distanceTo(point.x(), point.y());
	}

	/**
	 * Calculates the shortest distance from this line segment to the point.
	 */
	public double distanceTo(double x, double y) {
		// a = start of line, b = end of line, p = point, c = closest point on line to p
		// a--c---b
		// |
		// p
		var vector = vector();
		if (vector.isZero()) return from().to(x, y).distance();
		var ab = Geometry.vector(vector());
		var ap = Geometry.vector(from().to(x, y));
		double t = Maths.limit(ab.dot(ap) / ab.dot(ab), 0, 1);
		var c = Geometry.vector(from()).add(ab.multiply(t));
		return Geometry.point(c).to(x, y).distance();
	}

	/**
	 * Converts the line into an equation.
	 */
	public Equation equation() {
		return Equation.between(from(), to());
	}

	/**
	 * Returns true if this line is between the given start and end points.
	 */
	public boolean equals(Point2d from, Point2d to) {
		return from().equals(from) && to().equals(to);
	}

	/**
	 * Returns true if this line is between the given start and end points.
	 */
	public boolean equals(double fromX, double fromY, double toX, double toY) {
		return from().equals(fromX, fromY) && to().equals(toX, toY);
	}

	@Override
	public String toString() {
		return from() + "-" + to();
	}

	private Line2d create(Point2d from, Point2d to) {
		if (equals(from, to)) return this;
		return of(from, to);
	}

	private double vectorCalc(Functions.DoubleBiOperator calc) {
		double vectorX = vectorX();
		double vectorY = vectorY();
		if (Point2d.ZERO.equals(vectorX, vectorY)) return Double.NaN;
		return calc.applyAsDouble(vectorX, vectorY);
	}

	private double vectorX() {
		return to().x() - from().x();
	}

	private double vectorY() {
		return to().y() - from().y();
	}
}
