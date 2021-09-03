package ceri.common.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
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
	public void testTryWithResources() {
		int[] array = { 0, 0, 0 };
		BasicUtil.tryWithResources(array, t -> t[1] = 1, t -> t[2] = 2);
		assertArray(array, 0, 1, 2);
		BasicUtil.tryWithResources((int[]) null, t -> t[1] = 1, t -> t[2] = 2);
	}

	@Test
	public void testTryWithResourcesError() {
		int[] array = { 0, 0, 0 };
		assertThrown(() -> BasicUtil.tryWithResources(array, t -> {
			throw new IOException();
		}, t -> t[2] = 1));
		assertArray(array, 0, 0, 1);
	}

	@Test
	public void testBeep() {
		// Make sure no error thrown
		BasicUtil.beep();
	}

	@Test
	public void testDefaultValue() {
		assertNull(BasicUtil.defaultValue(null, null));
		assertEquals(BasicUtil.defaultValue(null, 1), 1);
		assertEquals(BasicUtil.defaultValue(1, null), 1);
		assertEquals(BasicUtil.defaultValue(1, 2), 1);
	}

	@Test
	public void testConditional() {
		assertNull(BasicUtil.conditional(null, "a", "b"));
		assertEquals(BasicUtil.conditional(null, "a", "b", "c"), "c");
		assertEquals(BasicUtil.conditional(Boolean.TRUE, "a", "b", "c"), "a");
		assertEquals(BasicUtil.conditional(Boolean.FALSE, "a", "b", "c"), "b");
		assertEquals(BasicUtil.conditionalInt(true, 1, -1), 1);
		assertEquals(BasicUtil.conditionalInt(false, 1, -1), -1);
		assertEquals(BasicUtil.conditionalLong(true, 1, -1), 1L);
		assertEquals(BasicUtil.conditionalLong(false, 1, -1), -1L);
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
		assertThrown(RuntimeException.class, () -> BasicUtil.runtimeRun(() -> {
			throw new IOException();
		}));
		BasicUtil.runtimeCall(() -> "test");
		assertThrown(RuntimeException.class, () -> BasicUtil.runtimeCall(() -> {
			throw new IOException();
		}));
	}

}
