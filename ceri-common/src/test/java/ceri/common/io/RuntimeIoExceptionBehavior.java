package ceri.common.io;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
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
		assertEquals(e.getCause(), io);
	}

}
