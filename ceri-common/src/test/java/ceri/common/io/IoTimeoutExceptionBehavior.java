package ceri.common.io;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.io.FileNotFoundException;
import org.junit.Test;

public class IoTimeoutExceptionBehavior {

	@Test
	public void shouldWrapException() {
		Exception ex = new FileNotFoundException("test");
		IoTimeoutException e = new IoTimeoutException(ex);
		assertThat(e.getCause(), is(ex));
	}

}
