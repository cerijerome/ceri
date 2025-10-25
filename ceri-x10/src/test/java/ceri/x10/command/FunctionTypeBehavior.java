package ceri.x10.command;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class FunctionTypeBehavior {

	@Test
	public void shouldLookupById() {
		assertEquals(FunctionType.from(1), FunctionType.allUnitsOff);
		assertEquals(FunctionType.from(3), FunctionType.on);
		assertEquals(FunctionType.from(8), FunctionType.ext);
		assertEquals(FunctionType.from(16), FunctionType.statusReq);
		Assert.thrown(() -> FunctionType.from(0));
		Assert.thrown(() -> FunctionType.from(17));
	}
}
