package ceri.x10.command;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class UnitBehavior {

	@Test
	public void shouldLookupFromIndex() {
		assertEquals(Unit.from(1), Unit._1);
		assertEquals(Unit.from(10), Unit._10);
		assertEquals(Unit.from(16), Unit._16);
		Assert.thrown(() -> Unit.from(0));
		Assert.thrown(() -> Unit.from(17));
	}
}
