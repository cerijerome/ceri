package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class LineUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LineUtil.class);
	}

	@Test
	public void testDistanceOfZeroLengthLineToPoint() {
		Line2d l = Line2d.ZERO;
		assertThat(LineUtil.distance(l, 0, 0), is(0.0));
		assertThat(LineUtil.distance(l, 1, 0), is(1.0));
		assertThat(LineUtil.distance(l, 0, 1), is(1.0));
		assertThat(LineUtil.distance(l, -3, -4), is(5.0));
		l = Line2d.of(Point2d.X_UNIT, Point2d.X_UNIT);
		assertThat(LineUtil.distance(l, 0, 0), is(1.0));
		assertThat(LineUtil.distance(l, 1, 0), is(0.0));
		assertApprox(LineUtil.distance(l, 0, -1), 1.4142);
	}

	@Test
	public void testDistanceOfLineToPoint() {
		Line2d l = Line2d.of(Point2d.Y_UNIT, Point2d.ZERO);
		assertThat(LineUtil.distance(l, 0, 0), is(0.0));
		assertThat(LineUtil.distance(l, 1, 0), is(1.0));
		assertThat(LineUtil.distance(l, 0, 1), is(0.0));
		assertThat(LineUtil.distance(l, 0, 3), is(2.0));
		assertThat(LineUtil.distance(l, 0, -2), is(2.0));
		assertThat(LineUtil.distance(l, 3, -4), is(5.0));
		assertThat(LineUtil.distance(l, -4, 4), is(5.0));
	}

}
