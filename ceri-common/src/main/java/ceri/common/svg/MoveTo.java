package ceri.common.svg;

import static ceri.common.svg.SvgUtil.string;
import java.util.Objects;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToString;

public class MoveTo implements Path<MoveTo> {
	public final Position position;

	public static MoveTo absolute(Point2d p) {
		return absolute(p.x, p.y);
	}

	public static MoveTo absolute(double x, double y) {
		return position(Position.of(PositionType.absolute, x, y));
	}

	public static MoveTo relative(Point2d p) {
		return relative(p.x, p.y);
	}

	public static MoveTo relative(double x, double y) {
		return position(Position.of(PositionType.relative, x, y));
	}

	public static MoveTo position(Position position) {
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
		return String.format("%s%s,%s", position.absolute() ? "M" : "m", string(position.x),
			string(position.y));
	}

	@Override
	public int hashCode() {
		return Objects.hash(position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MoveTo)) return false;
		MoveTo other = (MoveTo) obj;
		if (!Objects.equals(position, other.position)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, position);
	}

}
