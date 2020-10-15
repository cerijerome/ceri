package ceri.common.process;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import org.junit.Test;

public class ProcessCommandBehavior {

	@Test
	public void shouldAllowNullSupplier() throws IOException {
		ProcessCommand cmd = ProcessCommand.of(null, null);
		assertThat(cmd.command().isEmpty(), is(true));
		assertNull(cmd.start());
	}

}
