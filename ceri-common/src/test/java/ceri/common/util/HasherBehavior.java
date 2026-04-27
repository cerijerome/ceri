package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class HasherBehavior {

	@Test
	public void shouldProvideDeepHash() {
		boolean[] bb = { true, false };
		int[][] ii = { { 3 }, { 1, -1 } };
		Assert.equal(Hasher.deep(bb, ii), 0x1326b8);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Hasher.of().add(1.1).add(true).add(-1L);
		var eq0 = Hasher.of().add(1.1).add(true).add(-1L);
		var ne0 = Hasher.of().add((float) 1.1).add(true).add(-1L);
		var ne1 = Hasher.of().add(1.1).add(false).add(-1L);
		var ne2 = Hasher.of().add(1.1).add(true).add(-2L);
		var ne3 = Hasher.of().add(1.1).add(true).add(-1L).add(null);
		var ne4 = Hasher.of().add(1.1).add(true).add(-1L).add("");
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDeepHash() {
		int[][] ii = { { 3 }, { 1, -1 } };
		Assert.equal(Hasher.of().deepAdd(null).hashCode(), 31);
		Assert.equal(Hasher.of().deepAdd(ii).hashCode(), 0xbfc);
	}
}
