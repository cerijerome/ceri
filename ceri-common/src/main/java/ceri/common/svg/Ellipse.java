package ceri.common.svg;

import java.util.Objects;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToString;

public class Ellipse implements Path<Ellipse> {
	public final Dimension2d radii;
	public final double rotation;
	private final Position center;

	public static class Builder {
		Dimension2d radii = null;
		double rotation = 0; // degrees
		Position center;

		Builder() {}

		Builder center(Position center) {
			this.center = center;
			return this;
		}

		Builder radii(Dimension2d radii) {
			this.radii = radii;
			return this;
		}

		public Builder rotation(double rotation) {
			this.rotation = rotation;
			return this;
		}

		public Ellipse build() {
			return new Ellipse(this);
		}
	}

	public static Builder builder(Ellipse arc) {
		return new Builder().center(arc.center).radii(arc.radii).rotation(arc.rotation);
	}

	public static Builder builder(Position center, Dimension2d radii) {
		return new Builder().center(center).radii(radii);
	}

	public static Ellipse circle(Position center, double radius) {
		return builder(center, Dimension2d.of(radius, radius)).build();
	}

	public static Ellipse of(Position center, Dimension2d radii) {
		return builder(center, radii).build();
	}

	Ellipse(Builder builder) {
		radii = builder.radii;
		rotation = builder.rotation;
		center = builder.center;
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public Ellipse rotate(double xRotation) {
		if (xRotation == 0) return this;
		return builder(this).rotation(xRotation).build();
	}

	@Override
	public Ellipse reverse() {
		return builder(this).center(center.reverse()).rotation(-rotation).build();
	}

	@Override
	public Ellipse reflect(Line2d line) {
		return builder(this).center(center.reflect(line)).build();
	}

	@Override
	public Ellipse scale(Ratio2d scale) {
		return builder(this).center(center.scale(scale)).radii(radii.resize(scale)).build();
	}

	@Override
	public Ellipse translate(Point2d offset) {
		return builder(this).center(center.translate(offset)).build();
	}

	@Override
	public Position end() {
		return center;
	}

	@Override
	public String path() {
		double radians = Math.toRadians(rotation);
		double x0 = radii.w * Math.cos(radians);
		double y0 = radii.w * Math.sin(radians);
		Position start = center.combine(Position.relative(-x0, -y0));
		Position offset = Position.relative(x0 * 2, y0 * 2);
		return PathGroup.of(MoveTo.position(start), //
			EllipticalArc.builder(offset, radii).build(), //
			EllipticalArc.builder(offset.reverse(), radii).build(), //
			MoveTo.relative(x0, y0)).path();
	}

	@Override
	public int hashCode() {
		return Objects.hash(radii, rotation, center);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ellipse)) return false;
		Ellipse other = (Ellipse) obj;
		if (!Objects.equals(radii, other.radii)) return false;
		if (!Objects.equals(rotation, other.rotation)) return false;
		if (!Objects.equals(center, other.center)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, center, radii, rotation);
	}

}
