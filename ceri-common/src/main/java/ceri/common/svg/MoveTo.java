package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

/**
 * A move to a position.
 */
public record MoveTo(Position position) implements Path<MoveTo> {

	/**
	 * Returns an absolute instance.
	 */
	public static MoveTo absolute(Point2d p) {
		return absolute(p.x(), p.y());
	}

	/**
	 * Returns an absolute instance.
	 */
	public static MoveTo absolute(double x, double y) {
		return new MoveTo(new Position(Position.Type.absolute, x, y));
	}

	/**
	 * Returns a relative instance.
	 */
	public static MoveTo relative(Point2d p) {
		return relative(p.x(), p.y());
	}

	/**
	 * Returns a relative instance.
	 */
	public static MoveTo relative(double x, double y) {
		return new MoveTo(new Position(Position.Type.relative, x, y));
	}

	@Override
	public MoveTo reverse() {
		return create(position().reverse());
	}

	@Override
	public MoveTo reflect(Line2d line) {
		return create(position().reflect(line));
	}

	@Override
	public MoveTo scale(Ratio2d scale) {
		return create(position().scale(scale));
	}

	@Override
	public MoveTo translate(Point2d offset) {
		return create(position().translate(offset));
	}

	@Override
	public Position end() {
		return position();
	}

	@Override
	public String d() {
		return String.format("%s%s,%s", position().absolute() ? "M" : "m",
			Svg.string(position().x()), Svg.string(position().y()));
	}

	private MoveTo create(Position position) {
		if (position().equals(position)) return this;
		return new MoveTo(position);
	}
}
