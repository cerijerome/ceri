package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Polar2dBehavior {
	private final Polar2d p0 = Polar2d.create(2, Math.PI / 3);
	private final Polar2d p1 = Polar2d.from(new Point2d(4, 3));

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(p0, Polar2d.create(2, Math.PI / 3));
		assertNotEquals(p0, Polar2d.create(2.1, Math.PI / 3));
		assertNotEquals(p0, Polar2d.create(2, Math.PI / 3.1));
	}

	@Test
	public void shouldOnlyAllowPositiveRadius() {
		assertException(() -> Cone3d.create(-0.1, 2));
		assertThat(Polar2d.from(Point2d.ZERO), is(Polar2d.ZERO));
	}

	@Test
	public void shouldConvertPoints() {
		assertApprox(p0.asPoint().x, 1);
		assertApprox(p0.asPoint().y, 1.732);
		assertThat(p1.r, is(5.0));
		assertThat(p1.asPoint(), is(new Point2d(4, 3)));

	}

}
