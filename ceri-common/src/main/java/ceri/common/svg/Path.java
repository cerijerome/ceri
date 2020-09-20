package ceri.common.svg;

import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;

public interface Path<T extends Path<T>> {

	String path();

	Position end();

	T translate(Point2d offset);

	T scale(Ratio2d scale);

	T reverse();

	T reflect(Line2d line);

}
