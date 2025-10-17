package ceri.common.svg;

import java.util.Objects;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

/**
 * A 2d position, relative or absolute.
 */
public record Position(Type type, double x, double y) {
	public static final Position RELATIVE_ZERO = new Position(Type.relative, 0, 0);
	public static final Position ABSOLUTE_ZERO = new Position(Type.absolute, 0, 0);

	/**
	 * Position type.
	 */
	public enum Type {
		absolute,
		relative;
	}

	/**
	 * Returns a relative position.
	 */
	public static Position relative(Point2d position) {
		return relative(position.x(), position.y());
	}

	/**
	 * Returns a relative position.
	 */
	public static Position relative(double x, double y) {
		return of(Type.relative, x, y);
	}

	/**
	 * Returns an absolute position.
	 */
	public static Position absolute(Point2d position) {
		return absolute(position.x(), position.y());
	}

	/**
	 * Returns an absolute position.
	 */
	public static Position absolute(double x, double y) {
		return of(Type.absolute, x, y);
	}

	/**
	 * Returns a position of given type.
	 */
	public static Position of(Type type, Point2d position) {
		return of(type, position.x(), position.y());
	}

	/**
	 * Returns a position of given type.
	 */
	public static Position of(Type type, double x, double y) {
		Objects.requireNonNull(type);
		if (x == 0.0 && y == 0.0) return type == Type.absolute ? ABSOLUTE_ZERO : RELATIVE_ZERO;
		return new Position(type, x, y);
	}

	/**
	 * Returns the offset vector.
	 */
	public Point2d offset() {
		return Point2d.of(x(), y());
	}

	/**
	 * Returns true if absolute.
	 */
	public boolean absolute() {
		return type() == Type.absolute;
	}

	/**
	 * Combines with another position.
	 */
	public Position combine(Position position) {
		if (position.absolute()) return position;
		return create(x() + position.x(), y() + position.y());
	}

	/**
	 * Returns the position after reflecting this position in the line.
	 */
	public Position reflect(Line2d line) {
		var point = line.reflect(x(), y());
		return create(point.x(), point.y());
	}

	/**
	 * Returns this position reversed; only changes if relative.
	 */
	public Position reverse() {
		return absolute() ? this : create(-x(), -y());
	}

	/**
	 * Returns this position scaled by ratio.
	 */
	public Position scale(Ratio2d scale) {
		return scale(scale.x(), scale.y());
	}

	/**
	 * Returns this position scaled by ratio.
	 */
	public Position scale(double scaleX, double scaleY) {
		return create(x() * scaleX, y() * scaleY);
	}

	/**
	 * Returns this position moved by offset.
	 */
	public Position translate(Point2d offset) {
		return translate(offset.x(), offset.y());
	}

	/**
	 * Returns this position moved by offset.
	 */
	public Position translate(double offsetX, double offsetY) {
		return !absolute() ? this : create(x() + offsetX, y() + offsetY);
	}

	private Position create(double x, double y) {
		return create(type(), x, y);
	}

	private Position create(Type type, double x, double y) {
		if (type() == type && x() == x && y() == y) return this;
		return of(type, x, y);
	}
}
