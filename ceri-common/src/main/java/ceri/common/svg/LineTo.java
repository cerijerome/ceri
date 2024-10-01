package ceri.common.svg;

import static ceri.common.svg.SvgUtil.string;
import java.util.Objects;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToString;

public class LineTo implements Path<LineTo> {
	public final Position position;

	public static LineTo absolute(Point2d p) {
		return absolute(p.x, p.y);
	}

	public static LineTo absolute(double x, double y) {
		return create(Position.absolute(x, y));
	}

	public static LineTo relative(Point2d p) {
		return relative(p.x, p.y);
	}

	public static LineTo relative(double x, double y) {
		return create(Position.relative(x, y));
	}

	public static LineTo create(Position position) {
		return new LineTo(position);
	}

	private LineTo(Position position) {
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
		return String.format("%s%s,%s", position.absolute() ? "L" : "l", string(position.x),
			string(position.y));
	}

	@Override
	public int hashCode() {
		return Objects.hash(position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof LineTo other) && Objects.equals(position, other.position);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, position);
	}

}
