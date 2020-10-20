package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;

public class MoveToBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		MoveTo m = MoveTo.relative(Point2d.X_UNIT);
		MoveTo eq0 = MoveTo.relative(1, 0);
		MoveTo ne0 = MoveTo.absolute(Point2d.X_UNIT);
		MoveTo ne1 = MoveTo.relative(0, 0);
		MoveTo ne2 = MoveTo.relative(1, 1);
		exerciseEquals(m, eq0);
		assertAllNotEqual(m, ne0, ne1, ne2);
	}

	@Test
	public void shouldTranslate() {
		MoveTo m = MoveTo.absolute(1, 1);
		assertEquals(m.translate(Point2d.ZERO), m);
		assertEquals(m.translate(Point2d.Y_UNIT), MoveTo.absolute(1, 2));
		m = MoveTo.relative(1, 1);
		assertEquals(m.translate(Point2d.ZERO), m);
		assertEquals(m.translate(Point2d.Y_UNIT), m);
	}

}
