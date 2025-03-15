package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertInstance;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Test;
import ceri.common.exception.ExceptionUtil.Rte;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.Captor;

public class ExceptionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ExceptionUtil.class);
	}

	@Test
	public void testRteStub() {
		assertInstance(new Rte(), RuntimeException.class);
		ExceptionSupplier<Rte, String> supplier = () -> "test";
		assertEquals(supplier.get(), "test");
	}

	@Test
	public void testDoNotCall() {
		assertRte(() -> ExceptionUtil.doNotCall(1, "2"));
	}

	@Test
	public void testIllegalArg() {
		assertThrowable(ExceptionUtil.illegalArg("test%d", 123), IllegalArgumentException.class,
			"test123");
	}

	@Test
	public void testUnsupportedOp() {
		assertThrowable(ExceptionUtil.unsupportedOp("test%d", 123),
			UnsupportedOperationException.class, "test123");
	}

	@Test
	public void testExceptionf() {
		var e = ExceptionUtil.exceptionf(IOException::new, "test%d", 123);
		assertThrowable(e, IOException.class, "test123");
	}

	@Test
	public void testThrowUnchecked() {
		assertIoe(() -> ExceptionUtil.throwUnchecked(new IOException("io")));
	}

	@Test
	public void testShouldNotThrow() {
		Captor.OfInt capturer = Captor.ofInt();
		ExceptionUtil.shouldNotThrow(() -> capturer.accept(1));
		ExceptionRunnable<IOException> runnable = () -> {
			capturer.accept(2);
			throw new IOException();
		};
		assertRte(() -> ExceptionUtil.shouldNotThrow(runnable));
		Callable<String> callable = () -> {
			capturer.accept(3);
			throw new IOException();
		};
		assertRte(() -> ExceptionUtil.shouldNotThrow(callable));
		capturer.verifyInt(1, 2, 3);
	}

	@Test
	public void testRootCause() {
		assertNull(ExceptionUtil.rootCause(null));
		IOException io = new IOException();
		assertEquals(ExceptionUtil.rootCause(io), io);
		RuntimeException r = new RuntimeException(io);
		assertEquals(ExceptionUtil.rootCause(r), io);
	}

	@Test
	public void testMatchesThrowable() {
		assertFalse(ExceptionUtil.matches(null, Exception.class));
		assertTrue(ExceptionUtil.matches(new IOException(), Exception.class));
		assertFalse(ExceptionUtil.matches(new IOException(), RuntimeException.class));
		assertFalse(ExceptionUtil.matches(new IOException(), String::isEmpty));
		assertFalse(ExceptionUtil.matches(new Exception("test"), RuntimeException.class));
		assertTrue(ExceptionUtil.matches(new Exception("test"), s -> s.startsWith("t")));
		assertFalse(ExceptionUtil.matches(new Exception("Test"), s -> s.startsWith("t")));
	}

	@Test
	public void testInitCause() {
		IllegalStateException e1 = new IllegalStateException();
		IllegalArgumentException e2 = new IllegalArgumentException();
		IllegalStateException e = ExceptionUtil.initCause(e1, e2);
		assertEquals(e.getCause(), e2);
		ExceptionUtil.initCause(e1, null);
		assertEquals(e1.getCause(), e2);
	}

	@Test
	public void testMessage() {
		assertNull(ExceptionUtil.message(null));
		assertEquals(ExceptionUtil.message(new IOException()), "IOException");
		assertEquals(ExceptionUtil.message(new Exception("test")), "test");
	}

	@Test
	public void testStackTrace() {
		assertNull(ExceptionUtil.stackTrace(null));
		String stackTrace = ExceptionUtil.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertEquals(lines[0], "java.lang.Exception");
		String fullClassName = getClass().getName();
		String className = getClass().getSimpleName();
		String methodName = ReflectUtil.currentMethodName();
		String s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		assertTrue(lines[1].trim().startsWith(s));
	}

	@Test
	public void testFirstStackElement() {
		assertNull(ExceptionUtil.firstStackElement(null));
		StackTraceElement el = ExceptionUtil.firstStackElement(new IOException());
		assertEquals(el.getMethodName(), ReflectUtil.currentMethodName());
		TestException e = new TestException();
		assertNull(ExceptionUtil.firstStackElement(e));
		e.stackTrace = new StackTraceElement[0];
		assertNull(ExceptionUtil.firstStackElement(e));
	}

	@Test
	public void testLimitStackTrace() {
		assertFalse(ExceptionUtil.limitStackTrace(null, 0));
		Exception e = new Exception();
		int count = e.getStackTrace().length;
		assertFalse(ExceptionUtil.limitStackTrace(e, count + 1));
		assertFalse(ExceptionUtil.limitStackTrace(e, count));
		assertTrue(ExceptionUtil.limitStackTrace(e, count - 1));
		assertEquals(e.getStackTrace().length, count - 1);
	}

	@Test
	public void testThrowIfType() throws IOException {
		ExceptionUtil.throwIfType(IOException.class, new InterruptedException());
		assertIoe(() -> ExceptionUtil.throwIfType(IOException.class, new EOFException()));
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
