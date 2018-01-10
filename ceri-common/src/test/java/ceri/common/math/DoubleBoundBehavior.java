package ceri.common.math;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DoubleBoundBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		DoubleBound b = DoubleBound.exclusive(-33.33);
		DoubleBound eq0 = DoubleBound.exclusive(-33.33);
		DoubleBound ne0 = DoubleBound.inclusive(-33.33);
		DoubleBound ne1 = DoubleBound.exclusive(-33.3);
		DoubleBound ne2 = DoubleBound.exclusive(33.33);
		DoubleBound ne3 = DoubleBound.UNBOUND;
		exerciseEquals(b, eq0);
		assertAllNotEqual(b, ne0, ne1, ne2, ne3);
		exerciseEnum(BoundType.class);
	}

	@Test
	public void shouldEncapsulateUnboundedness() {
		DoubleBound b = DoubleBound.UNBOUND;
		assertTrue(b.unbound());
		assertTrue(b.lowerFor(-33.33));
		assertTrue(b.lowerFor(33.33));
		assertTrue(b.lowerFor(Double.MAX_VALUE));
		assertTrue(b.lowerFor(-Double.MAX_VALUE));
		assertTrue(b.upperFor(-33.33));
		assertTrue(b.upperFor(33.33));
		assertTrue(b.upperFor(Double.MAX_VALUE));
		assertTrue(b.upperFor(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateInclusiveBounds() {
		DoubleBound b = DoubleBound.inclusive(-33.33);
		assertFalse(b.unbound());
		assertTrue(b.lowerFor(-33.3));
		assertTrue(b.lowerFor(-33.33));
		assertFalse(b.lowerFor(-33.34));
		assertTrue(b.lowerFor(33.33));
		assertFalse(b.upperFor(-33.3));
		assertTrue(b.upperFor(-33.33));
		assertTrue(b.upperFor(-33.34));
		assertFalse(b.upperFor(33.33));
	}

	@Test
	public void shouldEncapsulateExclusiveBounds() {
		DoubleBound b = DoubleBound.exclusive(-33.33);
		assertFalse(b.unbound());
		assertTrue(b.lowerFor(-33.3));
		assertFalse(b.lowerFor(-33.33));
		assertFalse(b.lowerFor(-33.34));
		assertTrue(b.lowerFor(33.33));
		assertFalse(b.upperFor(-33.3));
		assertFalse(b.upperFor(-33.33));
		assertTrue(b.upperFor(-33.34));
		assertFalse(b.upperFor(33.33));
	}

}
