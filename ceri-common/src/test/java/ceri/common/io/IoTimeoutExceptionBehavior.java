package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
