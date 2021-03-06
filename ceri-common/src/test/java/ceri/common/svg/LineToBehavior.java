package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;

public class LineToBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		LineTo m = LineTo.relative(Point2d.X_UNIT);
		LineTo eq0 = LineTo.relative(1, 0);
		LineTo ne0 = LineTo.absolute(Point2d.X_UNIT);
		LineTo ne1 = LineTo.relative(0, 0);
		LineTo ne2 = LineTo.relative(1, 1);
		exerciseEquals(m, eq0);
		assertAllNotEqual(m, ne0, ne1, ne2);
	}

}
