package ceri.common.svg;

import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class LineToBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var m = LineTo.relative(Point2d.X_UNIT);
		var eq0 = LineTo.relative(1, 0);
		var ne0 = LineTo.absolute(Point2d.X_UNIT);
		var ne1 = LineTo.relative(0, 0);
		var ne2 = LineTo.relative(1, 1);
		Testing.exerciseEquals(m, eq0);
		Assert.notEqualAll(m, ne0, ne1, ne2);
	}
}
