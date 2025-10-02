package ceri.common.svg;

import ceri.common.geom.Size2d;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public record EllipticalArc(Position end, Size2d radii, double rotation, Svg.LargeArcFlag size,
	Svg.SweepFlag sweep) implements Path<EllipticalArc> {

	public static EllipticalArc circular(Position end, double radii) {
		return of(end, Size2d.of(radii, radii));
	}

	public static EllipticalArc of(Position end, Size2d radii) {
		return new EllipticalArc(end, radii, 0.0, Svg.LargeArcFlag.small, Svg.SweepFlag.positive);
	}

	public EllipticalArc flag(Svg.LargeArcFlag size) {
		return new EllipticalArc(end(), radii(), rotation(), size, sweep());
	}

	public EllipticalArc flag(Svg.SweepFlag sweep) {
		return new EllipticalArc(end(), radii(), rotation(), size(), sweep);
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public EllipticalArc rotate(double rotation) {
		return rotation == 0.0 ? this :
			new EllipticalArc(end(), radii(), rotation, size(), sweep());
	}

	@Override
	public EllipticalArc reverse() {
		return new EllipticalArc(end().reverse(), radii(), rotation(), size(), sweep().reverse());
	}

	@Override
	public EllipticalArc reflect(Line2d line) {
		return new EllipticalArc(end().reflect(line), radii(), rotation(), size(),
			sweep().reverse());
	}

	@Override
	public EllipticalArc scale(Ratio2d scale) {
		return new EllipticalArc(end().scale(scale), radii().resize(scale), rotation(), size(),
			sweep());
	}

	@Override
	public EllipticalArc translate(Point2d offset) {
		return new EllipticalArc(end().translate(offset), radii(), rotation(), size(), sweep());
	}

	@Override
	public String d() {
		return String.format("%s%s,%s %s %d,%d %s,%s", end().absolute() ? "A" : "a",
			Svg.string(radii().w()), Svg.string(radii().h()), Svg.string(rotation()), size().value,
			sweep().value, Svg.string(end().x()), Svg.string(end().y()));
	}
}
