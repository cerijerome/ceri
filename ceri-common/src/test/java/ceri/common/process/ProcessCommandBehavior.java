package ceri.common.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
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
