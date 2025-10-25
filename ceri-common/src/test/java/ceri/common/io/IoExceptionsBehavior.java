package ceri.common.io;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class IoExceptionsBehavior {

	@Test
	public void shouldCreateRuntimeExceptions() {
		var e = new IoExceptions.Runtime("test");
		var ne0 = new IoExceptions.Runtime("test");
		var ne1 = new IoExceptions.Runtime(e);
		var ne2 = new IoExceptions.Runtime("test", e);
		assertAllNotEqual(e, ne0, ne1, ne2);
	}

	@Test
	public void shouldWrapRuntimeIoException() {
		var io = new FileNotFoundException("test");
		var e = new IoExceptions.Runtime(io);
		assertEquals(e.getCause(), io);
	}

	@Test
	public void shouldCreateEofExceptions() {
		var e0 = IoExceptions.RuntimeEof.of();
		var e1 = IoExceptions.RuntimeEof.of("test");
		var e2 = IoExceptions.RuntimeEof.of("%s", "test");
		var e3 = IoExceptions.RuntimeEof.of(new IOException(), "%s", "test");
		assertAllNotEqual(e0, e1, e2, e3);
		assertEquals(e1.getMessage(), e2.getMessage());
		assertEquals(e1.getMessage(), e3.getMessage());
	}

	@Test
	public void shouldWrapTimeoutException() {
		var ex = new FileNotFoundException("test");
		var e = new IoExceptions.Timeout(ex);
		assertEquals(e.getCause(), ex);
	}

	@Test
	public void testIncompleteVerifyMinimumBytesTransferred() throws IOException {
		IoExceptions.Incomplete.verify(100, 100);
		IoExceptions.Incomplete.verify(100, 99);
		Assert.thrown(() -> IoExceptions.Incomplete.verify(99, 100));
	}

	@Test
	public void shouldCreateIncompleteWithCause() {
		var e = IoExceptions.Incomplete.of(99, 100, new IOException("test"));
		assertEquals(e.actual, 99);
		assertEquals(e.expected, 100);
	}
}
