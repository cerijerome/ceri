package ceri.common.reflect;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CreateExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		CreateException e = new CreateException("test");
		assertThat(e.getMessage(), is("test"));
		assertNull(e.getCause());
		CreateException e2 = new CreateException(e);
		assertThat(e2.getCause(), is((Throwable) e));
		assertNull(e2.getMessage());
	}

}
