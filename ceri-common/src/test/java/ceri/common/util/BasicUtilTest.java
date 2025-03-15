package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;
import ceri.common.reflect.ReflectUtil;

public class BasicUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BasicUtil.class);
	}

	@Test
	public void testDefaultValue() throws Exception {
		assertNull(BasicUtil.<String>defaultValue(null, null));
		assertEquals(BasicUtil.defaultValue(null, 1), 1);
		assertEquals(BasicUtil.defaultValue(1, null), 1);
		assertEquals(BasicUtil.defaultValue(1, 2), 1);
	}

	@Test
	public void testConditionalGet() {
		assertEquals(BasicUtil.conditionalGet(false, () -> "x"), null);
		assertEquals(BasicUtil.conditionalGet(true, () -> "x"), "x");
		assertEquals(BasicUtil.conditionalGet(true, () -> "x", () -> "y"), "x");
	}

	@Test
	public void testConditionalGetWithNull() {
		assertEquals(BasicUtil.conditionalGet(null, () -> "T", () -> "F", () -> null), null);
		assertEquals(BasicUtil.conditionalGet(true, () -> "T", () -> "F", () -> null), "T");
		assertEquals(BasicUtil.conditionalGet(false, () -> "T", () -> "F", () -> null), "F");
		assertEquals(BasicUtil.conditionalGet(null, () -> "T", () -> "F", null), null);
	}

	@Test
	public void testConditional() {
		assertEquals(BasicUtil.conditional(true, "a", "b"), "a");
		assertEquals(BasicUtil.conditional(false, "a", "b"), "b");
		assertEquals(BasicUtil.conditionalInt(true, 1, -1), 1);
		assertEquals(BasicUtil.conditionalInt(false, 1, -1), -1);
		assertEquals(BasicUtil.conditionalLong(true, 1, -1), 1L);
		assertEquals(BasicUtil.conditionalLong(false, 1, -1), -1L);
	}

	@Test
	public void testConditionalObj() {
		assertEquals(BasicUtil.conditional(null, "a", "b", "c"), "c");
		assertEquals(BasicUtil.conditional(true, "a", "b", "c"), "a");
		assertEquals(BasicUtil.conditional(false, "a", "b", "c"), "b");
		assertEquals(BasicUtil.conditionalInt(null, 1, -1, 0), 0);
		assertEquals(BasicUtil.conditionalInt(true, 1, -1, 0), 1);
		assertEquals(BasicUtil.conditionalInt(false, 1, -1, 0), -1);
		assertEquals(BasicUtil.conditionalLong(null, 1, -1, 0), 0L);
		assertEquals(BasicUtil.conditionalLong(true, 1, -1, 0), 1L);
		assertEquals(BasicUtil.conditionalLong(false, 1, -1, 0), -1L);
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
		assertEquals(ReflectUtil.castOrNull(Date.class, sqlDate), sqlDate);
		Date date = new Date(0);
		assertNull(ReflectUtil.castOrNull(java.sql.Date.class, date));
	}

	@Test
	public void testRuntimeRun() {
		BasicUtil.runtimeRun(() -> {});
		assertRte(() -> BasicUtil.runtimeRun(() -> {
			throw new IOException();
		}));
		BasicUtil.runtimeCall(() -> "test");
		assertRte(() -> BasicUtil.runtimeCall(() -> {
			throw new IOException();
		}));
	}

}
