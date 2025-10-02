package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public record GroundedPath<T extends Path<T>>(MoveTo move, T path, Position end)
	implements Path<GroundedPath<T>> {

	public static <T extends Path<T>> GroundedPath<T> of(Position start, T path) {
		return of(new MoveTo(start), path);
	}

	public static <T extends Path<T>> GroundedPath<T> of(MoveTo move, T path) {
		return new GroundedPath<>(move, path, Svg.combinedEnd(move, path));
	}

	public GroundedPath<T> move(Position position) {
		return of(position.combine(start()), path());
	}

	@Override
	public GroundedPath<T> reverse() {
		return of(end(), path().reverse());
	}

	@Override
	public GroundedPath<T> reflect(Line2d line) {
		return of(start().reflect(line), path().reflect(line));
	}

	@Override
	public GroundedPath<T> scale(Ratio2d scale) {
		return of(start().scale(scale), path().scale(scale));
	}

	@Override
	public GroundedPath<T> translate(Point2d offset) {
		return of(start().translate(offset), path().translate(offset));
	}

	public Position start() {
		return move().end();
	}

	@Override
	public String d() {
		return Svg.combinedPath(move(), path());
	}
}
