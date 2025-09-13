package ceri.x10.command;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class FunctionTypeBehavior {

	@Test
	public void shouldLookupById() {
		assertEquals(FunctionType.from(1), FunctionType.allUnitsOff);
		assertEquals(FunctionType.from(3), FunctionType.on);
		assertEquals(FunctionType.from(8), FunctionType.ext);
		assertEquals(FunctionType.from(16), FunctionType.statusReq);
		assertThrown(() -> FunctionType.from(0));
		assertThrown(() -> FunctionType.from(17));
	}
}
