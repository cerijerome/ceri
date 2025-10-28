package ceri.common.math;

import static ceri.common.test.Testing.exerciseEnum;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.function.Compares;
import ceri.common.math.Bound.Type;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class BoundBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Bound<Double> b = Bound.exclusive(-33.33);
		Bound<Double> eq0 = Bound.exclusive(-33.33);
		Bound<Double> eq1 = Bound.of(-33.33, Type.exc);
		Bound<Double> ne0 = Bound.inclusive(-33.33);
		Bound<Double> ne1 = Bound.exclusive(-33.3);
		Bound<Double> ne2 = Bound.exclusive(33.33);
		Bound<Double> ne3 = Bound.unbound();
		Bound<Double> ne4 = Bound.of(-33.33, Type.inc);
		Bound<Double> ne5 = Bound.of(-33.3, Type.exc);
		Testing.exerciseEquals(b, eq0, eq1);
		Assert.notEqualAll(b, ne0, ne1, ne2, ne3, ne4, ne5);
		exerciseEnum(Bound.Type.class);
	}

	@Test
	public void shouldDetermineTypeLowerLimit() {
		Assert.yes(Bound.Type.exc.isLower(Integer.MAX_VALUE, Integer.MIN_VALUE));
		Assert.no(Bound.Type.exc.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE));
		Assert.no(Bound.Type.inc.isLower(Integer.MIN_VALUE, Integer.MAX_VALUE));
		Assert.yes(Bound.Type.inc.isLower(Integer.MIN_VALUE, Integer.MIN_VALUE));
		Assert.yes(Bound.Type.exc.isLower(Long.MAX_VALUE, Long.MIN_VALUE));
		Assert.no(Bound.Type.exc.isLower(Long.MIN_VALUE, Long.MIN_VALUE));
		Assert.no(Bound.Type.inc.isLower(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.yes(Bound.Type.inc.isLower(Long.MIN_VALUE, Long.MIN_VALUE));
		Assert.yes(Bound.Type.exc.isLower(0.1f, 0.0f));
		Assert.no(Bound.Type.exc.isLower(0.0f, 0.0f));
		Assert.no(Bound.Type.inc.isLower(0.0f, 0.1f));
		Assert.yes(Bound.Type.inc.isLower(0.0f, 0.0f));
		Assert.yes(Bound.Type.exc.isLower(0.1, 0.0));
		Assert.no(Bound.Type.exc.isLower(0.0, 0.0));
		Assert.no(Bound.Type.inc.isLower(0.0, 0.1));
		Assert.yes(Bound.Type.inc.isLower(0.0, 0.0));
	}

	@Test
	public void shouldDetermineTypeUpperLimit() {
		Assert.yes(Bound.Type.exc.isUpper(Integer.MIN_VALUE, Integer.MAX_VALUE));
		Assert.no(Bound.Type.exc.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE));
		Assert.no(Bound.Type.inc.isUpper(Integer.MAX_VALUE, Integer.MIN_VALUE));
		Assert.yes(Bound.Type.inc.isUpper(Integer.MIN_VALUE, Integer.MIN_VALUE));
		Assert.yes(Bound.Type.exc.isUpper(Long.MIN_VALUE, Long.MAX_VALUE));
		Assert.no(Bound.Type.exc.isUpper(Long.MIN_VALUE, Long.MIN_VALUE));
		Assert.no(Bound.Type.inc.isUpper(Long.MAX_VALUE, Long.MIN_VALUE));
		Assert.yes(Bound.Type.inc.isUpper(Long.MIN_VALUE, Long.MIN_VALUE));
		Assert.yes(Bound.Type.exc.isUpper(0.0f, 0.1f));
		Assert.no(Bound.Type.exc.isUpper(0.0f, 0.0f));
		Assert.no(Bound.Type.inc.isUpper(0.1f, 0.0f));
		Assert.yes(Bound.Type.inc.isUpper(0.0f, 0.0f));
		Assert.yes(Bound.Type.exc.isUpper(0.0, 0.1));
		Assert.no(Bound.Type.exc.isUpper(0.0, 0.0));
		Assert.no(Bound.Type.inc.isUpper(0.1, 0.0));
		Assert.yes(Bound.Type.inc.isUpper(0.0, 0.0));
	}

	@Test
	public void shouldBeUnboundForNullValue() {
		Assert.equal(Bound.of((String) null, Type.exc), Bound.unbound());
	}

	@Test
	public void shouldEncapsulateUnboundedness() {
		Bound<Double> b = Bound.unbound();
		Assert.yes(b.isUnbound());
		Assert.yes(b.lowerFor(-33.33));
		Assert.yes(b.lowerFor(33.33));
		Assert.yes(b.lowerFor(Double.MAX_VALUE));
		Assert.yes(b.lowerFor(-Double.MAX_VALUE));
		Assert.yes(b.upperFor(-33.33));
		Assert.yes(b.upperFor(33.33));
		Assert.yes(b.upperFor(Double.MAX_VALUE));
		Assert.yes(b.upperFor(-Double.MAX_VALUE));
	}

	@Test
	public void shouldEncapsulateInclusiveBounds() {
		Bound<Double> b = Bound.inclusive(-33.33);
		Assert.no(b.isUnbound());
		Assert.yes(b.lowerFor(-33.3));
		Assert.yes(b.lowerFor(-33.33));
		Assert.no(b.lowerFor(-33.34));
		Assert.yes(b.lowerFor(33.33));
		Assert.no(b.upperFor(-33.3));
		Assert.yes(b.upperFor(-33.33));
		Assert.yes(b.upperFor(-33.34));
		Assert.no(b.upperFor(33.33));
	}

	@Test
	public void shouldEncapsulateExclusiveBounds() {
		Bound<Double> b = Bound.exclusive(-33.33);
		Assert.no(b.isUnbound());
		Assert.yes(b.lowerFor(-33.3));
		Assert.no(b.lowerFor(-33.33));
		Assert.no(b.lowerFor(-33.34));
		Assert.yes(b.lowerFor(33.33));
		Assert.no(b.upperFor(-33.3));
		Assert.no(b.upperFor(-33.33));
		Assert.yes(b.upperFor(-33.34));
		Assert.no(b.upperFor(33.33));
	}

	@Test
	public void shouldNotBeABoundForNull() {
		Assert.no(Bound.unbound().lowerFor(null));
		Assert.no(Bound.unbound().upperFor(null));
		Assert.no(Bound.exclusive(1).lowerFor(null));
		Assert.no(Bound.exclusive(1).upperFor(null));
	}

	@Test
	public void shouldCompareValues() {
		Assert.isNull(Bound.unbound().valueCompare(0));
		Assert.isNull(Bound.exclusive(0).valueCompare(null));
		Assert.equal(Bound.exclusive(0).valueCompare(0), 0);
		Assert.equal(Bound.exclusive(-1).valueCompare(0), -1);
		Assert.equal(Bound.exclusive(1).valueCompare(0), 1);
		Comparator<String> comparator = Compares.STRING.reversed();
		Assert.equal(Bound.inclusive("abc", comparator).valueCompare("abc"), 0);
		Assert.equal(Bound.inclusive("abc", comparator).valueCompare("abd"), 1);
		Assert.equal(Bound.inclusive("abc", comparator).valueCompare("abb"), -1);
		Assert.equal(Bound.exclusive("abc", comparator).valueCompare("abc"), 0);
		Assert.equal(Bound.exclusive("abc", comparator).valueCompare("abd"), 1);
		Assert.equal(Bound.exclusive("abc", comparator).valueCompare("abb"), -1);
	}

	@Test
	public void shouldCheckValueEquality() {
		Assert.yes(Bound.unbound().valueEquals(null));
		Assert.no(Bound.unbound().valueEquals(0));
		Assert.no(Bound.inclusive(0).valueEquals(null));
		Assert.no(Bound.exclusive(0).valueEquals(null));
		Assert.yes(Bound.inclusive(0).valueEquals(0));
		Assert.no(Bound.inclusive(0).valueEquals(1));
		Assert.yes(Bound.exclusive(0).valueEquals(0));
		Assert.no(Bound.exclusive(0).valueEquals(1));
	}

}
