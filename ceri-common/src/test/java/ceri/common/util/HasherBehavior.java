package ceri.common.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class HasherBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Hasher t = Hasher.of().hash(1.1).hash(true).hash(-1L);
		Hasher eq0 = Hasher.of().hash(1.1).hash(true).hash(-1L);
		Hasher ne0 = Hasher.of().hash((float) 1.1).hash(true).hash(-1L);
		Hasher ne1 = Hasher.of().hash(1.1).hash(false).hash(-1L);
		Hasher ne2 = Hasher.of().hash(1.1).hash(true).hash(-2L);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

}
