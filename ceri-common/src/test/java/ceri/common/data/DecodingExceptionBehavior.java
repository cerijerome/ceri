package ceri.common.data;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;

public class DecodingExceptionBehavior {

	@Test
	public void should() {
		assertThat(new DecodingException("test").getMessage(), containsString("test"));
		assertNotNull(new DecodingException(new IOException()).getCause());
		assertThat(new DecodingException("test", new IOException()).getMessage(),
			containsString("test"));
	}

}
