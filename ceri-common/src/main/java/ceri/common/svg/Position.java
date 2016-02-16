package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Position {
	public static final Position ZERO = new Position(PositionType.relative, 0, 0);
	public final PositionType type;
	public final double x;
	public final double y;

	public Position(PositionType type, Point2d position) {
		this(type, position.x, position.y);
	}

	public Position(PositionType type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;
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
		return new Position(type, line.reflect(new Point2d(x, y)));
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
