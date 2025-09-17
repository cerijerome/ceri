package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertInstance;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.EOFException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.reflect.Reflect;
import ceri.common.test.TestUtil;
import ceri.common.text.Regex;
import ceri.common.text.Strings;

public class ExceptionsTest {
	private static final Exception noMsgEx = new Exception();
	private static final Exception emptyEx = new Exception("");
	private static final Exception testEx = new Exception("test");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Exceptions.class);
		assertPrivateConstructor(Exceptions.Filter.class);
	}

	@Test
	public void testFilterMessage() {
		assertEquals(Exceptions.Filter.message(Strings::nonEmpty).test(null), false);
		assertEquals(Exceptions.Filter.message(Strings::nonEmpty).test(noMsgEx), false);
		assertEquals(Exceptions.Filter.message(Strings::nonEmpty).test(emptyEx), false);
		assertEquals(Exceptions.Filter.message(Strings::nonEmpty).test(testEx), true);
	}

	@Test
	public void testFrom() {
		assertThrowable(Exceptions.from(IOException::new, "%d", 123), IOException.class, "123");
	}

	@Test
	public void testNullPtr() {
		assertThrowable(Exceptions.nullPtr("%d", 123), NullPointerException.class, "123");
	}

	@Test
	public void testIllegalArg() {
		assertThrowable(Exceptions.illegalArg("%d", 123), IllegalArgumentException.class, "123");
	}

	@Test
	public void testIllegalState() {
		assertThrowable(Exceptions.illegalState("%d", 123), IllegalStateException.class, "123");
	}

	@Test
	public void testUnsupportedOp() {
		assertThrowable(Exceptions.unsupportedOp("%d", 123), UnsupportedOperationException.class,
			"123");
	}

	@Test
	public void testIo() {
		assertThrowable(Exceptions.io("%d", 123), IOException.class, "123");
	}

	@Test
	public void testRteStub() {
		assertInstance(new TestUtil.Rte("test"), RuntimeException.class);
		Excepts.Supplier<TestUtil.Rte, String> supplier = () -> "test";
		assertEquals(supplier.get(), "test");
	}

	@Test
	public void testRootCause() {
		assertNull(Exceptions.rootCause(null));
		var io = new IOException();
		assertEquals(Exceptions.rootCause(io), io);
		var r = new RuntimeException(io);
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
		var e1 = new IllegalStateException();
		var e2 = new IllegalArgumentException();
		var e = Exceptions.initCause(e1, e2);
		assertEquals(e.getCause(), e2);
		Exceptions.initCause(e1, null);
		assertEquals(e1.getCause(), e2);
	}

	@Test
	public void testMessage() {
		assertString(Exceptions.message(null), "");
		assertString(Exceptions.message(new IOException()), "IOException");
		assertString(Exceptions.message(new Exception("test")), "test");
	}

	@Test
	public void testStackTrace() {
		assertString(Exceptions.stackTrace(null), "");
		var stackTrace = Exceptions.stackTrace(new Exception());
		var lines = Regex.Split.LINE.array(stackTrace);
		assertEquals(lines[0], "java.lang.Exception");
		var fullClassName = getClass().getName();
		var className = getClass().getSimpleName();
		var methodName = Reflect.currentMethodName();
		var s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		assertTrue(lines[1].trim().startsWith(s));
	}

	@Test
	public void testFirstStackElement() {
		assertNull(Exceptions.firstStackElement(null));
		var el = Exceptions.firstStackElement(new IOException());
		assertEquals(el.getMethodName(), Reflect.currentMethodName());
		var e = new TestException();
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

	@SuppressWarnings("serial")
	private static class TestException extends Exception {
		public StackTraceElement[] stackTrace = null;

		@Override
		public StackTraceElement[] getStackTrace() {
			return stackTrace;
		}
	}
}
