package ceri.common.process;

import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;

public class ProcessCommandBehavior {

	@Test
	public void shouldAllowNullSupplier() throws IOException {
		ProcessCommand cmd = ProcessCommand.of(null, null);
		assertTrue(cmd.command().isEmpty());
		assertNull(cmd.start());
	}

}
