package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class IdentifiedBehavior {

	@Test
	public void shouldProvideDefaultIntId() {
		var id = new Identified.ByInt() {};
		assertEquals(Identified.ByInt.id(null, null), 0);
		assertEquals(Identified.ByInt.id(id, 100), 100);
		assertEquals(Identified.ByInt.id(id, null), id.id());
	}

	@Test
	public void shouldProvideDefaultLongId() {
		var id = new Identified.ByLong() {};
		assertEquals(Identified.ByLong.id(null, null), 0L);
		assertEquals(Identified.ByLong.id(id, 100L), 100L);
		assertEquals(Identified.ByLong.id(id, null), id.id());
	}

}
