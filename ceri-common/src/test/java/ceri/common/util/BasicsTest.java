package ceri.common.util;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.nullPointer;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;

public class BasicsTest {

	private static class TestRef extends Basics.Ref<AutoCloseable> {
		private TestRef(AutoCloseable c) {
			super(c);
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Basics.class);
	}

	@Test
	public void shouldProvideRefAccess() throws Exception {
		try (AutoCloseable c = () -> {}) {
			var r = new TestRef(c);
			assertEquals(r.ref, c);
		}
	}

	@Test
	public void testAllNull() {
		assertEquals(Basics.allNull((Object[]) null), true);
		assertEquals(Basics.allNull(), true);
		assertEquals(Basics.allNull(new Object[] { null }), true);
		assertEquals(Basics.allNull("a", null), false);
		assertEquals(Basics.allNull("a", "B"), false);
	}

	@Test
	public void testAnyNull() {
		assertEquals(Basics.anyNull((Object[]) null), false);
		assertEquals(Basics.anyNull(), false);
		assertEquals(Basics.anyNull(new Object[] { null }), true);
		assertEquals(Basics.anyNull("a", null), true);
		assertEquals(Basics.anyNull("a", "B"), false);
	}

	@Test
	public void testNoneNull() {
		assertEquals(Basics.noneNull((Object[]) null), true);
		assertEquals(Basics.noneNull(), true);
		assertEquals(Basics.noneNull(new Object[] { null }), false);
		assertEquals(Basics.noneNull("a", null), false);
		assertEquals(Basics.noneNull("a", "B"), true);
	}

	@Test
	public void testDef() throws Exception {
		Assert.isNull(Basics.def(null, null));
		assertEquals(Basics.def(null, 1), 1);
		assertEquals(Basics.def(1, null), 1);
		assertEquals(Basics.def(1, (Excepts.Supplier<Exception, Integer>) null), 1);
		assertEquals(Basics.def(1, 2), 1);
	}

	@Test
	public void testDefInt() {
		nullPointer(() -> Basics.defInt(null, null));
		assertEquals(Basics.defInt(null, -1), -1);
		assertEquals(Basics.defInt(1, -1), 1);
		assertEquals(Basics.defInt(null, () -> -1), -1);
		assertEquals(Basics.defInt(1, () -> -1), 1);
	}

	@Test
	public void testDefLong() {
		nullPointer(() -> Basics.defLong(null, null));
		assertEquals(Basics.defLong(null, -1), -1L);
		assertEquals(Basics.defLong(1L, -1), 1L);
		assertEquals(Basics.defLong(null, () -> -1), -1L);
		assertEquals(Basics.defLong(1L, () -> -1), 1L);
	}

	@Test
	public void testDefDouble() {
		nullPointer(() -> Basics.defDouble(null, null));
		assertEquals(Basics.defDouble(null, -1), -1.0);
		assertEquals(Basics.defDouble(1.0, -1), 1.0);
		assertEquals(Basics.defDouble(null, () -> -1), -1.0);
		assertEquals(Basics.defDouble(1.0, () -> -1), 1.0);
	}

	@Test
	public void testDefGet() throws Exception {
		assertEquals(Basics.defGet(null, null), null);
		assertEquals(Basics.defGet(null, () -> null), null);
		assertEquals(Basics.defGet(null, () -> 1), 1);
		assertEquals(Basics.defGet(() -> null, null), null);
		assertEquals(Basics.defGet(() -> null, () -> null), null);
		assertEquals(Basics.defGet(() -> null, () -> 1), 1);
		assertEquals(Basics.defGet(() -> -1, null), -1);
		assertEquals(Basics.defGet(() -> -1, () -> null), -1);
		assertEquals(Basics.defGet(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetInt() {
		nullPointer(() -> Basics.defGetInt(null, null));
		assertEquals(Basics.defGetInt(null, () -> 1), 1);
		nullPointer(() -> Basics.defGetInt(() -> null, null));
		assertEquals(Basics.defGetInt(() -> null, () -> 1), 1);
		assertEquals(Basics.defGetInt(() -> -1, null), -1);
		assertEquals(Basics.defGetInt(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetLong() {
		nullPointer(() -> Basics.defGetLong(null, null));
		assertEquals(Basics.defGetLong(null, () -> 1), 1L);
		nullPointer(() -> Basics.defGetLong(() -> null, null));
		assertEquals(Basics.defGetLong(() -> null, () -> 1), 1L);
		assertEquals(Basics.defGetLong(() -> -1L, null), -1L);
		assertEquals(Basics.defGetLong(() -> -1L, () -> 1), -1L);
	}

	@Test
	public void testDefGetDouble() {
		nullPointer(() -> Basics.defGetDouble(null, null));
		assertEquals(Basics.defGetDouble(null, () -> 1), 1.0);
		nullPointer(() -> Basics.defGetDouble(() -> null, null));
		assertEquals(Basics.defGetDouble(() -> null, () -> 1), 1.0);
		assertEquals(Basics.defGetDouble(() -> -1.0, null), -1.0);
		assertEquals(Basics.defGetDouble(() -> -1.0, () -> 1), -1.0);
	}

	@Test
	public void testTernaryGet() {
		assertEquals(Basics.ternaryGet(false, () -> "x"), null);
		assertEquals(Basics.ternaryGet(true, () -> "x"), "x");
		assertEquals(Basics.ternaryGet(true, () -> "x", () -> "y"), "x");
	}

	@Test
	public void testTernaryGetWithNull() {
		assertEquals(Basics.ternaryGet(null, () -> "T", () -> "F", () -> null), null);
		assertEquals(Basics.ternaryGet(true, () -> "T", () -> "F", () -> null), "T");
		assertEquals(Basics.ternaryGet(false, () -> "T", () -> "F", () -> null), "F");
		assertEquals(Basics.ternaryGet(null, () -> "T", () -> "F", null), null);
	}

	@Test
	public void testTernary() {
		assertEquals(Basics.ternary(true, "a", "b"), "a");
		assertEquals(Basics.ternary(false, "a", "b"), "b");
		assertEquals(Basics.ternary(null, "a", "b", "c"), "c");
		assertEquals(Basics.ternary(true, "a", "b", "c"), "a");
		assertEquals(Basics.ternary(false, "a", "b", "c"), "b");
	}

	@Test
	public void testTernaryInt() {
		assertEquals(Basics.ternaryInt(true, 1, -1), 1);
		assertEquals(Basics.ternaryInt(false, 1, -1), -1);
		assertEquals(Basics.ternaryInt(null, 1, -1, 0), 0);
		assertEquals(Basics.ternaryInt(true, 1, -1, 0), 1);
		assertEquals(Basics.ternaryInt(false, 1, -1, 0), -1);
	}

	@Test
	public void testTernaryLong() {
		assertEquals(Basics.ternaryLong(true, 1, -1), 1L);
		assertEquals(Basics.ternaryLong(false, 1, -1), -1L);
		assertEquals(Basics.ternaryLong(null, 1, -1, 0), 0L);
		assertEquals(Basics.ternaryLong(true, 1, -1, 0), 1L);
		assertEquals(Basics.ternaryLong(false, 1, -1, 0), -1L);
	}

	@Test
	public void testTernaryDouble() {
		assertEquals(Basics.ternaryDouble(true, 1, -1), 1.0);
		assertEquals(Basics.ternaryDouble(false, 1, -1), -1.0);
		assertEquals(Basics.ternaryDouble(null, 1, -1, 0), 0.0);
		assertEquals(Basics.ternaryDouble(true, 1, -1, 0), 1.0);
		assertEquals(Basics.ternaryDouble(false, 1, -1, 0), -1.0);
	}
}
