package ceri.common.reflect;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.isClass;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;

public class ReflectUtilTest {

	private static abstract class Abstract {
		@SuppressWarnings("unused")
		public Abstract() {}
	}

	private static class Error {
		@SuppressWarnings("unused")
		public Error() {
			throw new RuntimeException();
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ReflectUtil.class);
	}

	@Test(expected = CreateException.class)
	public void testCreateAbstractObject() throws CreateException {
		ReflectUtil.createObject(Abstract.class);
	}

	@Test
	public void testCreateErrorObject() {
		assertException(() -> ReflectUtil.createObject(Error.class));
	}

	@Test
	public void testCreateObject() throws CreateException {
		Class<?>[] argTypes = {};
		Object[] args = {};
		assertThat(ReflectUtil.createObject(String.class, argTypes, args), is(""));
		argTypes = new Class<?>[] { long.class };
		args = new Object[] { 0 };
		assertThat(ReflectUtil.createObject(Date.class, argTypes, args), is(new Date(0)));
	}

	@Test
	public void testInstanceOfAny() {
		assertFalse(ReflectUtil.instanceOfAny(null));
		assertFalse(ReflectUtil.instanceOfAny(Object.class));
		Object obj = Long.valueOf(Long.MAX_VALUE);
		assertTrue(ReflectUtil.instanceOfAny(obj, Long.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Number.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Object.class));
		assertFalse(ReflectUtil.instanceOfAny(obj, Float.class));
		assertFalse(ReflectUtil.instanceOfAny(obj, Float.class, Integer.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Float.class, Integer.class, Number.class));
	}

	@Test(expected = CreateException.class)
	public void testCreateObjectDefault() throws CreateException {
		assertThat(ReflectUtil.createObject(String.class), is(""));
		ReflectUtil.createObject(Boolean.class);
	}

	@Test
	public void testCurrentCaller() {
		Caller caller = ReflectUtil.currentCaller();
		Class<?> cls = getClass();
		assertThat(caller.cls, is(cls.getSimpleName()));
		assertThat(caller.fullCls, is(cls.getName()));
		assertThat(caller.file, is(cls.getSimpleName() + ".java"));
		assertThat(caller.cls(), isClass(cls));
		Caller caller2 = ReflectUtil.currentCaller();
		assertThat(caller, not(caller2));
		assertThat(new Caller(caller2.fullCls, caller.line, caller2.method, caller2.file),
			is(caller));
	}

	@Test
	public void testPreviousCaller() {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		assertThat(ReflectUtil.previousCaller(e.length - 2), not(Caller.NULL));
		assertThat(ReflectUtil.previousCaller(e.length - 1), is(Caller.NULL));
	}

	@Test
	public void testCurrentMethodName() {
		assertThat(ReflectUtil.currentMethodName(), is("testCurrentMethodName"));
	}

	@Test
	public void testPreviousMethodName() {
		callPreviousMethodName1();
	}

	private void callPreviousMethodName1() {
		assertThat(ReflectUtil.previousMethodName(1), is("testPreviousMethodName"));
		callPreviousMethodName2();
	}

	private void callPreviousMethodName2() {
		assertThat(ReflectUtil.previousMethodName(2), is("testPreviousMethodName"));
	}

}
