package ceri.common.geom;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class Rectangle2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Rectangle2d r = Rectangle2d.of(0, 0, 20, 40);
		Rectangle2d eq0 = Rectangle2d.of(Point2d.of(0, 0), Dimension2d.of(20, 40));
		Rectangle2d ne0 = Rectangle2d.of(1, 0, 20, 40);
		Rectangle2d ne1 = Rectangle2d.of(0, -1, 20, 40);
		Rectangle2d ne2 = Rectangle2d.of(0, 0, 19, 40);
		Rectangle2d ne3 = Rectangle2d.of(0, 0, 20, 41);
		Rectangle2d ne4 = Rectangle2d.of(0, 0, 0, 0);
		exerciseEquals(r, eq0);
		assertAllNotEqual(r, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldCalculateArea() {
		assertThat(Rectangle2d.of(100, -200, 0, 0).area(), is(0.0));
		assertThat(Rectangle2d.of(10, -20, 50, 20).area(), is(1000.0));
	}

	@Test
	public void shouldExposeDimensions() {
		Rectangle2d r = Rectangle2d.of(100, -20, 50, 10);
		assertThat(r.position(), is(Point2d.of(100, -20)));
		assertThat(r.size(), is(Dimension2d.of(50, 10)));
		assertThat(r.corner(), is(Point2d.of(150, -10)));
	}

}
