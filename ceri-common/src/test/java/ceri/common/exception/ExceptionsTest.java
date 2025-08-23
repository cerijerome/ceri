package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertInstance;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.EOFException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Excepts.Supplier;
import ceri.common.reflect.Reflect;
import ceri.common.test.TestUtil.Rte;

public class ExceptionsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Exceptions.class);
	}
	
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

	@Test
	public void testRteStub() {
		assertInstance(new Rte("test"), RuntimeException.class);
		Supplier<Rte, String> supplier = () -> "test";
		assertEquals(supplier.get(), "test");
	}

	@Test
	public void testRootCause() {
		assertNull(Exceptions.rootCause(null));
		IOException io = new IOException();
		assertEquals(Exceptions.rootCause(io), io);
		RuntimeException r = new RuntimeException(io);
		assertEquals(Exceptions.rootCause(r), io);
	}

	@Test
	public void testMatchesThrowable() {
		assertFalse(Exceptions.matches(null, Exception.class));
		assertTrue(Exceptions.matches(new IOException(), Exception.class));
		assertFalse(Exceptions.matches(new IOException(), RuntimeException.class));
		assertFalse(Exceptions.matches(new IOException(), String::isEmpty));
		assertFalse(Exceptions.matches(new Exception("test"), RuntimeException.class));
		assertTrue(Exceptions.matches(new Exception("test"), s -> s.startsWith("t")));
		assertFalse(Exceptions.matches(new Exception("Test"), s -> s.startsWith("t")));
	}

	@Test
	public void testInitCause() {
		IllegalStateException e1 = new IllegalStateException();
		IllegalArgumentException e2 = new IllegalArgumentException();
		IllegalStateException e = Exceptions.initCause(e1, e2);
		assertEquals(e.getCause(), e2);
		Exceptions.initCause(e1, null);
		assertEquals(e1.getCause(), e2);
	}

	@Test
	public void testMessage() {
		assertNull(Exceptions.message(null));
		assertEquals(Exceptions.message(new IOException()), "IOException");
		assertEquals(Exceptions.message(new Exception("test")), "test");
	}

	@Test
	public void testStackTrace() {
		assertNull(Exceptions.stackTrace(null));
		String stackTrace = Exceptions.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertEquals(lines[0], "java.lang.Exception");
		String fullClassName = getClass().getName();
		String className = getClass().getSimpleName();
		String methodName = Reflect.currentMethodName();
		String s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		assertTrue(lines[1].trim().startsWith(s));
	}

	@Test
	public void testFirstStackElement() {
		assertNull(Exceptions.firstStackElement(null));
		StackTraceElement el = Exceptions.firstStackElement(new IOException());
		assertEquals(el.getMethodName(), Reflect.currentMethodName());
		TestException e = new TestException();
		assertNull(Exceptions.firstStackElement(e));
		e.stackTrace = new StackTraceElement[0];
		assertNull(Exceptions.firstStackElement(e));
	}

	@Test
	public void testLimitStackTrace() {
		assertFalse(Exceptions.limitStackTrace(null, 0));
		Exception e = new Exception();
		int count = e.getStackTrace().length;
		assertFalse(Exceptions.limitStackTrace(e, count + 1));
		assertFalse(Exceptions.limitStackTrace(e, count));
		assertTrue(Exceptions.limitStackTrace(e, count - 1));
		assertEquals(e.getStackTrace().length, count - 1);
	}

	@Test
	public void testThrowIfType() throws IOException {
		Exceptions.throwIfType(IOException.class, new InterruptedException());
		assertIoe(() -> Exceptions.throwIfType(IOException.class, new EOFException()));
	}

	private static class TestException extends Exception {
		private static final long serialVersionUID = 1L;
		public StackTraceElement[] stackTrace = null;

		@Override
		public StackTraceElement[] getStackTrace() {
			return stackTrace;
		}
	}
}
