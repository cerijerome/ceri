package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EllipseBehavior {
	private final Ellipse e0 = new Ellipse(4, 2);
	private final Ellipse e1 = new Ellipse(1, 4);

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseEquals(e0, new Ellipse(4, 2));
		assertNotEquals(e0, new Ellipse(4.1, 2));
		assertNotEquals(e0, new Ellipse(4, 1.9));
	}

	@Test
	public void shouldOnlyAllowPositiveAxes() {
		assertException(() -> new Ellipse(0, 4));
		assertException(() -> new Ellipse(1, 0));
	}

	@Test
	public void shouldCalculatePerimeter() {
		assertApprox(new Ellipse(1, 1).perimeter(), 2 * Math.PI);
		assertApprox(new Ellipse(1000000, 1000000).perimeter(), 2 * Math.PI * 1000000);
		assertApprox(e0.perimeter(), 19.377);
		assertApprox(e1.perimeter(), 17.157);
	}
	
	@Test
	public void shouldCalculateXAndYCoordinates() {
		assertTrue(Double.isNaN(e0.yFromX(4.00001)));
		assertTrue(Double.isNaN(e0.yFromX(-4.00001)));
		assertTrue(Double.isNaN(e0.xFromY(2.00001)));
		assertTrue(Double.isNaN(e0.xFromY(-2.00001)));
		assertApprox(e0.yFromX(-2), 1.732);
		assertApprox(e0.yFromX(2), 1.732);
		assertApprox(e0.xFromY(1.732), 2);
		assertApprox(e0.xFromY(-2), 0);
		assertApprox(e0.yFromX(-4), 0);
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
	}

	@Test
	public void shouldCalculateArea() {
		assertApprox(e0.area(), 25.133);
		assertApprox(e1.area(), 12.566);
	}

}
