package ceri.common.except;

import java.io.EOFException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.text.Regex;
import ceri.common.text.Strings;

public class ExceptionsTest {
	private static final Exception noMsgEx = new Exception();
	private static final Exception emptyEx = new Exception("");
	private static final Exception testEx = new Exception("test");

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Exceptions.class);
		Assert.privateConstructor(Exceptions.Filter.class);
	}

	@Test
	public void testFilterMessage() {
		Assert.equal(Exceptions.Filter.message(Strings::nonEmpty).test(null), false);
		Assert.equal(Exceptions.Filter.message(Strings::nonEmpty).test(noMsgEx), false);
		Assert.equal(Exceptions.Filter.message(Strings::nonEmpty).test(emptyEx), false);
		Assert.equal(Exceptions.Filter.message(Strings::nonEmpty).test(testEx), true);
	}

	@Test
	public void testFrom() {
		Assert.throwable(Exceptions.from(IOException::new, "%d", 123), IOException.class, "123");
	}

	@Test
	public void testNullPtr() {
		Assert.throwable(Exceptions.nullPtr("%d", 123), NullPointerException.class, "123");
	}

	@Test
	public void testIllegalArg() {
		Assert.throwable(Exceptions.illegalArg("%d", 123), IllegalArgumentException.class, "123");
	}

	@Test
	public void testIllegalState() {
		Assert.throwable(Exceptions.illegalState("%d", 123), IllegalStateException.class, "123");
	}

	@Test
	public void testUnsupportedOp() {
		Assert.throwable(Exceptions.unsupportedOp("%d", 123), UnsupportedOperationException.class,
			"123");
	}

	@Test
	public void testIo() {
		Assert.throwable(Exceptions.io("%d", 123), IOException.class, "123");
	}

	@Test
	public void testRootCause() {
		Assert.isNull(Exceptions.rootCause(null));
		var io = new IOException();
		Assert.equal(Exceptions.rootCause(io), io);
		var r = new RuntimeException(io);
		Assert.equal(Exceptions.rootCause(r), io);
	}

	@Test
	public void testMatchesThrowable() {
		Assert.no(Exceptions.matches(null, Exception.class));
		Assert.yes(Exceptions.matches(new IOException(), Exception.class));
		Assert.no(Exceptions.matches(new IOException(), RuntimeException.class));
		Assert.no(Exceptions.matches(new IOException(), String::isEmpty));
		Assert.no(Exceptions.matches(new Exception("test"), RuntimeException.class));
		Assert.yes(Exceptions.matches(new Exception("test"), s -> s.startsWith("t")));
		Assert.no(Exceptions.matches(new Exception("Test"), s -> s.startsWith("t")));
	}

	@Test
	public void testInitCause() {
		var e1 = new IllegalStateException();
		var e2 = new IllegalArgumentException();
		var e = Exceptions.initCause(e1, e2);
		Assert.equal(e.getCause(), e2);
		Exceptions.initCause(e1, null);
		Assert.equal(e1.getCause(), e2);
	}

	@Test
	public void testMessage() {
		Assert.string(Exceptions.message(null), "");
		Assert.string(Exceptions.message(new IOException()), "IOException");
		Assert.string(Exceptions.message(new Exception("test")), "test");
	}

	@Test
	public void testStackTrace() {
		Assert.string(Exceptions.stackTrace(null), "");
		var stackTrace = Exceptions.stackTrace(new Exception());
		var lines = Regex.Split.LINE.array(stackTrace);
		Assert.equal(lines[0], "java.lang.Exception");
		var fullClassName = getClass().getName();
		var className = getClass().getSimpleName();
		var methodName = Reflect.currentMethodName();
		var s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		Assert.yes(lines[1].trim().startsWith(s));
	}

	@Test
	public void testFirstStackElement() {
		Assert.isNull(Exceptions.firstStackElement(null));
		var el = Exceptions.firstStackElement(new IOException());
		Assert.equal(el.getMethodName(), Reflect.currentMethodName());
		var e = new TestException();
		Assert.isNull(Exceptions.firstStackElement(e));
		e.stackTrace = new StackTraceElement[0];
		Assert.isNull(Exceptions.firstStackElement(e));
	}

	@Test
	public void testLimitStackTrace() {
		Assert.no(Exceptions.limitStackTrace(null, 0));
		Exception e = new Exception();
		int count = e.getStackTrace().length;
		Assert.no(Exceptions.limitStackTrace(e, count + 1));
		Assert.no(Exceptions.limitStackTrace(e, count));
		Assert.yes(Exceptions.limitStackTrace(e, count - 1));
		Assert.equal(e.getStackTrace().length, count - 1);
	}

	@Test
	public void testThrowIfType() throws IOException {
		Exceptions.throwIfType(IOException.class, new InterruptedException());
		Assert.io(() -> Exceptions.throwIfType(IOException.class, new EOFException()));
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
