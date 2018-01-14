package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Position {
	public static final Position RELATIVE_ZERO = new Position(PositionType.relative, 0, 0);
	public static final Position ABSOLUTE_ZERO = new Position(PositionType.absolute, 0, 0);
	public final PositionType type;
	public final double x;
	public final double y;

	public static Position relative(Point2d position) {
		return relative(position.x, position.y);
	}

	public static Position relative(double x, double y) {
		return of(PositionType.relative, x, y);
	}

	public static Position absolute(Point2d position) {
		return absolute(position.x, position.y);
	}

	public static Position absolute(double x, double y) {
		return of(PositionType.absolute, x, y);
	}

	public static Position of(PositionType type, Point2d position) {
		return of(type, position.x, position.y);
	}

	public static Position of(PositionType type, double x, double y) {
		return new Position(type, x, y);
	}

	private Position(PositionType type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public Point2d vector() {
		return Point2d.of(x, y);
	}

	public boolean absolute() {
		return type == PositionType.absolute;
	}

	public Position combine(Position position) {
		if (position == null) return this;
		if (position.absolute()) return position;
		return new Position(type, x + position.x, y + position.y);
	}

	public Position reflect(Line2d line) {
		return Position.of(type, line.reflect(Point2d.of(x, y)));
	}

	public Position reverse() {
		if (absolute()) return this;
		return new Position(type, -x, -y);
	}

	public Position scale(double scaleX, double scaleY) {
		return new Position(type, x * scaleX, y * scaleY);
	}

	public Position scale(Ratio2d scale) {
		return scale(scale.x, scale.y);
	}

	public Position translate(Point2d offset) {
		return translate(offset.x, offset.y);
	}

	public Position translate(double offsetX, double offsetY) {
		if (!absolute()) return this;
		return new Position(type, x + offsetX, y + offsetY);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Position)) return false;
		Position other = (Position) obj;
		if (!EqualsUtil.equals(type, other.type)) return false;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(y, other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, type, x, y).toString();
	}

}
