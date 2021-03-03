package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class BiMapBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = BiMap.of(1, "1", 2, "2", 3, "3");
		var eq0 = BiMap.of(1, "1", 2, "2", 3, "3");
		var eq1 = BiMap.<Integer, String>builder().putAll(t.keys).build();
		var eq2 = BiMap.of(2, "2", 3, "3", 1, "1");
		var ne0 = BiMap.of(2, "1", 2, "2", 3, "3");
		var ne1 = BiMap.of(1, "2", 2, "2", 3, "3");
		var ne2 = BiMap.of();
		var ne3 = BiMap.of(1, "1");
		var ne4 = BiMap.of(1, "1", 2, "2");
		var ne5 = BiMap.of(1, "1", 2, "2", 3, "3", 4, "4");
		exerciseEquals(t, eq0, eq1);
		assertEquals(t, eq2); // toString() differs
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

}
