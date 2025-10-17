package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.geom.Size2d;
import ceri.common.util.Validate;

/**
 * An elliptical arc curve.
 */
public record EllipticalArc(Position end, Size2d radii, double rotation, Svg.LargeArcFlag size,
	Svg.SweepFlag sweep) implements Path<EllipticalArc> {

	/**
	 * Returns a circular instance.
	 */
	public static EllipticalArc circular(Position end, double radii) {
		return of(end, Size2d.of(radii, radii));
	}

	/**
	 * Returns an instance with small arc, positive sweep, and no rotation.
	 */
	public static EllipticalArc of(Position end, Size2d radii) {
		return new EllipticalArc(end, radii, 0.0, Svg.LargeArcFlag.small, Svg.SweepFlag.positive);
	}

	/**
	 * Constructor validation.
	 */
	public EllipticalArc {
		Validate.nonNull(end);
		Validate.nonNull(radii);
		Validate.nonNull(size);
		Validate.nonNull(sweep);
	}

	/**
	 * Returns a new instance
	 */
	public EllipticalArc flag(Svg.LargeArcFlag size) {
		return create(end(), radii(), rotation(), size, sweep());
	}

	public EllipticalArc flag(Svg.SweepFlag sweep) {
		return create(end(), radii(), rotation(), size(), sweep);
	}

	/**
	 * Rotate x-axis in degrees.
	 */
	public EllipticalArc rotate(double rotation) {
		return create(end(), radii(), rotation() + rotation, size(), sweep());
	}

	@Override
	public EllipticalArc reverse() {
		return create(end().reverse(), radii(), rotation(), size(), sweep().reverse());
	}

	@Override
	public EllipticalArc reflect(Line2d line) {
		return create(end().reflect(line), radii(), rotation(), size(), sweep().reverse());
	}

	@Override
	public EllipticalArc scale(Ratio2d scale) {
		return create(end().scale(scale), radii().resize(scale), rotation(), size(), sweep());
	}

	@Override
	public EllipticalArc translate(Point2d offset) {
		return create(end().translate(offset), radii(), rotation(), size(), sweep());
	}

	@Override
	public String d() {
		return String.format("%s%s,%s %s %d,%d %s,%s", end().absolute() ? "A" : "a",
			Svg.string(radii().w()), Svg.string(radii().h()), Svg.string(rotation()), size().value,
			sweep().value, Svg.string(end().x()), Svg.string(end().y()));
	}

	/**
	 * Returns true if the values equal this instance.
	 */
	public boolean equals(Position end, Size2d radii, double rotation, Svg.LargeArcFlag size,
		Svg.SweepFlag sweep) {
		return end().equals(end) && radii().equals(radii) && rotation() == rotation
			&& size() == size && sweep() == sweep;
	}

	private EllipticalArc create(Position end, Size2d radii, double rotation, Svg.LargeArcFlag size,
		Svg.SweepFlag sweep) {
		if (equals(end, radii, rotation, size, sweep)) return this;
		return new EllipticalArc(end, radii, rotation, size, sweep);
	}
}
