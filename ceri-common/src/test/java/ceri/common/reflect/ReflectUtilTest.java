package ceri.common.reflect;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.function.Fluent;
import ceri.common.reflect.ReflectUtil.ThreadElement;
import ceri.common.test.Captor;

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

		@SuppressWarnings("unused")
		public Error(Object ok) {}

		@SuppressWarnings("unused")
		public String error(int i) {
			throw new RuntimeException();
		}
	}

	@SuppressWarnings("unused")
	private static class Fields implements Fluent<Fields> {
		public String s;
		public int i;
		public byte[] b;
		protected long l;
		private double d;
	}

	private enum E {
		a,
		b,
		c;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ReflectUtil.class);
	}

	@Test
	public void testThreadElement() {
		var te = new ThreadElement(Thread.currentThread(),
			new StackTraceElement("my.pkg.MyClass", "method", "File.java", 123));
		assertFind(te, "\\[.*\\] \\Qmy.pkg.MyClass.method(File.java:123)\\E");
	}

	@Test
	public void testFindElement() {
		assertEquals(ReflectUtil.findElement(e -> false), ThreadElement.NULL);
		var thread = Thread.currentThread();
		var name = getClass().getName();
		var te = ReflectUtil.findElement((t, e) -> (thread == t) && e.getClassName().equals(name));
		assertEquals(te.thread(), thread);
		assertEquals(te.element().getClassName(), name);
	}

	@Test
	public void testGetClass() {
		assertEquals(ReflectUtil.getClass(null), null);
		assertEquals(ReflectUtil.getClass(String.class), Class.class);
		assertEquals(ReflectUtil.getClass("abc"), String.class);
	}

	@Test
	public void testEnumField() {
		assertEquals(ReflectUtil.enumField(null), null);
		Field f = ReflectUtil.enumField(E.a);
		assertEquals(f.isEnumConstant(), true);
		assertEquals(f.getName(), "a");
	}

	@Test
	public void testIsStatic() {
		assertEquals(ReflectUtil.isStatic(null), false);
		assertEquals(ReflectUtil.isStatic(ReflectUtil.publicField(Fields.class, "s")), false);
		assertEquals(ReflectUtil.isStatic(ReflectUtil.publicField(E.class, "a")), true);
	}

	@Test
	public void testPublicField() {
		assertNull(ReflectUtil.publicField(Fields.class, "l"));
		assertNull(ReflectUtil.publicField(Fields.class, "d"));
		assertNull(ReflectUtil.publicField(Fields.class, "x"));
		assertNull(ReflectUtil.publicField(Fields.class, null));
		assertNull(ReflectUtil.publicField(null, "s"));
	}

	@Test
	public void testPublicFieldValue() {
		assertNull(
			ReflectUtil.publicFieldValue(new Object(), ReflectUtil.publicField(Fields.class, "s")));
		assertNull(ReflectUtil.publicFieldValue(new Fields().apply(f -> f.l = 100),
			ReflectUtil.publicField(Fields.class, "l")));
		assertNull(ReflectUtil.publicFieldValue(new Fields().apply(f -> f.s = "test"), null));
		assertNull(ReflectUtil.publicFieldValue(null, ReflectUtil.publicField(Fields.class, "i")));
	}

	@Test
	public void testPublicValue() {
		assertEquals(ReflectUtil.publicValue(new Fields().apply(f -> f.s = "test"), "s"), "test");
		assertEquals(ReflectUtil.publicValue(new Fields().apply(f -> f.i = 333), "i"), 333);
		byte[] bytes = bytes(1, 2, 3);
		assertEquals(ReflectUtil.publicValue(new Fields().apply(f -> f.b = bytes), "b"), bytes);
		assertNull(ReflectUtil.publicValue(new Fields().apply(f -> f.l = 100), "l"));
		assertNull(ReflectUtil.publicValue(new Fields().apply(f -> f.d = 0.3), "d"));
		assertNull(ReflectUtil.publicValue(new Fields().apply(f -> f.s = "test"), "x"));
		assertNull(ReflectUtil.publicValue(new Fields().apply(f -> f.s = "test"), null));
		assertNull(ReflectUtil.publicValue(null, "s"));
	}

	@Test
	public void testPackageLevels() {
		assertEquals(ReflectUtil.packageLevels((Class<?>) null), 0);
		assertEquals(ReflectUtil.packageLevels(getClass()), 3);
		assertEquals(ReflectUtil.packageLevels((String) null), 0);
		assertEquals(ReflectUtil.packageLevels(""), 0);
		assertEquals(ReflectUtil.packageLevels("a"), 1);
		assertEquals(ReflectUtil.packageLevels("a.b.c.d"), 4);
	}

	@Test
	public void testAbbreviatePackages() {
		assertNull(ReflectUtil.abbreviatePackages(null));
		assertEquals(ReflectUtil.abbreviatePackages(""), "");
		assertEquals(ReflectUtil.abbreviatePackages("ceri.common.reflect.ReflectUtil"),
			"c.c.r.ReflectUtil");
		assertEquals(ReflectUtil.abbreviatePackages("Name.abc.def.Xyz"), "Name.a.d.Xyz");
	}

	@Test
	public void testStackHasPackage() {
		assertFalse(ReflectUtil.stackHasPackage((Class<?>) null));
		assertFalse(ReflectUtil.stackHasPackage((String) null));
		assertTrue(ReflectUtil.stackHasPackage(getClass()));
	}

	@Test
	public void testForName() {
		assertSame(ReflectUtil.forName("java.lang.String"), String.class);
		assertThrown(() -> ReflectUtil.forName("___"));
	}

	@Test
	public void testName() {
		assertEquals(ReflectUtil.name(null), "null");
		assertEquals(ReflectUtil.name(int.class), "int");
		assertEquals(ReflectUtil.name(byte[].class), "byte[]");
		assertEquals(ReflectUtil.name(Abstract.class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName());
		assertEquals(ReflectUtil.name(Abstract[].class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testNestedName() {
		assertEquals(ReflectUtil.nestedName(null), "null");
		assertEquals(ReflectUtil.nestedName(int.class), "int");
		assertEquals(ReflectUtil.nestedName(byte[].class), "byte[]");
		assertEquals(ReflectUtil.nestedName(Abstract.class), Abstract.class.getSimpleName());
		assertEquals(ReflectUtil.nestedName(Abstract[].class),
			Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testToStringOrHashId() {
		assertNull(ReflectUtil.toStringOrHash(null));
		assertEquals(ReflectUtil.toStringOrHash(new Object() {
			@Override
			public int hashCode() {
				return 0xabcdef;
			}
		}), "@abcdef");
		assertEquals(ReflectUtil.toStringOrHash(new Object() {
			@Override
			public String toString() {
				return "test";
			}
		}), "test");
		assertEquals(ReflectUtil.toStringOrHash(new Object() {
			@Override
			public String toString() {
				return "test@";
			}
		}), "test@");
	}

	@Test
	public void testHashId() {
		assertNull(ReflectUtil.hashId(null));
		assertMatch(ReflectUtil.hashId(new Object()), "@[0-9a-fA-F]+");
	}

	@Test(expected = RuntimeInvocationException.class)
	public void testCreateAbstractObject() throws RuntimeInvocationException {
		ReflectUtil.create(Abstract.class);
	}

	@Test
	public void testCreateErrorObject() {
		assertThrown(() -> ReflectUtil.create(Error.class));
	}

	@Test
	public void testCreateObject() throws RuntimeInvocationException {
		Class<?>[] argTypes = {};
		Object[] args = {};
		assertEquals(ReflectUtil.create(String.class, argTypes, args), "");
		argTypes = new Class<?>[] { long.class };
		args = new Object[] { 0 };
		assertEquals(ReflectUtil.create(Date.class, argTypes, args), new Date(0));
	}

	@Test
	public void testInvokeMethodError() throws NoSuchMethodException {
		Method m = Error.class.getMethod("error", int.class);
		var err = new Error(null);
		assertThrown(RuntimeInvocationException.class, () -> ReflectUtil.invoke(m, err));
		assertThrown(RuntimeInvocationException.class, () -> ReflectUtil.invoke(m, err, 0));
		assertThrown(NullPointerException.class, () -> ReflectUtil.invoke(m, null, 0));
	}

	@Test
	public void testInstanceOfAny() {
		assertFalse(ReflectUtil.instanceOfAny(null));
		assertFalse(ReflectUtil.instanceOfAny(Object.class));
		Object obj = Long.MAX_VALUE;
		assertTrue(ReflectUtil.instanceOfAny(obj, Long.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Number.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Object.class));
		assertFalse(ReflectUtil.instanceOfAny(obj, Float.class));
		assertFalse(ReflectUtil.instanceOfAny(obj, Float.class, Integer.class));
		assertTrue(ReflectUtil.instanceOfAny(obj, Float.class, Integer.class, Number.class));
	}

	@Test
	public void testAssignableFromAny() {
		assertFalse(ReflectUtil.assignableFromAny(null));
		assertFalse(ReflectUtil.assignableFromAny(Object.class));
		assertTrue(ReflectUtil.assignableFromAny(Long.class, Long.class));
		assertFalse(ReflectUtil.assignableFromAny(Long.class, Integer.class));
		assertTrue(ReflectUtil.assignableFromAny(Long.class, Number.class));
		assertFalse(ReflectUtil.assignableFromAny(Number.class, Long.class));
		assertTrue(ReflectUtil.assignableFromAny(Number.class, Long.class, Serializable.class));
		assertFalse(ReflectUtil.assignableFromAny(Serializable.class, Number.class, Long.class));
	}

	@Test(expected = RuntimeInvocationException.class)
	public void testCreateObjectDefault() throws RuntimeInvocationException {
		assertEquals(ReflectUtil.create(String.class), "");
		ReflectUtil.create(Boolean.class);
	}

	@Test
	public void testJvmArgs() {
		Pattern p = Pattern.compile("-.+");
		for (var arg : ReflectUtil.jvmArgs())
			assertMatch(arg, p);
	}

	@Test
	public void testCurrentStackTraceElement() {
		StackTraceElement element = ReflectUtil.currentStackTraceElement();
		assertEquals(element.getMethodName(), "testCurrentStackTraceElement");
		assertEquals(element.getClassName(), ReflectUtilTest.class.getName());
	}

	@Test
	public void testPreviousStackTraceElement() {
		StackTraceElement element = getPreviousStackTraceElement(0);
		assertEquals(element.getMethodName(), "getPreviousStackTraceElement");
		assertEquals(element.getClassName(), ReflectUtilTest.class.getName());
		element = getPreviousStackTraceElement(1);
		assertEquals(element.getMethodName(), "testPreviousStackTraceElement");
		assertEquals(element.getClassName(), ReflectUtilTest.class.getName());
	}

	private StackTraceElement getPreviousStackTraceElement(int countBack) {
		return ReflectUtil.previousStackTraceElement(countBack);
	}

	@Test
	public void testCurrentCaller() {
		Caller caller = ReflectUtil.currentCaller();
		Class<?> cls = getClass();
		assertEquals(caller.cls, cls.getSimpleName());
		assertEquals(caller.fullCls, cls.getName());
		assertEquals(caller.file, cls.getSimpleName() + ".java");
		assertSame(caller.cls(), cls);
		Caller caller2 = ReflectUtil.currentCaller();
		assertNotEquals(caller, caller2);
		assertEquals(new Caller(caller2.fullCls, caller.line, caller2.method, caller2.file),
			caller);
	}

	@Test
	public void testCurrentClassLine() {
		assertMatch(ReflectUtil.currentClassLine(),
			"\\Q" + ReflectUtilTest.class.getName() + "\\E:\\d+");
	}

	@Test
	public void testPreviousClassLine() {
		assertMatch(getPreviousClassLine(0), "\\Q" + ReflectUtilTest.class.getName() + "\\E:\\d+");
		assertMatch(getPreviousClassLine(1), "\\Q" + ReflectUtilTest.class.getName() + "\\E:\\d+");
	}

	private String getPreviousClassLine(int countBack) {
		return ReflectUtil.previousClassLine(countBack);
	}

	@Test
	public void testPreviousCaller() {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		assertNotEquals(ReflectUtil.previousCaller(e.length - 2), Caller.NULL);
		assertEquals(ReflectUtil.previousCaller(e.length - 1), Caller.NULL);
	}

	@Test
	public void testCurrentMethodName() {
		assertEquals(ReflectUtil.currentMethodName(), "testCurrentMethodName");
	}

	@Test
	public void testPreviousMethodName() {
		callPreviousMethodName1();
	}

	private void callPreviousMethodName1() {
		assertEquals(ReflectUtil.previousMethodName(1), "testPreviousMethodName");
		callPreviousMethodName2();
	}

	private void callPreviousMethodName2() {
		assertEquals(ReflectUtil.previousMethodName(2), "testPreviousMethodName");
	}

	@Test
	public void testInterceptor() throws ReflectiveOperationException {
		var delegate = Captor.ofInt();
		var captor = Captor.<Method, Object[]>ofBi();
		var method = IntConsumer.class.getMethod("accept", int.class);
		ReflectUtil.<IntConsumer>interceptor(delegate, captor).accept(3);
		ReflectUtil.interceptor(IntConsumer.class, delegate, captor).accept(-1);
		delegate.verify(3, -1);
		captor.first.verify(method, method);
		captor.second.verify(new Object[] { 3 }, new Object[] { -1 });
	}

}
