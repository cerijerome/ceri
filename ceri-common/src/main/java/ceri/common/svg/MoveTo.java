package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class MoveTo implements Path<MoveTo> {
	public final Position position;

	public static MoveTo absolute(Point2d p) {
		return absolute(p.x, p.y);
	}

	public static MoveTo absolute(double x, double y) {
		return create(Position.create(PositionType.absolute, x, y));
	}
	
	public static MoveTo relative(Point2d p) {
		return relative(p.x, p.y);
	}

	public static MoveTo relative(double x, double y) {
		return create(Position.create(PositionType.relative, x, y));
	}

	public static MoveTo create(Position position) {
		return new MoveTo(position);
	}

	private MoveTo(Position position) {
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
		return String.format("%s%f,%f", position.absolute() ? "M" : "m", position.x,
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