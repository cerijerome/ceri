package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

/**
 * A line to a position.
 */
public record LineTo(Position position) implements Path<LineTo> {

	/**
	 * Returns an absolute instance.
	 */
	public static LineTo absolute(Point2d p) {
		return absolute(p.x(), p.y());
	}

	/**
	 * Returns an absolute instance.
	 */
	public static LineTo absolute(double x, double y) {
		return new LineTo(Position.absolute(x, y));
	}

	/**
	 * Returns a relative instance.
	 */
	public static LineTo relative(Point2d p) {
		return relative(p.x(), p.y());
	}

	/**
	 * Returns a relative instance.
	 */
	public static LineTo relative(double x, double y) {
		return new LineTo(Position.relative(x, y));
	}

	@Override
	public LineTo reverse() {
		return create(position().reverse());
	}

	@Override
	public LineTo reflect(Line2d line) {
		return create(position().reflect(line));
	}

	@Override
	public LineTo scale(Ratio2d scale) {
		return create(position().scale(scale));
	}

	@Override
	public LineTo translate(Point2d offset) {
		return create(position().translate(offset));
	}

	@Override
	public Position end() {
		return position();
	}

	@Override
	public String d() {
		return String.format("%s%s,%s", position().absolute() ? "L" : "l",
			Svg.string(position().x()), Svg.string(position().y()));
	}

	private LineTo create(Position position) {
		if (position().equals(position)) return this;
		return new LineTo(position);
	}
}
