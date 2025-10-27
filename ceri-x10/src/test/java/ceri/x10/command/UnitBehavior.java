package ceri.x10.command;

import org.junit.Test;
import ceri.common.test.Assert;

public class UnitBehavior {

	@Test
	public void shouldLookupFromIndex() {
		Assert.equal(Unit.from(1), Unit._1);
		Assert.equal(Unit.from(10), Unit._10);
		Assert.equal(Unit.from(16), Unit._16);
		Assert.thrown(() -> Unit.from(0));
		Assert.thrown(() -> Unit.from(17));
	}
}
