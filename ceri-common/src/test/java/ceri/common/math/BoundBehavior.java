package ceri.common.math;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEnum;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.comparator.Comparators;
import ceri.common.math.Bound.Type;
import ceri.common.test.TestUtil;

public class BoundBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Bound<Double> b = Bound.exclusive(-33.33);
		Bound<Double> eq0 = Bound.exclusive(-33.33);
		Bound<Double> eq1 = Bound.of(-33.33, Type.exclusive);
		Bound<Double> ne0 = Bound.inclusive(-33.33);
		Bound<Double> ne1 = Bound.exclusive(-33.3);
		Bound<Double> ne2 = Bound.exclusive(33.33);
		Bound<Double> ne3 = Bound.unbound();
		Bound<Double> ne4 = Bound.of(-33.33, Type.inclusive);
		Bound<Double> ne5 = Bound.of(-33.3, Type.exclusive);
		TestUtil.exerciseEquals(b, eq0, eq1);
		assertAllNotEqual(b, ne0, ne1, ne2, ne3, ne4, ne5);
		exerciseEnum(Bound.Type.class);
	}

	@Test
	public void shouldDetermineTypeLowerLimit() {
		assertTrue(Bound.Type.exclusive.isLower(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertFalse(Bound.Type.exclusive.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertFalse(Bound.Type.inclusive.isLower(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertTrue(Bound.Type.inclusive.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertTrue(Bound.Type.exclusive.isLower(Long.MAX_VALUE, Long.MIN_VALUE));
		assertFalse(Bound.Type.exclusive.isLower(Long.MIN_VALUE, Long.MIN_VALUE));
		assertFalse(Bound.Type.inclusive.isLower(Long.MIN_VALUE, Long.MAX_VALUE));
		assertTrue(Bound.Type.inclusive.isLower(Long.MIN_VALUE, Long.MIN_VALUE));
		assertTrue(Bound.Type.exclusive.isLower(0.1f, 0.0f));
		assertFalse(Bound.Type.exclusive.isLower(0.0f, 0.0f));
		assertFalse(Bound.Type.inclusive.isLower(0.0f, 0.1f));
		assertTrue(Bound.Type.inclusive.isLower(0.0f, 0.0f));
		assertTrue(Bound.Type.exclusive.isLower(0.1, 0.0));
		assertFalse(Bound.Type.exclusive.isLower(0.0, 0.0));
		assertFalse(Bound.Type.inclusive.isLower(0.0, 0.1));
		assertTrue(Bound.Type.inclusive.isLower(0.0, 0.0));
	}

	@Test
	public void shouldDetermineTypeUpperLimit() {
		assertTrue(Bound.Type.exclusive.isUpper(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertFalse(Bound.Type.exclusive.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertFalse(Bound.Type.inclusive.isUpper(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertTrue(Bound.Type.inclusive.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertTrue(Bound.Type.exclusive.isUpper(Long.MIN_VALUE, Long.MAX_VALUE));
		assertFalse(Bound.Type.exclusive.isUpper(Long.MIN_VALUE, Long.MIN_VALUE));
		assertFalse(Bound.Type.inclusive.isUpper(Long.MAX_VALUE, Long.MIN_VALUE));
		assertTrue(Bound.Type.inclusive.isUpper(Long.MIN_VALUE, Long.MIN_VALUE));
		assertTrue(Bound.Type.exclusive.isUpper(0.0f, 0.1f));
		assertFalse(Bound.Type.exclusive.isUpper(0.0f, 0.0f));
		assertFalse(Bound.Type.inclusive.isUpper(0.1f, 0.0f));
		assertTrue(Bound.Type.inclusive.isUpper(0.0f, 0.0f));
		assertTrue(Bound.Type.exclusive.isUpper(0.0, 0.1));
		assertFalse(Bound.Type.exclusive.isUpper(0.0, 0.0));
		assertFalse(Bound.Type.inclusive.isUpper(0.1, 0.0));
		assertTrue(Bound.Type.inclusive.isUpper(0.0, 0.0));
	}

	@Test
	public void shouldBeUnboundForNullValue() {
		assertEquals(Bound.of((String) null, Type.exclusive), Bound.unbound());
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
	public void shouldNotBeABoundForNull() {
		assertFalse(Bound.unbound().lowerFor(null));
		assertFalse(Bound.unbound().upperFor(null));
		assertFalse(Bound.exclusive(1).lowerFor(null));
		assertFalse(Bound.exclusive(1).upperFor(null));
	}

	@Test
	public void shouldCompareValues() {
		assertNull(Bound.unbound().valueCompare(0));
		assertNull(Bound.exclusive(0).valueCompare(null));
		assertEquals(Bound.exclusive(0).valueCompare(0), 0);
		assertEquals(Bound.exclusive(-1).valueCompare(0), -1);
		assertEquals(Bound.exclusive(1).valueCompare(0), 1);
		Comparator<String> comparator = Comparators.STRING.reversed();
		assertEquals(Bound.inclusive("abc", comparator).valueCompare("abc"), 0);
		assertEquals(Bound.inclusive("abc", comparator).valueCompare("abd"), 1);
		assertEquals(Bound.inclusive("abc", comparator).valueCompare("abb"), -1);
		assertEquals(Bound.exclusive("abc", comparator).valueCompare("abc"), 0);
		assertEquals(Bound.exclusive("abc", comparator).valueCompare("abd"), 1);
		assertEquals(Bound.exclusive("abc", comparator).valueCompare("abb"), -1);
	}

	@Test
	public void shouldCheckValueEquality() {
		assertTrue(Bound.unbound().valueEquals(null));
		assertFalse(Bound.unbound().valueEquals(0));
		assertFalse(Bound.inclusive(0).valueEquals(null));
		assertFalse(Bound.exclusive(0).valueEquals(null));
		assertTrue(Bound.inclusive(0).valueEquals(0));
		assertFalse(Bound.inclusive(0).valueEquals(1));
		assertTrue(Bound.exclusive(0).valueEquals(0));
		assertFalse(Bound.exclusive(0).valueEquals(1));
	}

}
