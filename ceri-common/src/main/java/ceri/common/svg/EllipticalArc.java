package ceri.common.svg;

import java.util.Objects;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToString;

public class EllipticalArc implements Path<EllipticalArc> {
	public final Dimension2d radii;
	public final double rotation;
	public final LargeArcFlag size;
	public final SweepFlag sweep;
	private final Position end;

	public static class Builder {
		Dimension2d radii = null;
		double rotation = 0; // degrees
		LargeArcFlag size = LargeArcFlag.small;
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

		public Builder rotation(double rotation) {
			this.rotation = rotation;
			return this;
		}

		public Builder flag(LargeArcFlag large) {
			this.size = large;
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
		return new Builder().end(arc.end).radii(arc.radii).rotation(arc.rotation).flag(arc.size)
			.flag(arc.sweep);
	}

	public static Builder builder(Position end, Dimension2d radii) {
		return new Builder().end(end).radii(radii);
	}

	public static EllipticalArc circular(Position end, double radii) {
		return builder(end, Dimension2d.of(radii, radii)).build();
	}

	public static EllipticalArc of(Position end, Dimension2d radii) {
		return builder(end, radii).build();
	}

	EllipticalArc(Builder builder) {
		radii = builder.radii;
		rotation = builder.rotation;
		size = builder.size;
		sweep = builder.sweep;
		end = builder.end;
	}

	public EllipticalArc flag(LargeArcFlag large) {
		return builder(this).flag(large).build();
	}

	public EllipticalArc flag(SweepFlag sweep) {
		return builder(this).flag(sweep).build();
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public EllipticalArc rotate(double rotation) {
		if (rotation == 0) return this;
		return builder(this).rotation(rotation).build();
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
		return String.format("%s%s,%s %s %d,%d %s,%s", end.absolute() ? "A" : "a",
			SvgUtil.string(radii.w), SvgUtil.string(radii.h), SvgUtil.string(rotation), size.value,
			sweep.value, SvgUtil.string(end.x), SvgUtil.string(end.y));
	}

	@Override
	public int hashCode() {
		return Objects.hash(radii, rotation, size, sweep, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EllipticalArc other)) return false;
		if (!Objects.equals(radii, other.radii)) return false;
		if (!Objects.equals(rotation, other.rotation)) return false;
		if (!Objects.equals(size, other.size)) return false;
		if (!Objects.equals(sweep, other.sweep)) return false;
		if (!Objects.equals(end, other.end)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, end, radii, rotation, size, sweep);
	}
}
