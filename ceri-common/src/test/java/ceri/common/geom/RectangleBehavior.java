package ceri.common.geom;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.geom.Dimension2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Rectangle;

public class RectangleBehavior {

	@Test
	public void shouldNonBreachEqualsContract() {
		Rectangle r0 = new Rectangle(0, 0, 20, 40);
		Rectangle r1 = new Rectangle(new Point2d(0, 0), new Dimension2d(20, 40));
		Rectangle r2 = new Rectangle(1, 0, 20, 40);
		Rectangle r3 = new Rectangle(0, -1, 20, 40);
		Rectangle r4 = new Rectangle(0, 0, 19, 40);
		Rectangle r5 = new Rectangle(0, 0, 20, 41);
		exerciseEquals(r0, r1);
		assertNotEquals(r0, r2);
		assertNotEquals(r0, r3);
		assertNotEquals(r0, r4);
		assertNotEquals(r0, r5);
	}

	@Test
	public void shouldCalculateArea() {
		assertThat(new Rectangle(100, -200, 0, 0).area(), is(0.0));
		assertThat(new Rectangle(10, -20, 50, 20).area(), is(1000.0));
	}
	
	@Test
	public void shouldExposeDimensions() {
		Rectangle r = new Rectangle(100, -20, 50, 10);
		assertThat(r.position(), is(new Point2d(100, -20)));
		assertThat(r.size(), is(new Dimension2d(50, 10)));
		assertThat(r.corner(), is(new Point2d(150, -10)));
	}

}
