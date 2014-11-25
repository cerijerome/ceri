package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class RuntimeIoExceptionBehavior {

	@Test
	public void shouldWrapIoException() {
		IOException io = new FileNotFoundException("test");
		RuntimeIoException e = new RuntimeIoException(io);
		assertThat(e.getCause(), is(io));
	}

}
