package ceri.common.svg;

import ceri.common.geom.Size2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public record Ellipse(Position center, Size2d radii, double rotation)
	implements Path<Ellipse> {

	public static Ellipse of(Position center, Size2d radii) {
		return new Ellipse(center, radii, 0.0);
	}

	public static Ellipse circle(Position center, double radius) {
		return new Ellipse(center, Size2d.of(radius, radius), 0.0);
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public Ellipse rotate(double xRotation) {
		return xRotation == 0 ? this : new Ellipse(center(), radii(), xRotation);
	}

	@Override
	public Ellipse reverse() {
		return new Ellipse(center().reverse(), radii(), -rotation());
	}

	@Override
	public Ellipse reflect(Line2d line) {
		return new Ellipse(center().reflect(line), radii(), rotation());
	}

	@Override
	public Ellipse scale(Ratio2d scale) {
		return new Ellipse(center().scale(scale), radii().resize(scale), rotation());
	}

	@Override
	public Ellipse translate(Point2d offset) {
		return new Ellipse(center().translate(offset), radii(), rotation());
	}

	@Override
	public Position end() {
		return center();
	}

	@Override
	public String d() {
		double radians = Math.toRadians(rotation());
		double x0 = radii().w() * Math.cos(radians);
		double y0 = radii().w() * Math.sin(radians);
		var start = center().combine(Position.relative(-x0, -y0));
		var offset = Position.relative(x0 * 2, y0 * 2);
		return PathGroup.of(new MoveTo(start), EllipticalArc.of(offset, radii()),
			EllipticalArc.of(offset.reverse(), radii()), MoveTo.relative(x0, y0)).d();
	}
}
