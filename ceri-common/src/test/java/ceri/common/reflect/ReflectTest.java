package ceri.common.reflect;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.function.Fluent;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect.ThreadElement;
import ceri.common.test.Assert;
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
		Assert.privateConstructor(Reflect.class);
	}

	static class SuperA {}

	static class SuperB extends SuperA {}

	static class SuperC extends SuperB {}

	@Test
	public void testSuperclass() {
		SuperC[][] obj = new SuperC[0][];
		Class<?> cls = obj.getClass();
		Assert.same(cls, SuperC[][].class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, SuperB[][].class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, SuperA[][].class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, Object[][].class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, Object[].class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, Object.class);
		cls = Reflect.superClass(cls);
		Assert.same(cls, null);
	}

	@Test
	public void testThreadElement() {
		var te = new ThreadElement(Thread.currentThread(),
			new StackTraceElement("my.pkg.MyClass", "method", "File.java", 123));
		Assert.string(te, "MyClass.method:123");
		Assert.match(te.full(), "\\[.*\\] \\Qmy.pkg.MyClass.method(File.java:123)\\E");
		Assert.string(ThreadElement.NULL, "null");
		Assert.string(ThreadElement.NULL.full(), "[null] null");
	}

	@Test
	public void testFindElement() {
		Assert.equal(Reflect.findElement(_ -> false), ThreadElement.NULL);
		var thread = Thread.currentThread();
		var name = getClass().getName();
		var te = Reflect.findElement((t, e) -> (thread == t) && e.getClassName().equals(name));
		Assert.equal(te.thread(), thread);
		Assert.equal(te.element().getClassName(), name);
	}

	@Test
	public void testGetClass() {
		Assert.equal(Reflect.getClass(null), null);
		Assert.equal(Reflect.getClass(String.class), Class.class);
		Assert.equal(Reflect.getClass("abc"), String.class);
	}

	@Test
	public void testNested() {
		Assert.ordered(Reflect.nested());
		Assert.ordered(Reflect.nested(Nested.class), Nested.class, Nested.A.class, Nested.A.AA.class,
			Nested.A.AB.class, Nested.B.class);
		Assert.ordered(Reflect.nested(Nested.A.class, Nested.B.class), Nested.A.class,
			Nested.A.AA.class, Nested.A.AB.class, Nested.B.class);
	}

	@Test
	public void testEnumToField() {
		Assert.equal(Reflect.enumToField(null), null);
		Field f = Reflect.enumToField(E.a);
		Assert.equal(f.isEnumConstant(), true);
		Assert.equal(f.getName(), "a");
	}

	@Test
	public void testFieldToEnum() throws ReflectiveOperationException {
		Assert.equal(Reflect.fieldToEnum(null), null);
		Assert.equal(Reflect.fieldToEnum(Fields.class.getField("s")), null);
		var f = E.class.getField("a");
		var en = Reflect.fieldToEnum(f);
		Assert.equal(en, E.a);
	}

	@Test
	public void testOptionalCast() {
		Assert.optional(Reflect.castOptional(String.class, 1.1), null);
		Assert.optional(Reflect.castOptional(Number.class, 1.1), 1.1);
	}

	@Test
	public void testIsStaticMember() {
		Assert.equal(Reflect.isStatic((Member) null), false);
		Assert.equal(Reflect.isStatic(Reflect.publicField(Fields.class, "s")), false);
		Assert.equal(Reflect.isStatic(Reflect.publicField(E.class, "a")), true);
	}

	@Test
	public void testIsStaticClass() {
		Assert.equal(Reflect.isStatic((Class<?>) null), false);
		Assert.equal(Reflect.isStatic(String.class), false);
		Assert.equal(Reflect.isStatic(E_CLASS), true);
	}

	@Test
	public void testIsPublicClass() {
		Assert.equal(Reflect.isPublic((Class<?>) null), false);
		Assert.equal(Reflect.isPublic(String.class), true);
		Assert.equal(Reflect.isPublic(Fields.class), false);
	}

	@Test
	public void testPublicField() {
		Assert.isNull(Reflect.publicField(Fields.class, "l"));
		Assert.isNull(Reflect.publicField(Fields.class, "d"));
		Assert.isNull(Reflect.publicField(Fields.class, "x"));
		Assert.isNull(Reflect.publicField(Fields.class, null));
		Assert.isNull(Reflect.publicField(null, "s"));
	}

	@Test
	public void testPublicFieldValue() {
		Assert
			.isNull(Reflect.publicFieldValue(new Object(), Reflect.publicField(Fields.class, "s")));
		Assert.isNull(Reflect.publicFieldValue(new Fields().apply(f -> f.l = 100),
			Reflect.publicField(Fields.class, "l")));
		Assert.isNull(Reflect.publicFieldValue(new Fields().apply(f -> f.s = "test"), null));
		Assert.isNull(Reflect.publicFieldValue(null, Reflect.publicField(Fields.class, "i")));
	}

	@Test
	public void testPublicValue() {
		Assert.equal(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), "s"), "test");
		Assert.equal(Reflect.publicValue(new Fields().apply(f -> f.i = 333), "i"), 333);
		byte[] bytes = Array.bytes.of(1, 2, 3);
		Assert.equal(Reflect.publicValue(new Fields().apply(f -> f.b = bytes), "b"), bytes);
		Assert.isNull(Reflect.publicValue(new Fields().apply(f -> f.l = 100), "l"));
		Assert.isNull(Reflect.publicValue(new Fields().apply(f -> f.d = 0.3), "d"));
		Assert.isNull(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), "x"));
		Assert.isNull(Reflect.publicValue(new Fields().apply(f -> f.s = "test"), null));
		Assert.isNull(Reflect.publicValue(null, "s"));
	}

	@Test
	public void testStaticFields() {
		Fields.ss = null;
		Assert.ordered(Reflect.staticFields(Fields.class, String.class).toList(), "sfs");
		Fields.ss = "ss";
		Assert.ordered(Reflect.staticFields(Fields.class, String.class).toList(), "sfs", "ss");
	}

	@Test
	public void testPackageLevels() {
		Assert.equal(Reflect.packageLevels((Class<?>) null), 0);
		Assert.equal(Reflect.packageLevels(getClass()), 3);
		Assert.equal(Reflect.packageLevels((String) null), 0);
		Assert.equal(Reflect.packageLevels(""), 0);
		Assert.equal(Reflect.packageLevels("a"), 1);
		Assert.equal(Reflect.packageLevels("a.b.c.d"), 4);
	}

	@Test
	public void testAbbreviatePackages() {
		Assert.isNull(Reflect.abbreviatePackages(null));
		Assert.equal(Reflect.abbreviatePackages(""), "");
		Assert.equal(Reflect.abbreviatePackages("ceri.common.reflect.ReflectUtil"),
			"c.c.r.ReflectUtil");
		Assert.equal(Reflect.abbreviatePackages("Name.abc.def.Xyz"), "Name.a.d.Xyz");
	}

	@Test
	public void testStackHasPackage() {
		Assert.no(Reflect.stackHasPackage((Class<?>) null));
		Assert.no(Reflect.stackHasPackage((String) null));
		Assert.yes(Reflect.stackHasPackage(getClass()));
	}

	@Test
	public void testInit() {
		var cls = Init.class;
		Assert.equal(counter.get(), 0);
		Assert.equal(Reflect.init(null), null);
		Assert.equal(Reflect.init(cls), cls);
		Assert.equal(counter.get(), 1);
		Assert.equal(Reflect.init(cls), cls);
		Assert.equal(counter.get(), 1);
	}

	@Test
	public void testForName() {
		var cl = getClass().getClassLoader();
		Assert.same(Reflect.forName("java.lang.String"), String.class);
		Assert.thrown(() -> Reflect.forName("___"));
		Assert.same(Reflect.forName("java.lang.String", true, cl), String.class);
		Assert.thrown(() -> Reflect.forName("___", true, cl));
	}

	@Test
	public void testName() {
		Assert.equal(Reflect.name((Class<?>) null), "null");
		Assert.equal(Reflect.name((String) null), "null");
		Assert.equal(Reflect.name(int.class), "int");
		Assert.equal(Reflect.name(byte[].class), "byte[]");
		Assert.equal(Reflect.name(Abstract.class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName());
		Assert.equal(Reflect.name(Abstract[].class),
			getClass().getSimpleName() + "." + Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testNestedName() {
		Assert.equal(Reflect.nestedName(null), "null");
		Assert.equal(Reflect.nestedName(int.class), "int");
		Assert.equal(Reflect.nestedName(byte[].class), "byte[]");
		Assert.equal(Reflect.nestedName(Abstract.class), Abstract.class.getSimpleName());
		Assert.equal(Reflect.nestedName(Abstract[].class), Abstract.class.getSimpleName() + "[]");
	}

	@Test
	public void testHashId() {
		Assert.isNull(Reflect.hashId(null));
		Assert.match(Reflect.hashId(new Object()), "@[0-9a-fA-F]+");
	}

	@Test
	public void testSameClass() {
		Assert.equal(Reflect.same((Class<?>) null, null), true);
		Assert.equal(Reflect.same(null, E.class), false);
		Assert.equal(Reflect.same(E.class, null), false);
		Assert.equal(Reflect.same(E.class, E.class), true);
		class TestSame {
			static {
				Assert.equal(E_CLASS.equals(E.class), false);
				Assert.equal(Reflect.same(E_CLASS, E.class), true);
			}
		}
		ClassReInitializer.of(TestSame.class, E.class).reinit();
	}

	@Test
	public void testSameRef() {
		var o0 = new Object();
		var o1 = new Object();
		var wrn = new WeakReference<>(null);
		var wr0 = new WeakReference<>(o0);
		var wr1 = new WeakReference<>(o1);
		var srn = new SoftReference<>(null);
		var sr0 = new SoftReference<>(o0);
		Assert.equal(Reflect.same((Reference<?>) null, null), true);
		Assert.equal(Reflect.same(null, wrn), false);
		Assert.equal(Reflect.same(wrn, null), false);
		Assert.equal(Reflect.same(wrn, srn), true);
		Assert.equal(Reflect.same(wr0, wrn), false);
		Assert.equal(Reflect.same(wr0, wr1), false);
		Assert.equal(Reflect.same(wr0, wr0), true);
		Assert.equal(Reflect.same(wr0, sr0), true);
	}

	@Test
	public void testCreateWithError() {
		Assert.thrown(() -> Reflect.create(Abstract.class));
		Assert.thrown(() -> Reflect.create(Error.class));
	}

	@Test
	public void testCreateObject() throws RuntimeInvocationException {
		Class<?>[] argTypes = {};
		Object[] args = {};
		Assert.equal(Reflect.create(String.class, argTypes, args), "");
		argTypes = new Class<?>[] { long.class };
		args = new Object[] { 0 };
		Assert.equal(Reflect.create(Date.class, argTypes, args), new Date(0));
		Assert.equal(Reflect.create(String.class, byte[].class, Array.bytes.of(0, 0)), "\0\0");
	}

	@Test
	public void testInvokeMethodError() throws NoSuchMethodException {
		Method m = Error.class.getMethod("error", int.class);
		var err = new Error(null);
		Assert.thrown(RuntimeInvocationException.class, () -> Reflect.invoke(m, err));
		Assert.thrown(RuntimeInvocationException.class, () -> Reflect.invoke(m, err, 0));
		Assert.thrown(NullPointerException.class, () -> Reflect.invoke(m, null, 0));
	}

	@Test
	public void testInstanceOfAny() {
		Assert.no(Reflect.instanceOfAny(null));
		Assert.no(Reflect.instanceOfAny(Object.class));
		Object obj = Long.MAX_VALUE;
		Assert.yes(Reflect.instanceOfAny(obj, Long.class));
		Assert.yes(Reflect.instanceOfAny(obj, Number.class));
		Assert.yes(Reflect.instanceOfAny(obj, Object.class));
		Assert.no(Reflect.instanceOfAny(obj, Float.class));
		Assert.no(Reflect.instanceOfAny(obj, Float.class, Integer.class));
		Assert.yes(Reflect.instanceOfAny(obj, Float.class, Integer.class, Number.class));
	}

	@Test
	public void testAssignableFromAny() {
		Assert.no(Reflect.assignableFromAny(null));
		Assert.no(Reflect.assignableFromAny(Object.class));
		Assert.yes(Reflect.assignableFromAny(Long.class, Long.class));
		Assert.no(Reflect.assignableFromAny(Long.class, Integer.class));
		Assert.yes(Reflect.assignableFromAny(Long.class, Number.class));
		Assert.no(Reflect.assignableFromAny(Number.class, Long.class));
		Assert.yes(Reflect.assignableFromAny(Number.class, Long.class, Serializable.class));
		Assert.no(Reflect.assignableFromAny(Serializable.class, Number.class, Long.class));
	}

	@Test
	public void testIsPrimitive() {
		Assert.equal(Reflect.isPrimitive(null), false);
		Assert.equal(Reflect.isPrimitive(boolean.class), true);
		Assert.equal(Reflect.isPrimitive(char.class), true);
		Assert.equal(Reflect.isPrimitive(Integer.class), false);
	}

	@Test
	public void testIsPrimitiveInt() {
		Assert.equal(Reflect.isPrimitiveInt(null), false);
		Assert.equal(Reflect.isPrimitiveInt(char.class), false);
		Assert.equal(Reflect.isPrimitiveInt(long.class), true);
		Assert.equal(Reflect.isPrimitiveInt(Integer.class), false);
	}

	@Test
	public void testIsPrimitiveNumber() {
		Assert.equal(Reflect.isPrimitiveNumber(null), false);
		Assert.equal(Reflect.isPrimitiveNumber(char.class), false);
		Assert.equal(Reflect.isPrimitiveNumber(double.class), true);
		Assert.equal(Reflect.isPrimitiveNumber(Integer.class), false);
	}

	@Test
	public void testIsNumber() {
		Assert.equal(Reflect.isNumber(null), false);
		Assert.equal(Reflect.isNumber(char.class), false);
		Assert.equal(Reflect.isNumber(double.class), true);
		Assert.equal(Reflect.isNumber(Integer.class), true);
		Assert.equal(Reflect.isNumber(Float.class), true);
		Assert.equal(Reflect.isNumber(String.class), false);
	}

	@Test
	public void testCreateObjectDefault() {
		Assert.equal(Reflect.create(String.class), "");
		Assert.equal(Reflect.create(Boolean.class), null);
	}

	@Test
	public void testJvmArgs() {
		Pattern p = Pattern.compile("-.+");
		for (var arg : Reflect.jvmArgs())
			Assert.match(arg, p);
	}

	@Test
	public void testCurrentStackTraceElement() {
		StackTraceElement element = Reflect.currentElement();
		Assert.equal(element.getMethodName(), "testCurrentStackTraceElement");
		Assert.equal(element.getClassName(), ReflectTest.class.getName());
	}

	@Test
	public void testPreviousStackTraceElement() {
		StackTraceElement element = getPreviousStackTraceElement(0);
		Assert.equal(element.getMethodName(), "getPreviousStackTraceElement");
		Assert.equal(element.getClassName(), ReflectTest.class.getName());
		element = getPreviousStackTraceElement(1);
		Assert.equal(element.getMethodName(), "testPreviousStackTraceElement");
		Assert.equal(element.getClassName(), ReflectTest.class.getName());
	}

	private StackTraceElement getPreviousStackTraceElement(int countBack) {
		return Reflect.previousElement(countBack);
	}

	@Test
	public void testCurrentCaller() {
		Caller caller = Reflect.currentCaller();
		Class<?> cls = getClass();
		Assert.equal(caller.cls, cls.getSimpleName());
		Assert.equal(caller.fullCls, cls.getName());
		Assert.equal(caller.file, cls.getSimpleName() + ".java");
		Assert.same(caller.cls(), cls);
		Caller caller2 = Reflect.currentCaller();
		Assert.notEqual(caller, caller2);
		Assert.equal(new Caller(caller2.fullCls, caller.line, caller2.method, caller2.file),
			caller);
	}

	@Test
	public void testCurrentClassLine() {
		Assert.match(Reflect.currentClassLine(), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
	}

	@Test
	public void testPreviousClassLine() {
		Assert.match(getPreviousClassLine(0), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
		Assert.match(getPreviousClassLine(1), "\\Q" + ReflectTest.class.getName() + "\\E:\\d+");
	}

	private String getPreviousClassLine(int countBack) {
		return Reflect.previousClassLine(countBack);
	}

	@Test
	public void testPreviousCaller() {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		Assert.notEqual(Reflect.previousCaller(e.length - 2), Caller.NULL);
		Assert.equal(Reflect.previousCaller(e.length - 1), Caller.NULL);
	}

	@Test
	public void testCurrentMethodName() {
		Assert.equal(Reflect.currentMethodName(), "testCurrentMethodName");
	}

	@Test
	public void testPreviousMethodName() {
		callPreviousMethodName1();
	}

	private void callPreviousMethodName1() {
		Assert.equal(Reflect.previousMethodName(1), "testPreviousMethodName");
		callPreviousMethodName2();
	}

	private void callPreviousMethodName2() {
		Assert.equal(Reflect.previousMethodName(2), "testPreviousMethodName");
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
		Assert.equal(Reflect.castOrNull(Date.class, sqlDate), sqlDate);
		Date date = new Date(0);
		Assert.isNull(Reflect.castOrNull(java.sql.Date.class, date));
	}
}
