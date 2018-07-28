package ceri.common.math;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.math.Bound.Type;

public class BoundBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Bound<Double> b = Bound.exclusive(-33.33);
		Bound<Double> eq0 = Bound.exclusive(-33.33);
		Bound<Double> ne0 = Bound.inclusive(-33.33);
		Bound<Double> ne1 = Bound.exclusive(-33.3);
		Bound<Double> ne2 = Bound.exclusive(33.33);
		Bound<Double> ne3 = Bound.unbound();
		exerciseEquals(b, eq0);
		assertAllNotEqual(b, ne0, ne1, ne2, ne3);
		exerciseEnum(Bound.Type.class);
	}

	@Test
	public void shouldBeUnboundForNullValue() {
		assertThat(Bound.of((String) null, Type.exclusive), is(Bound.unbound()));
	}

	@Test
	public void shouldEncapsulateUnboundedness() {
		Bound<Double> b = Bound.unbound();
		assertTrue(b.isUnbound());
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
		Bound<Double> b = Bound.inclusive(-33.33);
		assertFalse(b.isUnbound());
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
		Bound<Double> b = Bound.exclusive(-33.33);
		assertFalse(b.isUnbound());
		assertTrue(b.lowerFor(-33.3));
		assertFalse(b.lowerFor(-33.33));
		assertFalse(b.lowerFor(-33.34));
		assertTrue(b.lowerFor(33.33));
		assertFalse(b.upperFor(-33.3));
		assertFalse(b.upperFor(-33.33));
		assertTrue(b.upperFor(-33.34));
		assertFalse(b.upperFor(33.33));
	}

	@Test
	public void shouldNotBeABoundNull() {
		assertFalse(Bound.unbound().lowerFor(null));
		assertFalse(Bound.unbound().upperFor(null));
		assertFalse(Bound.exclusive(1).lowerFor(null));
		assertFalse(Bound.exclusive(1).upperFor(null));
	}

}
