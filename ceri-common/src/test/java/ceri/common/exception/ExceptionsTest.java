package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertThrowable;
import java.io.IOException;
import org.junit.Test;

public class ExceptionsTest {

	@Test
	public void testIllegalArg() {
		assertThrowable(Exceptions.illegalArg("test%d", 123), IllegalArgumentException.class,
			"test123");
	}

	@Test
	public void testIllegalState() {
		assertThrowable(Exceptions.illegalState("test%d", 123), IllegalStateException.class,
			"test123");
	}

	@Test
	public void testUnsupportedOp() {
		assertThrowable(Exceptions.unsupportedOp("test%d", 123),
			UnsupportedOperationException.class, "test123");
	}

	@Test
	public void testExceptionf() {
		var e = Exceptions.from(IOException::new, "test%d", 123);
		assertThrowable(e, IOException.class, "test123");
	}

}
