package ceri.x10.command;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class FunctionTypeBehavior {

	@Test
	public void shouldLookupById() {
		assertThat(FunctionType.from(1), is(FunctionType.allUnitsOff));
		assertThat(FunctionType.from(3), is(FunctionType.on));
		assertThat(FunctionType.from(8), is(FunctionType.ext));
		assertThat(FunctionType.from(16), is(FunctionType.statusReq));
		assertThrown(() -> FunctionType.from(0));
		assertThrown(() -> FunctionType.from(17));
	}

}
