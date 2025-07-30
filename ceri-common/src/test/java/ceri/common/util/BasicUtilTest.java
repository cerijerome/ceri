package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNpe;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;
import ceri.common.function.Excepts;

public class BasicUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BasicUtil.class);
	}

	@Test
	public void testUnchecked() {
		Object[] array = new String[3];
		String[] castArray = BasicUtil.unchecked(array);
		BasicUtil.unused((Object) castArray);
	}

	@Test
	public void testRequireNonNull() {
		assertNpe(() -> BasicUtil.requireNonNull((Object[]) null));
		assertNpe(() -> BasicUtil.requireNonNull(new Object[] { null }));
		BasicUtil.requireNonNull();
		assertNpe(() -> BasicUtil.requireNonNull("a", null));
		BasicUtil.requireNonNull("a", "b");
	}

	@Test
	public void testAllNull() {
		assertEquals(BasicUtil.allNull((Object[]) null), true);
		assertEquals(BasicUtil.allNull(), true);
		assertEquals(BasicUtil.allNull(new Object[] { null }), true);
		assertEquals(BasicUtil.allNull("a", null), false);
		assertEquals(BasicUtil.allNull("a", "B"), false);
	}

	@Test
	public void testAnyNull() {
		assertEquals(BasicUtil.anyNull((Object[]) null), false);
		assertEquals(BasicUtil.anyNull(), false);
		assertEquals(BasicUtil.anyNull(new Object[] { null }), true);
		assertEquals(BasicUtil.anyNull("a", null), true);
		assertEquals(BasicUtil.anyNull("a", "B"), false);
	}

	@Test
	public void testNoneNull() {
		assertEquals(BasicUtil.noneNull((Object[]) null), true);
		assertEquals(BasicUtil.noneNull(), true);
		assertEquals(BasicUtil.noneNull(new Object[] { null }), false);
		assertEquals(BasicUtil.noneNull("a", null), false);
		assertEquals(BasicUtil.noneNull("a", "B"), true);
	}

	@Test
	public void testDef() throws Exception {
		assertNull(BasicUtil.def(null, null));
		assertEquals(BasicUtil.def(null, 1), 1);
		assertEquals(BasicUtil.def(1, null), 1);
		assertEquals(BasicUtil.def(1, (Excepts.Supplier<Exception, Integer>) null), 1);
		assertEquals(BasicUtil.def(1, 2), 1);
	}

	@Test
	public void testDefInt() {
		assertNpe(() -> BasicUtil.defInt(null, null));
		assertEquals(BasicUtil.defInt(null, -1), -1);
		assertEquals(BasicUtil.defInt(1, -1), 1);
		assertEquals(BasicUtil.defInt(null, () -> -1), -1);
		assertEquals(BasicUtil.defInt(1, () -> -1), 1);
	}

	@Test
	public void testDefLong() {
		assertNpe(() -> BasicUtil.defLong(null, null));
		assertEquals(BasicUtil.defLong(null, -1), -1L);
		assertEquals(BasicUtil.defLong(1L, -1), 1L);
		assertEquals(BasicUtil.defLong(null, () -> -1), -1L);
		assertEquals(BasicUtil.defLong(1L, () -> -1), 1L);
	}

	@Test
	public void testDefDouble() {
		assertNpe(() -> BasicUtil.defDouble(null, null));
		assertEquals(BasicUtil.defDouble(null, -1), -1.0);
		assertEquals(BasicUtil.defDouble(1.0, -1), 1.0);
		assertEquals(BasicUtil.defDouble(null, () -> -1), -1.0);
		assertEquals(BasicUtil.defDouble(1.0, () -> -1), 1.0);
	}

	@Test
	public void testDefGet() throws Exception {
		assertEquals(BasicUtil.defGet(null, null), null);
		assertEquals(BasicUtil.defGet(null, () -> null), null);
		assertEquals(BasicUtil.defGet(null, () -> 1), 1);
		assertEquals(BasicUtil.defGet(() -> null, null), null);
		assertEquals(BasicUtil.defGet(() -> null, () -> null), null);
		assertEquals(BasicUtil.defGet(() -> null, () -> 1), 1);
		assertEquals(BasicUtil.defGet(() -> -1, null), -1);
		assertEquals(BasicUtil.defGet(() -> -1, () -> null), -1);
		assertEquals(BasicUtil.defGet(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetInt() {
		assertNpe(() -> BasicUtil.defGetInt(null, null));
		assertEquals(BasicUtil.defGetInt(null, () -> 1), 1);
		assertNpe(() -> BasicUtil.defGetInt(() -> null, null));
		assertEquals(BasicUtil.defGetInt(() -> null, () -> 1), 1);
		assertEquals(BasicUtil.defGetInt(() -> -1, null), -1);
		assertEquals(BasicUtil.defGetInt(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetLong() {
		assertNpe(() -> BasicUtil.defGetLong(null, null));
		assertEquals(BasicUtil.defGetLong(null, () -> 1), 1L);
		assertNpe(() -> BasicUtil.defGetLong(() -> null, null));
		assertEquals(BasicUtil.defGetLong(() -> null, () -> 1), 1L);
		assertEquals(BasicUtil.defGetLong(() -> -1L, null), -1L);
		assertEquals(BasicUtil.defGetLong(() -> -1L, () -> 1), -1L);
	}

	@Test
	public void testDefGetDouble() {
		assertNpe(() -> BasicUtil.defGetDouble(null, null));
		assertEquals(BasicUtil.defGetDouble(null, () -> 1), 1.0);
		assertNpe(() -> BasicUtil.defGetDouble(() -> null, null));
		assertEquals(BasicUtil.defGetDouble(() -> null, () -> 1), 1.0);
		assertEquals(BasicUtil.defGetDouble(() -> -1.0, null), -1.0);
		assertEquals(BasicUtil.defGetDouble(() -> -1.0, () -> 1), -1.0);
	}

	@Test
	public void testTernaryGet() {
		assertEquals(BasicUtil.ternaryGet(false, () -> "x"), null);
		assertEquals(BasicUtil.ternaryGet(true, () -> "x"), "x");
		assertEquals(BasicUtil.ternaryGet(true, () -> "x", () -> "y"), "x");
	}

	@Test
	public void testTernaryGetWithNull() {
		assertEquals(BasicUtil.ternaryGet(null, () -> "T", () -> "F", () -> null), null);
		assertEquals(BasicUtil.ternaryGet(true, () -> "T", () -> "F", () -> null), "T");
		assertEquals(BasicUtil.ternaryGet(false, () -> "T", () -> "F", () -> null), "F");
		assertEquals(BasicUtil.ternaryGet(null, () -> "T", () -> "F", null), null);
	}

	@Test
	public void testTernary() {
		assertEquals(BasicUtil.ternary(true, "a", "b"), "a");
		assertEquals(BasicUtil.ternary(false, "a", "b"), "b");
		assertEquals(BasicUtil.ternary(null, "a", "b", "c"), "c");
		assertEquals(BasicUtil.ternary(true, "a", "b", "c"), "a");
		assertEquals(BasicUtil.ternary(false, "a", "b", "c"), "b");
	}

	@Test
	public void testTernaryInt() {
		assertEquals(BasicUtil.ternaryInt(true, 1, -1), 1);
		assertEquals(BasicUtil.ternaryInt(false, 1, -1), -1);
		assertEquals(BasicUtil.ternaryInt(null, 1, -1, 0), 0);
		assertEquals(BasicUtil.ternaryInt(true, 1, -1, 0), 1);
		assertEquals(BasicUtil.ternaryInt(false, 1, -1, 0), -1);
	}

	@Test
	public void testTernaryLong() {
		assertEquals(BasicUtil.ternaryLong(true, 1, -1), 1L);
		assertEquals(BasicUtil.ternaryLong(false, 1, -1), -1L);
		assertEquals(BasicUtil.ternaryLong(null, 1, -1, 0), 0L);
		assertEquals(BasicUtil.ternaryLong(true, 1, -1, 0), 1L);
		assertEquals(BasicUtil.ternaryLong(false, 1, -1, 0), -1L);
	}

	@Test
	public void testTernaryDouble() {
		assertEquals(BasicUtil.ternaryDouble(true, 1, -1), 1.0);
		assertEquals(BasicUtil.ternaryDouble(false, 1, -1), -1.0);
		assertEquals(BasicUtil.ternaryDouble(null, 1, -1, 0), 0.0);
		assertEquals(BasicUtil.ternaryDouble(true, 1, -1, 0), 1.0);
		assertEquals(BasicUtil.ternaryDouble(false, 1, -1, 0), -1.0);
	}
}
