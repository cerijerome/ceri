package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public record MoveTo(Position position) implements Path<MoveTo> {

	public static MoveTo absolute(Point2d p) {
		return absolute(p.x(), p.y());
	}

	public static MoveTo absolute(double x, double y) {
		return new MoveTo(new Position(Position.Type.absolute, x, y));
	}

	public static MoveTo relative(Point2d p) {
		return relative(p.x(), p.y());
	}

	public static MoveTo relative(double x, double y) {
		return new MoveTo(new Position(Position.Type.relative, x, y));
	}

	@Override
	public MoveTo reverse() {
		return new MoveTo(position().reverse());
	}

	@Override
	public MoveTo reflect(Line2d line) {
		return new MoveTo(position().reflect(line));
	}

	@Override
	public MoveTo scale(Ratio2d scale) {
		return new MoveTo(position().scale(scale));
	}

	@Override
	public MoveTo translate(Point2d offset) {
		var position = position().translate(offset);
		return position == position() ? this : new MoveTo(position);
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
}
