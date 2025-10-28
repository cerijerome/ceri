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
		Hasher t = Hasher.of().hash(1.1).hash(true).hash(-1L);
		Hasher eq0 = Hasher.of().hash(1.1).hash(true).hash(-1L);
		Hasher ne0 = Hasher.of().hash((float) 1.1).hash(true).hash(-1L);
		Hasher ne1 = Hasher.of().hash(1.1).hash(false).hash(-1L);
		Hasher ne2 = Hasher.of().hash(1.1).hash(true).hash(-2L);
		Hasher ne3 = Hasher.of().hash(1.1).hash(true).hash(-1L).hash(null);
		Hasher ne4 = Hasher.of().hash(1.1).hash(true).hash(-1L).hash("");
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4);
	}

}
