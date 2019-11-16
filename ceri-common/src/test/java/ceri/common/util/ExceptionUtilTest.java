package ceri.common.util;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Test;
import ceri.common.reflect.ReflectUtil;

public class ExceptionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ExceptionUtil.class);
	}

	@Test
	public void testShouldNotThrow() {
		Callable<String> callable = () -> {
			throw new IOException();
		};
		assertThrown(RuntimeException.class, () -> ExceptionUtil.shouldNotThrow(callable));
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
	public void testStackTrace() {
		String stackTrace = ExceptionUtil.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertThat(lines[0], is("java.lang.Exception"));
		String fullClassName = getClass().getName();
		String className = getClass().getSimpleName();
		String methodName = ReflectUtil.currentMethodName();
		String s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		assertTrue(lines[1].trim().startsWith(s));
	}

}
