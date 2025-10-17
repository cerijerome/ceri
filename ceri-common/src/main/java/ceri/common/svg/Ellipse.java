package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.geom.Size2d;
import ceri.common.util.Validate;

/**
 * A full elliptical path.
 */
public record Ellipse(Position end, Size2d radii, double rotation) implements Path<Ellipse> {
	/**
	 * Returns an instance with no rotation.
	 */
	public static Ellipse of(Position center, Size2d radii) {
		return new Ellipse(center, radii, 0.0);
	}

	/**
	 * Returns a circular instance.
	 */
	public static Ellipse circle(Position center, double radius) {
		return of(center, Size2d.of(radius, radius));
	}

	/**
	 * Constructor validation.
	 */
	public Ellipse {
		Validate.nonNull(end);
		Validate.nonNull(radii);
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public Ellipse rotate(double rotation) {
		return create(end(), radii(), rotation() + rotation);
	}

	@Override
	public Ellipse reverse() {
		return create(end().reverse(), radii(), -rotation());
	}

	@Override
	public Ellipse reflect(Line2d line) {
		return create(end().reflect(line), radii(), rotation());
	}

	@Override
	public Ellipse scale(Ratio2d scale) {
		return create(end().scale(scale), radii().resize(scale), rotation());
	}

	@Override
	public Ellipse translate(Point2d offset) {
		return create(end().translate(offset), radii(), rotation());
	}

	@Override
	public String d() {
		double radians = Math.toRadians(rotation());
		double x0 = radii().w() * Math.cos(radians);
		double y0 = radii().w() * Math.sin(radians);
		var start = end().combine(Position.relative(-x0, -y0));
		var offset = Position.relative(x0 * 2, y0 * 2);
		return PathGroup.of(new MoveTo(start), EllipticalArc.of(offset, radii()),
			EllipticalArc.of(offset.reverse(), radii()), MoveTo.relative(x0, y0)).d();
	}
	
	/**
	 * Returns true if the values equal this instance.
	 */
	public boolean equals(Position center, Size2d radii, double rotation) {
		return end().equals(center) && radii().equals(radii) && rotation() == rotation;
	}
	
	private Ellipse create(Position center, Size2d radii, double rotation) {
		if (equals(center, radii, rotation)) return this;
		return new Ellipse(center, radii, rotation);
	}
}
