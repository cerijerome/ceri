package ceri.common.math;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.comparator.Comparators;
import ceri.common.math.Bound.Type;

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
		exerciseEquals(b, eq0, eq1);
		assertAllNotEqual(b, ne0, ne1, ne2, ne3, ne4, ne5);
		exerciseEnum(Bound.Type.class);
	}

	@Test
	public void shouldDetermineTypeLowerLimit() {
		assertThat(Bound.Type.exclusive.isLower(Integer.MAX_VALUE, Integer.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isLower(Integer.MIN_VALUE, Integer.MAX_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isLower(Long.MAX_VALUE, Long.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isLower(Long.MIN_VALUE, Long.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isLower(Long.MIN_VALUE, Long.MAX_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isLower(Long.MIN_VALUE, Long.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isLower(0.1f, 0.0f), is(true));
		assertThat(Bound.Type.exclusive.isLower(0.0f, 0.0f), is(false));
		assertThat(Bound.Type.inclusive.isLower(0.0f, 0.1f), is(false));
		assertThat(Bound.Type.inclusive.isLower(0.0f, 0.0f), is(true));
		assertThat(Bound.Type.exclusive.isLower(0.1, 0.0), is(true));
		assertThat(Bound.Type.exclusive.isLower(0.0, 0.0), is(false));
		assertThat(Bound.Type.inclusive.isLower(0.0, 0.1), is(false));
		assertThat(Bound.Type.inclusive.isLower(0.0, 0.0), is(true));
	}
	
	@Test
	public void shouldDetermineTypeUpperLimit() {
		assertThat(Bound.Type.exclusive.isUpper(Integer.MIN_VALUE, Integer.MAX_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isUpper(Integer.MAX_VALUE, Integer.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isUpper(Long.MIN_VALUE, Long.MAX_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isUpper(Long.MIN_VALUE, Long.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isUpper(Long.MAX_VALUE, Long.MIN_VALUE), is(false));
		assertThat(Bound.Type.inclusive.isUpper(Long.MIN_VALUE, Long.MIN_VALUE), is(true));
		assertThat(Bound.Type.exclusive.isUpper(0.0f, 0.1f), is(true));
		assertThat(Bound.Type.exclusive.isUpper(0.0f, 0.0f), is(false));
		assertThat(Bound.Type.inclusive.isUpper(0.1f, 0.0f), is(false));
		assertThat(Bound.Type.inclusive.isUpper(0.0f, 0.0f), is(true));
		assertThat(Bound.Type.exclusive.isUpper(0.0, 0.1), is(true));
		assertThat(Bound.Type.exclusive.isUpper(0.0, 0.0), is(false));
		assertThat(Bound.Type.inclusive.isUpper(0.1, 0.0), is(false));
		assertThat(Bound.Type.inclusive.isUpper(0.0, 0.0), is(true));
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
		assertThat(Bound.exclusive(0).valueCompare(0), is(0));
		assertThat(Bound.exclusive(-1).valueCompare(0), is(-1));
		assertThat(Bound.exclusive(1).valueCompare(0), is(1));
		Comparator<String> comparator = Comparators.STRING.reversed();
		assertThat(Bound.inclusive("abc", comparator).valueCompare("abc"), is(0));
		assertThat(Bound.inclusive("abc", comparator).valueCompare("abd"), is(1));
		assertThat(Bound.inclusive("abc", comparator).valueCompare("abb"), is(-1));
		assertThat(Bound.exclusive("abc", comparator).valueCompare("abc"), is(0));
		assertThat(Bound.exclusive("abc", comparator).valueCompare("abd"), is(1));
		assertThat(Bound.exclusive("abc", comparator).valueCompare("abb"), is(-1));
	}
	
	@Test
	public void shouldCheckValueEquality() {
		assertThat(Bound.unbound().valueEquals(null), is(true));
		assertThat(Bound.unbound().valueEquals(0), is(false));
		assertThat(Bound.inclusive(0).valueEquals(null), is(false));
		assertThat(Bound.exclusive(0).valueEquals(null), is(false));
		assertThat(Bound.inclusive(0).valueEquals(0), is(true));
		assertThat(Bound.inclusive(0).valueEquals(1), is(false));
		assertThat(Bound.exclusive(0).valueEquals(0), is(true));
		assertThat(Bound.exclusive(0).valueEquals(1), is(false));
	}
	
}
