package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.geom.Point2d;
import ceri.common.test.TestUtil;

public class MoveToBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var m = MoveTo.relative(Point2d.X_UNIT);
		var eq0 = MoveTo.relative(1, 0);
		var ne0 = MoveTo.absolute(Point2d.X_UNIT);
		var ne1 = MoveTo.relative(0, 0);
		var ne2 = MoveTo.relative(1, 1);
		TestUtil.exerciseEquals(m, eq0);
		assertAllNotEqual(m, ne0, ne1, ne2);
	}

	@Test
	public void shouldTranslate() {
		var m = MoveTo.absolute(1, 1);
		assertEquals(m.translate(Point2d.ZERO), m);
		assertEquals(m.translate(Point2d.Y_UNIT), MoveTo.absolute(1, 2));
		m = MoveTo.relative(1, 1);
		assertEquals(m.translate(Point2d.ZERO), m);
		assertEquals(m.translate(Point2d.Y_UNIT), m);
	}
}
