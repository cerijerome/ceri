package ceri.common.reflect;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class CreateExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		CreateException e = new CreateException("test");
		assertThat(e.getMessage(), is("test"));
		assertNull(e.getCause());
		CreateException e2 = new CreateException(e);
		assertThat(e2.getCause(), is(e));
		assertNull(e2.getMessage());
	}

}
