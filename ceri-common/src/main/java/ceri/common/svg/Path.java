package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public interface Path<T extends Path<T>> {

	/**
	 * Generates the path descriptor text attribute.
	 */
	String d();

	/**
	 * The end position of the path.
	 */
	Position end();

	/**
	 * Operation to translate the path.
	 */
	T translate(Point2d offset);

	/**
	 * Operation to scale the path.
	 */
	T scale(Ratio2d scale);

	/**
	 * Operation to reverse the path.
	 */
	T reverse();

	/**
	 * Operation to reflect the path in the line.
	 */
	T reflect(Line2d line);
}
