package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class Ratio2dBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var r = Ratio2d.of(5, 10);
		var eq0 = Ratio2d.of(5, 10);
		var ne0 = Ratio2d.of(4.999, 10);
		var ne1 = Ratio2d.of(5, 10.001);
		var ne2 = Ratio2d.uniform(5);
		var ne3 = Ratio2d.of(0, 10);
		var ne4 = Ratio2d.of(5, 0);
		var ne5 = Ratio2d.of(1, 10);
		var ne6 = Ratio2d.of(5, 1);
		var ne7 = Ratio2d.of(0, 0);
		var ne8 = Ratio2d.of(1, 1);
		Testing.exerciseEquals(r, eq0);
		Assert.notEqualAll(r, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7, ne8);
		Assert.equal(r.equals(5, 10), true);
		Assert.equal(r.equals(6, 10), false);
		Assert.equal(r.equals(5, 11), false);
	}

	@Test
	public void shouldDetermineIfZero() {
		Assert.equal(Ratio2d.ZERO.isZero(), true);
		Assert.equal(Ratio2d.UNIT.isZero(), false);
	}

	@Test
	public void shouldDetermineIfUnit() {
		Assert.equal(Ratio2d.ZERO.isUnit(), false);
		Assert.equal(Ratio2d.UNIT.isUnit(), true);
	}

	@Test
	public void shouldMultiply() {
		GeomAssert.approx(Ratio2d.of(2, 4).multiply(Ratio2d.ZERO), 0, 0);
		GeomAssert.approx(Ratio2d.of(2, 4).multiply(0.5), 1, 2);
		GeomAssert.approx(Ratio2d.of(2, 4).multiply(1), 2, 4);
	}
}
