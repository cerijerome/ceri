package ceri.common.io;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class RuntimeIoExceptionBehavior {

	@Test
	public void shouldCreateExceptions() {
		RuntimeIoException e = new RuntimeIoException("test");
		RuntimeIoException ne0 = new RuntimeIoException("test");
		RuntimeIoException ne1 = new RuntimeIoException(e);
		RuntimeIoException ne2 = new RuntimeIoException("test", e);
		assertAllNotEqual(e, ne0, ne1, ne2);
	}

	@Test
	public void shouldWrapIoException() {
		IOException io = new FileNotFoundException("test");
		RuntimeIoException e = new RuntimeIoException(io);
		assertThat(e.getCause(), is(io));
	}

}
