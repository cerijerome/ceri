package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class GroundedPath<T extends Path<T>> implements Path<GroundedPath<T>> {
	private final MoveTo move;
	public final T path;
	private final Position end;

	public static <T extends Path<T>> GroundedPath<T> of(Position start, T path) {
		return new GroundedPath<>(start, path);
	}

	private GroundedPath(Position start, T path) {
		move = MoveTo.position(start);
		this.path = path;
		end = SvgUtil.combinedEnd(move, path);
	}

	public GroundedPath<T> move(Position position) {
		return new GroundedPath<>(position.combine(start()), path);
	}

	@Override
	public GroundedPath<T> reverse() {
		return new GroundedPath<>(end(), path.reverse());
	}

	@Override
	public GroundedPath<T> reflect(Line2d line) {
		return new GroundedPath<>(start().reflect(line), path.reflect(line));
	}

	@Override
	public GroundedPath<T> scale(Ratio2d scale) {
		return new GroundedPath<>(start().scale(scale), path.scale(scale));
	}

	@Override
	public GroundedPath<T> translate(Point2d offset) {
		return new GroundedPath<>(start().translate(offset), path.translate(offset));
	}

	public Position start() {
		return move.end();
	}

	@Override
	public Position end() {
		return end;
	}

	@Override
	public String path() {
		return SvgUtil.combinedPath(move, path);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(move, path);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof GroundedPath)) return false;
		GroundedPath<?> other = (GroundedPath<?>) obj;
		if (!EqualsUtil.equals(move, other.move)) return false;
		if (!EqualsUtil.equals(path, other.path)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, start(), end, path).toString();
	}

}
