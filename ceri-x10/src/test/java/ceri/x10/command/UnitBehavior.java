package ceri.x10.command;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class UnitBehavior {

	@Test
	public void shouldLookupFromIndex() {
		assertEquals(Unit.from(1), Unit._1);
		assertEquals(Unit.from(10), Unit._10);
		assertEquals(Unit.from(16), Unit._16);
		assertThrown(() -> Unit.from(0));
		assertThrown(() -> Unit.from(17));
	}

}
