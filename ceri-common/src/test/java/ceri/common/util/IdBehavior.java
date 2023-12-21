package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class IdBehavior {

	@Test
	public void shouldProvideDefaultIntId() {
		var id = new Id.OfInt() {};
		assertEquals(Id.OfInt.id(null, null), 0);
		assertEquals(Id.OfInt.id(id, 100), 100);
		assertEquals(Id.OfInt.id(id, null), id.id());
	}

	@Test
	public void shouldProvideDefaultLongId() {
		var id = new Id.OfLong() {};
		assertEquals(Id.OfLong.id(null, null), 0L);
		assertEquals(Id.OfLong.id(id, 100L), 100L);
		assertEquals(Id.OfLong.id(id, null), id.id());
	}

}
