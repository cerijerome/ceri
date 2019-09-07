package ceri.common.factory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class FactoryExceptionBehavior {

	@Test
	public void shouldAllowNullMessageAndCause() {
		FactoryException e = new FactoryException("test");
		assertThat(e.getMessage(), is("test"));
		assertNull(e.getCause());
		FactoryException e2 = new FactoryException(e);
		assertThat(e2.getCause(), is(e));
		assertNull(e2.getMessage());
	}

}
