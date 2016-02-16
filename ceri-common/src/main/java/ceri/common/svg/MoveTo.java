package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class MoveTo implements Path<MoveTo> {
	private final Position position;

	public static MoveTo absolute(double x, double y) {
		return new MoveTo(new Position(PositionType.absolute, x, y));
	}
	
	public static MoveTo relative(double x, double y) {
		return new MoveTo(new Position(PositionType.relative, x, y));
	}
	
	public MoveTo(Position position) {
		this.position = position;
	}

	@Override
	public MoveTo reverse() {
		return new MoveTo(position.reverse());
	}
	
	@Override
	public MoveTo reflect(Line2d line) {
		return new MoveTo(position.reflect(line));
	}
	
	@Override
	public MoveTo scale(Ratio2d scale) {
		return new MoveTo(position.scale(scale));
	}
	
	@Override
	public MoveTo translate(Point2d offset) {
		Position position = this.position.translate(offset);
		if (position == this.position) return this;
		return new MoveTo(position);
	}
	
	@Override
	public Position end() {
		return position;
	}

	@Override
	public String path() {
		return String.format("%s%.0f,%.0f", position.absolute() ? "M" : "m", position.x,
			position.y);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MoveTo)) return false;
		MoveTo other = (MoveTo) obj;
		if (!EqualsUtil.equals(position, other.position)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, position).toString();
	}

}
