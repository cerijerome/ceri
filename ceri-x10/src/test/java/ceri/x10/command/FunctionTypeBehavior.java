package ceri.x10.command;

import org.junit.Test;
import ceri.common.test.Assert;

public class FunctionTypeBehavior {

	@Test
	public void shouldLookupById() {
		Assert.equal(FunctionType.from(1), FunctionType.allUnitsOff);
		Assert.equal(FunctionType.from(3), FunctionType.on);
		Assert.equal(FunctionType.from(8), FunctionType.ext);
		Assert.equal(FunctionType.from(16), FunctionType.statusReq);
		Assert.thrown(() -> FunctionType.from(0));
		Assert.thrown(() -> FunctionType.from(17));
	}
}
