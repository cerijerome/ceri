package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class BasicUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BasicUtil.class);
	}

	@Test
	public void testDef() throws Exception {
		assertNull(BasicUtil.<String>def(null, null));
		assertEquals(BasicUtil.def(null, 1), 1);
		assertEquals(BasicUtil.def(1, null), 1);
		assertEquals(BasicUtil.def(1, 2), 1);
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
		assertEquals(BasicUtil.ternaryInt(true, 1, -1), 1);
		assertEquals(BasicUtil.ternaryInt(false, 1, -1), -1);
		assertEquals(BasicUtil.ternaryLong(true, 1, -1), 1L);
		assertEquals(BasicUtil.ternaryLong(false, 1, -1), -1L);
	}

	@Test
	public void testTernaryObj() {
		assertEquals(BasicUtil.ternary(null, "a", "b", "c"), "c");
		assertEquals(BasicUtil.ternary(true, "a", "b", "c"), "a");
		assertEquals(BasicUtil.ternary(false, "a", "b", "c"), "b");
		assertEquals(BasicUtil.ternaryInt(null, 1, -1, 0), 0);
		assertEquals(BasicUtil.ternaryInt(true, 1, -1, 0), 1);
		assertEquals(BasicUtil.ternaryInt(false, 1, -1, 0), -1);
		assertEquals(BasicUtil.ternaryLong(null, 1, -1, 0), 0L);
		assertEquals(BasicUtil.ternaryLong(true, 1, -1, 0), 1L);
		assertEquals(BasicUtil.ternaryLong(false, 1, -1, 0), -1L);
	}

	@Test
	public void testUnchecked() {
		Object[] array = new String[3];
		String[] castArray = BasicUtil.unchecked(array);
		BasicUtil.unused((Object) castArray);
	}
}
