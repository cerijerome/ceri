package ceri.common.exception;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Test;
import ceri.common.function.ExceptionRunnable;
import ceri.common.reflect.ReflectUtil;
import ceri.common.test.Capturer;

public class ExceptionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ExceptionUtil.class);
	}

	@Test
	public void testDoNotCall() {
		assertThrown(() -> ExceptionUtil.doNotCall(1, "2"));
	}
	
	@Test
	public void testShouldNotThrow() {
		Capturer.Int capturer = Capturer.ofInt();
		ExceptionUtil.shouldNotThrow(() -> capturer.accept(1));
		ExceptionRunnable<IOException> runnable = () -> {
			capturer.accept(2);
			throw new IOException();
		};
		assertThrown(RuntimeException.class, () -> ExceptionUtil.shouldNotThrow(runnable));
		Callable<String> callable = () -> {
			capturer.accept(3);
			throw new IOException();
		};
		assertThrown(RuntimeException.class, () -> ExceptionUtil.shouldNotThrow(callable));
		capturer.verifyInt(1, 2, 3);
	}

	@Test
	public void testRootCause() {
		assertNull(ExceptionUtil.rootCause(null));
		IOException io = new IOException();
		assertThat(ExceptionUtil.rootCause(io), is(io));
		RuntimeException r = new RuntimeException(io);
		assertThat(ExceptionUtil.rootCause(r), is(io));
	}

	@Test
	public void testMatchesThrowable() {
		assertThat(ExceptionUtil.matches(null, Exception.class), is(false));
		assertThat(ExceptionUtil.matches(new IOException(), Exception.class), is(true));
		assertThat(ExceptionUtil.matches(new IOException(), RuntimeException.class), is(false));
		assertThat(ExceptionUtil.matches(new IOException(), String::isEmpty), is(false));
		assertThat(ExceptionUtil.matches(new Exception("test"), RuntimeException.class), is(false));
		assertThat(ExceptionUtil.matches(new Exception("test"), s -> s.startsWith("t")), is(true));
		assertThat(ExceptionUtil.matches(new Exception("Test"), s -> s.startsWith("t")), is(false));
	}

	@Test
	public void testInitCause() {
		IllegalStateException e1 = new IllegalStateException();
		IllegalArgumentException e2 = new IllegalArgumentException();
		IllegalStateException e = ExceptionUtil.initCause(e1, e2);
		assertThat(e.getCause(), is(e2));
		ExceptionUtil.initCause(e1, null);
		assertThat(e1.getCause(), is(e2));
	}

	@Test
	public void testMessage() {
		assertNull(ExceptionUtil.message(null));
		assertThat(ExceptionUtil.message(new IOException()), is("IOException"));
		assertThat(ExceptionUtil.message(new Exception("test")), is("test"));
	}
	
	@Test
	public void testStackTrace() {
		assertNull(ExceptionUtil.stackTrace(null));
		String stackTrace = ExceptionUtil.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertThat(lines[0], is("java.lang.Exception"));
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
		assertThat(el.getMethodName(), is(ReflectUtil.currentMethodName()));
		
		Exception e = mock(Exception.class);
		assertNull(ExceptionUtil.firstStackElement(e));
		when(e.getStackTrace()).thenReturn(new StackTraceElement[0]);
		assertNull(ExceptionUtil.firstStackElement(e));
	}
	
	@Test
	public void testLimitStackTrace() {
		assertThat(ExceptionUtil.limitStackTrace(null, 0), is(false));
		Exception e = new Exception();
		int count = e.getStackTrace().length;
		assertThat(ExceptionUtil.limitStackTrace(e, count + 1), is(false));
		assertThat(ExceptionUtil.limitStackTrace(e, count), is(false));
		assertThat(ExceptionUtil.limitStackTrace(e, count - 1), is(true));
		assertThat(e.getStackTrace().length, is(count - 1));
	}
	
	@Test
	public void testThrowIfType() throws IOException {
		ExceptionUtil.throwIfType(IOException.class, new InterruptedException());
		assertThrown(IOException.class, () -> ExceptionUtil.throwIfType(IOException.class, new EOFException()));
	}
	
}
