package ceri.common.util;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.mockito.Mock;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.reflect.ReflectUtil;

public class BasicUtilTest {
	@Mock
	Class<?> badClass;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BasicUtil.class);
	}

	@Test
	public void testBeep() {
		// Make sure no error thrown
		BasicUtil.beep();
	}

	@Test
	public void shouldReturnElapsedTimeInMicros() {
		long t0 = BasicUtil.microTime();
		long t1 = BasicUtil.microTime();
		assertTrue(t1 >= t0);
	}

	@Test
	public void testShouldNotThrow() {
		Callable<String> callable = () -> {
			throw new IOException();
		};
		assertException(RuntimeException.class, () -> BasicUtil.shouldNotThrow(callable));
	}

	@Test
	public void testRootCause() {
		assertNull(BasicUtil.rootCause(null));
		IOException io = new IOException();
		assertThat(BasicUtil.rootCause(io), is(io));
		RuntimeException r = new RuntimeException(io);
		assertThat(BasicUtil.rootCause(r), is(io));
	}

	@Test
	public void testFind() {
		assertThat(BasicUtil.find(HAlign.class, t -> t != HAlign.left), is(HAlign.center));
		assertThat(BasicUtil.find(HAlign.class, t -> t.name().endsWith("t")), is(HAlign.left));
		assertNull(BasicUtil.find(HAlign.class, t -> t.name().endsWith("x")));
	}

	@Test
	public void testEnums() {
		assertIterable(BasicUtil.enums(HAlign.class), HAlign.left, HAlign.center, HAlign.right);
		assertIterable(BasicUtil.enums(VAlign.class), VAlign.top, VAlign.middle, VAlign.bottom);
		exerciseEnum(HAlign.class);
		exerciseEnum(VAlign.class);
	}

	@Test
	public void testMatchesThrowable() {
		assertThat(BasicUtil.matches(null, Exception.class), is(false));
		assertThat(BasicUtil.matches(new IOException(), Exception.class), is(true));
		assertThat(BasicUtil.matches(new IOException(), RuntimeException.class), is(false));
		assertThat(BasicUtil.matches(new IOException(), String::isEmpty), is(false));
		assertThat(BasicUtil.matches(new Exception("test"), RuntimeException.class), is(false));
		assertThat(BasicUtil.matches(new Exception("test"), s -> s.startsWith("t")), is(true));
		assertThat(BasicUtil.matches(new Exception("Test"), s -> s.startsWith("t")), is(false));
	}

	@Test
	public void testCopyToClipboard() throws IOException, UnsupportedFlavorException {
		String s0 = "clipboard\ntest\n";
		BasicUtil.copyToClipBoard(s0);
		Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		Object obj = trans.getTransferData(trans.getTransferDataFlavors()[0]);
		assertThat(obj, is(s0));
		String s = BasicUtil.copyFromClipBoard();
		assertThat(s, is(s0));
	}

	@Test
	public void testDefaultValue() {
		assertNull(BasicUtil.defaultValue(null, null));
		assertThat(BasicUtil.defaultValue(null, 1), is(1));
		assertThat(BasicUtil.defaultValue(1, null), is(1));
		assertThat(BasicUtil.defaultValue(1, 2), is(1));
	}

	@Test
	public void testConditional() {
		assertNull(BasicUtil.conditional(null, "a", "b"));
		assertThat(BasicUtil.conditional(null, "a", "b", "c"), is("c"));
		assertThat(BasicUtil.conditional(Boolean.TRUE, "a", "b", "c"), is("a"));
		assertThat(BasicUtil.conditional(Boolean.FALSE, "a", "b", "c"), is("b"));
	}

	private static enum Enum {
		a,
		b,
		c;
	}

	@Test
	public void testValueOf() {
		assertThat(BasicUtil.valueOf(null, "a", Enum.a), is(Enum.a));
		assertThat(BasicUtil.valueOf(Enum.class, "a"), is(Enum.a));
		assertNull(BasicUtil.valueOf(Enum.class, "ab"));
		assertNull(BasicUtil.valueOf(Enum.class, null, null));
		assertThat(BasicUtil.valueOf(Enum.class, "b", null), is(Enum.b));
		assertThat(BasicUtil.valueOf(Enum.class, null, Enum.a), is(Enum.a));
		assertThat(BasicUtil.valueOf(Enum.class, "ab", Enum.c), is(Enum.c));
		assertNull(BasicUtil.valueOf(Enum.class, "ab", null));
	}

	@Test
	public void testDelay() throws InterruptedException {
		final BooleanCondition flag = BooleanCondition.create();
		Thread thread = new Thread(() -> {
			try {
				BasicUtil.delay(0);
				BasicUtil.delayMicros(0);
				BasicUtil.delayMicros(1);
				BasicUtil.delayMicros(1000);
				// BasicUtil.delay(1);
				flag.signal();
				BasicUtil.delay(10000);
				fail("RuntimeInterruptedException should be thrown");
			} catch (RuntimeInterruptedException e) {}
		});
		thread.start();
		flag.await();
		thread.interrupt();
		thread.join();
	}

	@Test
	public void testUncheckedCast() {
		Object[] array = new String[3];
		String[] castArray = BasicUtil.uncheckedCast(array);
		BasicUtil.unused((Object) castArray);
	}

	@Test
	public void testCastOrNull() {
		java.sql.Date sqlDate = new java.sql.Date(0);
		assertThat(BasicUtil.castOrNull(Date.class, sqlDate), is((Date) sqlDate));
		Date date = new Date(0);
		assertThat(BasicUtil.castOrNull(java.sql.Date.class, date), nullValue());
	}

	@Test
	public void testForceInit() {
		assertThat(ForceInitTestClassHelper.count, is(0));
		Class<ForceInitTestClass> cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertThat(ForceInitTestClassHelper.count, is(1));
		cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertNotNull(cls);
		assertThat(ForceInitTestClassHelper.count, is(1));
		assertException(() -> BasicUtil.load("", this.getClass().getClassLoader()));
	}

	@Test
	public void testInitCause() {
		IllegalStateException e1 = new IllegalStateException();
		IllegalArgumentException e2 = new IllegalArgumentException();
		IllegalStateException e = BasicUtil.initCause(e1, e2);
		assertThat(e.getCause(), is((Throwable) e2));
		BasicUtil.initCause(e1, null);
		assertThat(e1.getCause(), is((Throwable) e2));
	}

	@Test
	public void testStackTrace() {
		String stackTrace = BasicUtil.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertThat(lines[0], is("java.lang.Exception"));
		String fullClassName = getClass().getName();
		String className = getClass().getSimpleName();
		String methodName = ReflectUtil.currentMethodName();
		String s = String.format("at %s.%s(%s.java:", fullClassName, methodName, className);
		assertTrue(lines[1].trim().startsWith(s));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(BasicUtil.isEmpty((String) null));
		assertTrue(BasicUtil.isEmpty((String[]) null));
		assertTrue(BasicUtil.isEmpty(" \t\r\n"));
		assertTrue(BasicUtil.isEmpty((Map<?, ?>) null));
		assertTrue(BasicUtil.isEmpty(Collections.emptyMap()));
		assertFalse(BasicUtil.isEmpty(Collections.singletonMap(1, 2)));
		assertTrue(BasicUtil.isEmpty((Collection<?>) null));
		Collection<?> collection = new HashSet<>();
		assertTrue(BasicUtil.isEmpty(collection));
		collection.add(null);
		assertFalse(BasicUtil.isEmpty(collection));
		assertTrue(BasicUtil.isEmpty(new Object[] {}));
		assertFalse(BasicUtil.isEmpty(new Object[] { null }));
	}

}
