package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EllipseBehavior {
	private final Ellipse e0 = Ellipse.create(4, 2);
	private final Ellipse e1 = Ellipse.create(1, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(e0, Ellipse.create(4, 2));
		assertNotEquals(e0, Ellipse.create(4.1, 2));
		assertNotEquals(e0, Ellipse.create(4, 1.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> Ellipse.create(-0.1, 4));
		assertException(() -> Ellipse.create(1, -0.1));
	}

	@Test
	public void shouldDefineNull() {
		assertThat(Ellipse.create(0, 2), is(Ellipse.NULL));
		assertThat(Ellipse.create(1, 0), is(Ellipse.NULL));
	}

	@Test
	public void shouldCalculateAngle() {
		assertApprox(e0.angleFromX(-4), Math.PI / 2);
		assertApprox(e0.angleFromX(-2), 0.281);
		assertApprox(e0.angleFromX(0), 0);
		assertApprox(e0.angleFromX(2), -0.281);
		assertApprox(e0.angleFromX(4), -Math.PI / 2);
		assertApprox(e0.angleFromY(-2), 0);
		assertApprox(e0.angleFromY(-1), 0.714);
		assertApprox(e0.angleFromY(0), Math.PI / 2);
		assertApprox(e0.angleFromY(1), -0.714);
		assertApprox(e0.angleFromY(2), 0);
		assertApprox(Ellipse.NULL.angleFromX(1), 0);
		assertApprox(Ellipse.NULL.angleFromY(1), 0);
	}
	
	@Test
	public void shouldCalculateGradient() {
		assertThat(e0.gradientFromX(-4), is(Double.POSITIVE_INFINITY));
		assertApprox(e0.gradientFromX(-2), 0.289);
		assertApprox(e0.gradientFromX(0), 0);
		assertApprox(e0.gradientFromX(2), -0.289);
		assertThat(e0.gradientFromX(4), is(Double.NEGATIVE_INFINITY));
		assertApprox(e0.gradientFromY(-2), 0);
		assertApprox(e0.gradientFromY(-1), 0.866);
		assertThat(e0.gradientFromY(0), is(Double.POSITIVE_INFINITY));
		assertApprox(e0.gradientFromY(1), -0.866);
		assertApprox(e0.gradientFromY(2), 0);
		assertApprox(Ellipse.NULL.gradientFromX(1), 0);
		assertApprox(Ellipse.NULL.gradientFromY(1), 0);
	}

	@Test
	public void shouldCalculatePerimeter() {
		assertApprox(Ellipse.create(1, 1).perimeter(), 2 * Math.PI);
		assertApprox(Ellipse.create(1000000, 1000000).perimeter(), 2 * Math.PI * 1000000);
		assertApprox(e0.perimeter(), 19.377);
		assertApprox(e1.perimeter(), 17.157);
		assertApprox(Ellipse.NULL.perimeter(), 0);
	}

	@Test
	public void shouldCalculateXAndYCoordinates() {
		assertTrue(Double.isNaN(e0.yFromX(4.00001)));
		assertTrue(Double.isNaN(e0.yFromX(-4.00001)));
		assertTrue(Double.isNaN(e0.xFromY(2.00001)));
		assertTrue(Double.isNaN(e0.xFromY(-2.00001)));
		assertApprox(e0.yFromX(-4), 0);
		assertApprox(e0.yFromX(0), 2);
		assertApprox(e0.yFromX(4), 0);
		assertApprox(e0.yFromX(-2), 1.732);
		assertApprox(e0.yFromX(2), 1.732);
		assertApprox(e0.xFromY(-2), 0);
		assertApprox(e0.xFromY(0), 4);
		assertApprox(e0.xFromY(2), 0);
		assertApprox(e0.xFromY(1.732), 2);
		assertApprox(e0.xFromY(-2), 0);
		assertApprox(Ellipse.NULL.yFromX(1), 0);
		assertApprox(Ellipse.NULL.xFromY(1), 0);
	}

	@Test
	public void shouldCalculatePartialArea() {
		assertTrue(Double.isNaN(e0.areaBetweenX(-4.1, 0)));
		assertTrue(Double.isNaN(e0.areaBetweenY(0, 2.0000001)));
		assertTrue(Double.isNaN(e0.areaBetweenX(0, Double.NaN)));
		assertApprox(e0.areaBetweenX(-4, 4), 25.133);
		assertApprox(e0.areaBetweenX(-4, 0), 12.566);
		assertApprox(e0.areaBetweenX(0, 4), 12.566);
		assertApprox(e0.areaBetweenX(0, 2), 7.653);
		assertApprox(e0.areaBetweenX(2, 4), 4.913);
		assertApprox(e0.areaBetweenY(0, 2), 12.566);
		assertApprox(Ellipse.NULL.areaBetweenX(-1, 1), 0);
		assertApprox(Ellipse.NULL.areaBetweenY(-1, 1), 0);
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(e0.area(), 25.133);
		assertApprox(e1.area(), 12.566);
	}

}
