package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class LineTo implements Path<LineTo> {
	private final Position position;

	public static LineTo absolute(double x, double y) {
		return new LineTo(new Position(PositionType.absolute, x, y));
	}

	public static LineTo relative(double x, double y) {
		return new LineTo(new Position(PositionType.relative, x, y));
	}

	public LineTo(Position position) {
		this.position = position;
	}

	@Override
	public LineTo reverse() {
		return new LineTo(position.reverse());
	}
	
	@Override
	public LineTo reflect(Line2d line) {
		return new LineTo(position.reflect(line));
	}

	@Override
	public LineTo scale(Ratio2d scale) {
		return new LineTo(position.scale(scale));
	}

	@Override
	public LineTo translate(Point2d offset) {
		Position position = this.position.translate(offset);
		if (position == this.position) return this;
		return new LineTo(position);
	}

	@Override
	public Position end() {
		return position;
	}

	@Override
	public String path() {
		return String
			.format("%s%.0f,%.0f", position.absolute() ? "L" : "l", position.x, position.y);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LineTo)) return false;
		LineTo other = (LineTo) obj;
		if (!EqualsUtil.equals(position, other.position)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, position).toString();
	}

}
