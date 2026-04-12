package ceri.common.reflect;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import org.junit.Test;
import ceri.common.except.ExceptionAdapter;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;

public class HandlesTest {
	private static final ExceptionAdapter<IOException> IO = ExceptionAdapter.io;
	private static final ExceptionAdapter<RuntimeException> NULL_E = null;
	private static final Class<?>[] NULL_CS = null;

	public static class C {
		public static VarHandle VI = Handles.handle(C.class, "i");
		public static VarHandle VL = Handles.handle(C.class, "l");
		public static MethodHandle MVI = Handles.getter(VI);
		public static MethodHandle MVL = Handles.getter(VL);
		public static MethodHandle MA = Handles.method(C.class, "a", long.class, int[].class);
		public static MethodHandle MS = Handles.staticMethod(C.class, "s", int.class, int.class);
		public static MethodHandle MC = Handles.constructor(C.class);
		public static MethodHandle MCIL = Handles.constructor(C.class, int.class, long.class);
		public static int i = -1;
		public long l = 333;

		public C() {}

		public C(int i, long l) {
			C.i = i;
			this.l = l;
		}

		public long a(int... ints) {
			return l - Streams.ints(ints).sum();
		}

		public static int s(int i) {
			return C.i - i;
		}
	}

	@Test
	public void testAddExactInt() throws Throwable {
		Assert.equal(Handles.Math.addExact(-1).invoke(3), 2);
		Assert.equal(Handles.Math.addExact(-1L).invoke(3), 2L);
	}

	@Test
	public void testGet() {
		Assert.equal(Handles.get(null), null);
		Assert.equal(Handles.get(C.VI), -1);
		var c = new C();
		Assert.equal(Handles.get(null, c), null);
		Assert.equal(Handles.get(C.VL, c), 333L);
	}

	@Test
	public void testHandle() {
		Assert.equal(Handles.handle(null), null);
		Assert.equal(C.VI.get(), -1);
		Assert.equal(C.VL.get(new C()), 333L);
	}

	@Test
	public void testGetter() {
		Assert.equal(Handles.getter(null), null);
		Assert.equal(Handles.invoke(C.MVI), -1);
		Assert.equal(Handles.invoke(C.MVL, new C()), 333L);
	}

	@Test
	public void testAsSupplier() throws Exception {
		Assert.equal(Handles.asSupplier(null), null);
		Assert.equal(Handles.asSupplier(C.MVI).get(), -1);
		Assert.equal(Handles.asSupplier(null, C.MVI), null);
		Assert.equal(Handles.asSupplier(IO, null), null);
		Assert.equal(Handles.asSupplier(IO, C.MVI).get(), -1);
	}

	@Test
	public void testAsFunction() throws IOException {
		Assert.equal(Handles.asFunction(null), null);
		Assert.equal(Handles.asFunction(C.MVL).apply(new C()), 333L);
		Assert.equal(Handles.asFunction(null, C.MVL), null);
		Assert.equal(Handles.asFunction(IO, null), null);
		Assert.equal(Handles.asFunction(IO, C.MVL).apply(new C()), 333L);
	}

	@Test
	public void testAsVarArg() {
		var c = new C();
		var va = Handles.asVargArg(C.MA);
		Assert.equal(Handles.asVargArg(null), null);
		Assert.equal(va.apply(c, 200, 20, 2), 111L);
		Assert.equal(va.apply(c), 333L);
	}

	@Test
	public void testInvoke() {
		Assert.equal(Handles.invoke(null), null);
		Assert.equal(Handles.invoke(NULL_E, C.MA), null);
		Assert.equal(Handles.invoke(C.MA, new C()), 333L);
		Assert.equal(Handles.invoke(C.MA, new C(), 111, 11, 1), 210L);
	}

	@Test
	public void testConstructor() {
		Assert.equal(Handles.constructor(null), null);
		Assert.equal(Handles.constructor(C.class, NULL_CS), null);
		Assert.equal(Handles.<C>invoke(C.MC).l, 333L);
		Assert.equal(Handles.<C>invoke(C.MCIL, 0, -1L).l, -1L);
	}

	@Test
	public void testMethod() {
		Assert.equal(Handles.method(null, "a", long.class, int[].class), null);
		Assert.equal(Handles.method(C.class, null, long.class, int[].class), null);
		Assert.equal(Handles.method(C.class, "a", null, int[].class), null);
		Assert.equal(Handles.method(C.class, "a", long.class, NULL_CS), null);
		Assert.equal(Handles.invoke(C.MA, new C(), 222), 111L);
	}

	@Test
	public void testStaticMethod() {
		Assert.equal(Handles.staticMethod(null, "a", int.class, int.class), null);
		Assert.equal(Handles.staticMethod(C.class, null, int.class, int.class), null);
		Assert.equal(Handles.staticMethod(C.class, "a", null, int.class), null);
		Assert.equal(Handles.staticMethod(C.class, "a", int.class, NULL_CS), null);
		Assert.equal(Handles.invoke(C.MS, -3), 2);
	}
}
