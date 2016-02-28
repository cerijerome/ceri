package ceri.common.svg;

import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class EllipticalArc implements Path<EllipticalArc> {
	public final Dimension2d radii;
	public final double xRotation;
	public final LargeArcFlag large;
	public final SweepFlag sweep;
	private final Position end;

	public static class Builder {
		Dimension2d radii = null;
		double xRotation = 0;
		LargeArcFlag large = LargeArcFlag.small;
		SweepFlag sweep = SweepFlag.positive;
		Position end;

		Builder() {}

		Builder end(Position end) {
			this.end = end;
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

		public Builder flag(LargeArcFlag large) {
			this.large = large;
			return this;
		}

		public Builder flag(SweepFlag sweep) {
			this.sweep = sweep;
			return this;
		}

		public EllipticalArc build() {
			return new EllipticalArc(this);
		}
	}

	public static Builder builder(EllipticalArc arc) {
		return new Builder().end(arc.end).radii(arc.radii).xRotation(arc.xRotation).flag(arc.large)
			.flag(arc.sweep);
	}

	public static Builder builder(Position end, Dimension2d radii) {
		return new Builder().end(end).radii(radii);
	}

	public static Builder absolute(double endX, double endY, double rx, double ry) {
		return builder(Position.absolute(endX, endY), new Dimension2d(rx, ry));
	}

	public static Builder relative(double endX, double endY, double rx, double ry) {
		return builder(Position.relative(endX, endY), new Dimension2d(rx, ry));
	}

	EllipticalArc(Builder builder) {
		radii = builder.radii;
		xRotation = builder.xRotation;
		large = builder.large;
		sweep = builder.sweep;
		end = builder.end;
	}

	@Override
	public EllipticalArc reverse() {
		return builder(this).end(end.reverse()).flag(sweep.reverse()).build();
	}

	@Override
	public EllipticalArc reflect(Line2d line) {
		return builder(this).end(end.reflect(line)).flag(sweep.reverse()).build();
	}

	@Override
	public EllipticalArc scale(Ratio2d scale) {
		return builder(this).end(end.scale(scale)).radii(radii.resize(scale)).build();
	}

	@Override
	public EllipticalArc translate(Point2d offset) {
		return builder(this).end(end.translate(offset)).build();
	}

	@Override
	public Position end() {
		return end;
	}

	@Override
	public String path() {
		return String.format("%s%.0f,%.0f %.0f %d,%d %.0f,%.0f", end.absolute() ? "A" : "a",
			radii.w, radii.h, xRotation, large.value, sweep.value, end.x, end.y);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(radii, xRotation, large, sweep, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EllipticalArc)) return false;
		EllipticalArc other = (EllipticalArc) obj;
		if (!EqualsUtil.equals(radii, other.radii)) return false;
		if (!EqualsUtil.equals(xRotation, other.xRotation)) return false;
		if (!EqualsUtil.equals(large, other.large)) return false;
		if (!EqualsUtil.equals(sweep, other.sweep)) return false;
		if (!EqualsUtil.equals(end, other.end)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, end, radii, xRotation, large, sweep).toString();
	}

}
