package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public record LineTo(Position position) implements Path<LineTo> {

	public static LineTo absolute(Point2d p) {
		return absolute(p.x(), p.y());
	}

	public static LineTo absolute(double x, double y) {
		return new LineTo(Position.absolute(x, y));
	}

	public static LineTo relative(Point2d p) {
		return relative(p.x(), p.y());
	}

	public static LineTo relative(double x, double y) {
		return new LineTo(Position.relative(x, y));
	}

	@Override
	public LineTo reverse() {
		return new LineTo(position().reverse());
	}

	@Override
	public LineTo reflect(Line2d line) {
		return new LineTo(position().reflect(line));
	}

	@Override
	public LineTo scale(Ratio2d scale) {
		return new LineTo(position().scale(scale));
	}

	@Override
	public LineTo translate(Point2d offset) {
		var position = position().translate(offset);
		return position == position() ? this : new LineTo(position);
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
}
