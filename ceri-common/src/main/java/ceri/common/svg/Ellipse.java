package ceri.common.svg;

import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Ellipse implements Path<Ellipse> {
	public final Dimension2d radii;
	public final double xRotation;
	private final Position center;

	public static class Builder {
		Dimension2d radii = null;
		double xRotation = 0;
		Position center;

		Builder() {}

		Builder center(Position center) {
			this.center = center;
			return this;
		}

		Builder radius(double radius) {
			this.radii = new Dimension2d(radius, radius);
			return this;
		}

		Builder radii(Dimension2d radii) {
			this.radii = radii;
			return this;
		}

		public Builder xRotation(double xRotation) {
			this.xRotation = xRotation;
			return this;
		}

		public Ellipse build() {
			return new Ellipse(this);
		}
	}

	public static Builder builder(Ellipse arc) {
		return new Builder().center(arc.center).radii(arc.radii).xRotation(arc.xRotation);
	}

	public static Ellipse circle(Position center, double radius) {
		return builder(center, new Dimension2d(radius, radius)).build();
	}

	public static Builder builder(Position center, Dimension2d radii) {
		return new Builder().center(center).radii(radii);
	}

	public static Ellipse absoluteCircle(double x, double y, double r) {
		return absolute(x, y, r, r).build();
	}

	public static Builder absolute(double x, double y, double rx, double ry) {
		return builder(Position.absolute(x, y), new Dimension2d(rx, ry));
	}

	public static Ellipse relativeCircle(double x, double y, double r) {
		return relative(x, y, r, r).build();
	}

	public static Builder relative(double x, double y, double rx, double ry) {
		return builder(Position.relative(x, y), new Dimension2d(rx, ry));
	}

	Ellipse(Builder builder) {
		radii = builder.radii;
		xRotation = builder.xRotation;
		center = builder.center;
	}

	@Override
	public Ellipse reverse() {
		return builder(this).center(center.reverse()).build();
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
		double radians = Math.toRadians(xRotation);
		double x0 = radii.w * Math.cos(radians);
		double y0 = radii.w * Math.sin(radians);
		Position offset = Position.relative(x0 * 2, y0 * 2);
		return PathGroup.of(MoveTo.create(center).translate(new Point2d(-x0, -y0)), //
			EllipticalArc.builder(offset, radii).build(), //
			EllipticalArc.builder(offset.reverse(), radii).build(), //
			MoveTo.relative(x0, y0)).path();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(radii, xRotation, center);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ellipse)) return false;
		Ellipse other = (Ellipse) obj;
		if (!EqualsUtil.equals(radii, other.radii)) return false;
		if (!EqualsUtil.equals(xRotation, other.xRotation)) return false;
		if (!EqualsUtil.equals(center, other.center)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, center, radii, xRotation).toString();
	}

}
