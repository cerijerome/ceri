package ceri.common.reflect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOptional;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Fluent;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect.ThreadElement;
import ceri.common.test.Captor;
import ceri.common.util.Basics;
import ceri.common.util.Counter;

public class ReflectTest {
	private static final Counter.OfInt counter = Counter.of(0);

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
		public static final String sfs = "sfs";
		public static String ss = null;
		public static final int sfi = -1;
		public static int si = 1;
	}

	public static final Class<?> E_CLASS = E.class;

	public enum E {
		a,
		b,
		c;
	}

	public static class Init {
		static {
			ReflectTest.counter.inc(1);
		}
	}

	public static class Nested {
		public static class A {
			public static class AA {}

			public static class AB {}
		}

		public static class B {}
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Reflect.class);
	}

	static class SuperA {}

	static class SuperB extends SuperA {}

	static class SuperC extends SuperB {}

	@Test
	public void testSuperclass() {
		SuperC[][] obj = new SuperC[0][];
		Class<?> cls = obj.getClass();
		assertSame(cls, SuperC[][].class);
		cls = Reflect.superClass(cls);
		assertSame(cls, SuperB[][].class);
		cls = Reflect.superClass(cls);
		assertSame(cls, SuperA[][].class);
		cls = Reflect.superClass(cls);
		assertSame(cls, Object[][].class);
		cls = Reflect.superClass(cls);
		assertSame(cls, Object[].class);
		cls = Reflect.superClass(cls);
		assertSame(cls, Object.class);
		cls = Reflect.superClass(cls);
		assertSame(cls, null);
	}

	@Test
	public void testThreadElement() {
		var te = new ThreadElement(Thread.currentThread(),
			new StackTraceElement("my.pkg.MyClass", "method", "File.java", 123));
		assertString(te, "MyClass.method:123");
		assertMatch(te.full(), "\\[.*\\] \\Qmy.pkg.MyClass.method(File.java:123)\\E");
		assertString(ThreadElement.NULL, "null");
		assertString(ThreadElement.NULL.full(), "[null] null");
	}

	@Test
	public void testFindElement() {
		assertEquals(Reflect.findElement(_ -> false), ThreadElement.NULL);
		var thread = Thread.currentThread();
		var name = getClass().getName();
		var te = Reflect.findElement((t, e) -> (thread == t) && e.getClassName().equals(name));
		assertEquals(te.thread(), thread);
		assertEquals(te.element().getClassName(), name);
	}

	@Test
	public void testGetClass() {
		assertEquals(Reflect.getClass(null), null);
		assertEquals(Reflect.getClass(String.class), Class.class);
		assertEquals(Reflect.getClass("abc"), String.class);
	}

	@Test
	public void testNested() {
		assertOrdered(Reflect.nested());
		assertOrdered(Reflect.nested(Nested.class), Nested.class, Nested.A.class, Nested.A.AA.class,
			Nested.A.AB.class, Nested.B.class);
		assertOrdered(Reflect.nested(Nested.A.class, Nested.B.class), Nested.A.class,
			Nested.A.AA.class, Nested.A.AB.class, Nested.B.class);
	}

	@Test
	public void testEnumToField() {
		assertEquals(Reflect.enumToField(null), null);
		Field f = Reflect.enumToField(E.a);
		assertEquals(f.isEnumConstant(), true);
		assertEquals(f.getName(), "a");
	}

	@Test
	public void testFieldToEnum() throws ReflectiveOperationException {
		assertEquals(Reflect.fieldToEnum(null), null);
		assertEquals(Reflect.fieldToEnum(Fields.class.getField("s")), null);
		var f = E.class.getField("a");
		var en = Reflect.fieldToEnum(f);
		assertEquals(en, E.a);
	}

	@Test
	public void testOptionalCast() {
		assertOptional(Reflect.castOptional(String.class, 1.1), null);
		assertOptional(Reflect.castOptional(Number.class, 1.1), 1.1);
	}

	@Test
	public void testIsStatic() {
		assertEquals(Reflect.isStatic(null), false);
		assertEquals(Reflect.isStatic(Reflect.publicField(Fields.class, "s")), false);
		assertEquals(Reflect.isStatic(Reflect.publicField(E.class, "a")), true);
	}

	@Test
	public void testPublicField() {
		assertNull(Reflect.publicField(Fields.class, "l"));
		assertNull(Reflect.publicField(Fields.class, "d"));
		assertNull(Reflect.publicField(Fields.class, "x"));
		assertNull(Reflect.publicField(Fields.class, null));
		assertNull(Reflect.publicField(null, "s"));
	}

	@Test
	public void testPublicFieldValue() {
		assertNull(Reflect.publicFieldValue(new Object(), Reflect.publicField(Fields.class, "s")));
		assertNull(Reflect.publicFieldValue(new Fields().apply(f -> f.l = 100),
			Reflect.publicField(Fields.class, "l")));
		assertNull(Reflect.publicFieldValue(new Fields().apply(f -> f.s = "test"), null));
		assertNull(Reflect.publicFieldValue(null, Reflect.publicField(Fields.class, "i")));
	}

	@Test
	public void testPublicValue() {
		assertEquals(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), "s"), "test");
		assertEquals(Reflect.publicValue(new Fields().apply(f -> f.i = 333), "i"), 333);
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3);
		assertEquals(Reflect.publicValue(new Fields().apply(f -> f.b = bytes), "b"), bytes);
		assertNull(Reflect.publicValue(new Fields().apply(f -> f.l = 100), "l"));
		assertNull(Reflect.publicValue(new Fields().apply(f -> f.d = 0.3), "d"));
		assertNull(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), "x"));
		assertNull(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), null));
		assertNull(Reflect.publicValue(null, "s"));
	}

	@Test
	public void testStaticFields() {
		Fields.ss = null;
		assertOrdered(Reflect.staticFields(Fields.class, String.class).toList(), "sfs");
		Fields.ss = "ss";
		assertOrdered(Reflect.staticFields(Fields.class, String.class).toList(), "sfs", "ss");
	}

	@Test
	public void testPackageLevels() {
		assertEquals(Reflect.packageLevels((Class<?>) null), 0);
		assertEquals(Reflect.packageLevels(getClass()), 3);
		assertEquals(Reflect.packageLevels((String) null), 0);
		assertEquals(Reflect.packageLevels(""), 0);
		assertEquals(Reflect.packageLevels("a"), 1);
		assertEquals(Reflect.packageLevels("a.b.c.d"), 4);
	}

	@Test
	public void testAbbreviatePackages() {
		assertNull(Reflect.abbreviatePackages(null));
		assertEquals(Reflect.abbreviatePackages(""), "");
		assertEquals(Reflect.abbreviatePackages("ceri.common.reflect.ReflectUtil"),
			"c.c.r.ReflectUtil");
		assertEquals(Reflect.abbreviatePackages("Name.abc.def.Xyz"), "Name.a.d.Xyz");
	}

	@Test
	public void testStackHasPackage() {
		assertFalse(Reflect.stackHasPackage((Class<?>) null));
		assertFalse(Reflect.stackHasPackage((String) null));
		assertTrue(Reflect.stackHasPackage(getClass()));
	}

	@Test
	public void testInit() {
		var cls = Init.class;
		assertEquals(counter.get(), 0);
		assertEquals(Reflect.init(null), null);
		assertEquals(Reflect.init(cls), cls);
		assertEquals(counter.get(), 1);
		assertEquals(Reflect.init(cls), cls);
		assertEquals(counter.get(), 1);
	}

	@Test
	public void testForName() {
		var cl = getClass().getClassLoader();
		assertSame(Reflect.forName("java.lang.String"), String.class);
		assertThrown(() -> Reflect.forName("___"));
		assertSame(Reflect.forName("java.lang.String", true, cl), String.class);
		assertThrown(() -> Reflect.forName("___", true, cl));
	}

	@Test
	public void testName() {
		assertEquals(Reflect.name((Class<?>) null), "null");
		assertEquals(Reflect.name((String) null), "null");
		assertEquals(Reflect.name(int.class), "int");
		assertEquals(Reflect.name(byte[].class), "byte[]");
		assertEquals(Reflect.name(Abstract.class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName());
		assertEquals(Reflect.name(Abstract[].class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testNestedName() {
		assertEquals(Reflect.nestedName(null), "null");
		assertEquals(Reflect.nestedName(int.class), "int");
		assertEquals(Reflect.nestedName(byte[].class), "byte[]");
		assertEquals(Reflect.nestedName(Abstract.class), Abstract.class.getSimpleName());
		assertEquals(Reflect.nestedName(Abstract[].class), Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testHashId() {
		assertNull(Reflect.hashId(null));
		assertMatch(Reflect.hashId(new Object()), "@[0-9a-fA-F]+");
	}

	@Test
	public void testSame() {
		assertEquals(Reflect.same(null, null), true);
		assertEquals(Reflect.same(E.class, E.class), true);
		assertEquals(Reflect.same(E.class, null), false);
		assertEquals(Reflect.same(null, E.class), false);
		class TestSame {
			static {
				assertEquals(E_CLASS.equals(E.class), false);
				assertEquals(Reflect.same(E_CLASS, E.class), true);
			}
		}
		ClassReInitializer.of(TestSame.class, E.class).reinit();
	}

	@Test
	public void testCreateWithError() {
		assertThrown(() -> Reflect.create(Abstract.class));
		assertThrown(() -> Reflect.create(Error.class));
	}

	@Test
	public void testCreateObject() throws RuntimeInvocationException {
		Class<?>[] argTypes = {};
		Object[] args = {};
		assertEquals(Reflect.create(String.class, argTypes, args), "");
		argTypes = new Class<?>[] { long.class };
		args = new Object[] { 0 };
		assertEquals(Reflect.create(Date.class, argTypes, args), new Date(0));
		assertEquals(Reflect.create(String.class, byte[].class, ArrayUtil.bytes.of(0, 0)), "\0\0");
	}

	@Test
	public void testInvokeMethodError() throws NoSuchMethodException {
		Method m = Error.class.getMethod("error", int.class);
		var err = new Error(null);
		assertThrown(RuntimeInvocationException.class, () -> Reflect.invoke(m, err));
		assertThrown(RuntimeInvocationException.class, () -> Reflect.invoke(m, err, 0));
		assertThrown(NullPointerException.class, () -> Reflect.invoke(m, null, 0));
	}

	@Test
	public void testInstanceOfAny() {
		assertFalse(Reflect.instanceOfAny(null));
		assertFalse(Reflect.instanceOfAny(Object.class));
		Object obj = Long.MAX_VALUE;
		assertTrue(Reflect.instanceOfAny(obj, Long.class));
		assertTrue(Reflect.instanceOfAny(obj, Number.class));
		assertTrue(Reflect.instanceOfAny(obj, Object.class));
		assertFalse(Reflect.instanceOfAny(obj, Float.class));
		assertFalse(Reflect.instanceOfAny(obj, Float.class, Integer.class));
		assertTrue(Reflect.instanceOfAny(obj, Float.class, Integer.class, Number.class));
	}

	@Test
	public void testAssignableFromAny() {
		assertFalse(Reflect.assignableFromAny(null));
		assertFalse(Reflect.assignableFromAny(Object.class));
		assertTrue(Reflect.assignableFromAny(Long.class, Long.class));
		assertFalse(Reflect.assignableFromAny(Long.class, Integer.class));
		assertTrue(Reflect.assignableFromAny(Long.class, Number.class));
		assertFalse(Reflect.assignableFromAny(Number.class, Long.class));
		assertTrue(Reflect.assignableFromAny(Number.class, Long.class, Serializable.class));
		assertFalse(Reflect.assignableFromAny(Serializable.class, Number.class, Long.class));
	}

	@Test
	public void testCreateObjectDefault() {
		assertEquals(Reflect.create(String.class), "");
		assertEquals(Reflect.create(Boolean.class), null);
	}

	@Test
	public void testJvmArgs() {
		Pattern p = Pattern.compile("-.+");
		for (var arg : Reflect.jvmArgs())
			assertMatch(arg, p);
	}

	@Test
	public void testCurrentStackTraceElement() {
		StackTraceElement element = Reflect.currentElement();
		assertEquals(element.getMethodName(), "testCurrentStackTraceElement");
		assertEquals(element.getClassName(), ReflectTest.class.getName());
	}

	@Test
	public void testPreviousStackTraceElement() {
		StackTraceElement element = getPreviousStackTraceElement(0);
		assertEquals(element.getMethodName(), "getPreviousStackTraceElement");
		assertEquals(element.getClassName(), ReflectTest.class.getName());
		element = getPreviousStackTraceElement(1);
		assertEquals(element.getMethodName(), "testPreviousStackTraceElement");
		assertEquals(element.getClassName(), ReflectTest.class.getName());
	}

	private StackTraceElement getPreviousStackTraceElement(int countBack) {
		return Reflect.previousElement(countBack);
	}

	@Test
	public void testCurrentCaller() {
		Caller caller = Reflect.currentCaller();
		Class<?> cls = getClass();
		assertEquals(caller.cls, cls.getSimpleName());
		assertEquals(caller.fullCls, cls.getName());
		assertEquals(caller.file, cls.getSimpleName() + ".java");
		assertSame(caller.cls(), cls);
		Caller caller2 = Reflect.currentCaller();
		assertNotEquals(caller, caller2);
		assertEquals(new Caller(caller2.fullCls, caller.line, caller2.method, caller2.file),
			caller);
	}

	@Test
	public void testCurrentClassLine() {
		assertMatch(Reflect.currentClassLine(), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
	}

	@Test
	public void testPreviousClassLine() {
		assertMatch(getPreviousClassLine(0), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
		assertMatch(getPreviousClassLine(1), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
	}

	private String getPreviousClassLine(int countBack) {
		return Reflect.previousClassLine(countBack);
	}

	@Test
	public void testPreviousCaller() {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		assertNotEquals(Reflect.previousCaller(e.length - 2), Caller.NULL);
		assertEquals(Reflect.previousCaller(e.length - 1), Caller.NULL);
	}

	@Test
	public void testCurrentMethodName() {
		assertEquals(Reflect.currentMethodName(), "testCurrentMethodName");
	}

	@Test
	public void testPreviousMethodName() {
		callPreviousMethodName1();
	}

	private void callPreviousMethodName1() {
		assertEquals(Reflect.previousMethodName(1), "testPreviousMethodName");
		callPreviousMethodName2();
	}

	private void callPreviousMethodName2() {
		assertEquals(Reflect.previousMethodName(2), "testPreviousMethodName");
	}

	@Test
	public void testInterceptor() throws ReflectiveOperationException {
		var delegate = Captor.ofInt();
		var captor = Captor.<Method, Object[]>ofBi();
		var method = Functions.IntConsumer.class.getMethod("accept", int.class);
		Reflect.<Functions.IntConsumer>interceptor(delegate, captor).accept(3);
		Reflect.interceptor(Functions.IntConsumer.class, delegate, captor).accept(-1);
		delegate.verify(3, -1);
		captor.first.verify(method, method);
		captor.second.verify(new Object[] { 3 }, new Object[] { -1 });
	}

	@Test
	public void testUnchecked() {
		Object[] array = new String[3];
		String[] castArray = Reflect.unchecked(array);
		Basics.unused((Object) castArray);
	}

	@Test
	public void testCastOrNull() {
		java.sql.Date sqlDate = new java.sql.Date(0);
		assertEquals(Reflect.castOrNull(Date.class, sqlDate), sqlDate);
		Date date = new Date(0);
		assertNull(Reflect.castOrNull(java.sql.Date.class, date));
	}
}
