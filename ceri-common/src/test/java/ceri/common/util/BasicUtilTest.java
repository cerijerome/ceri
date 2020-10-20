package ceri.common.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEnum;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mock;
import ceri.common.text.StringUtil;

public class BasicUtilTest {
	@Mock
	Class<?> badClass;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(BasicUtil.class);
	}

	@Test
	public void testAbbreviatePackages() {
		assertNull(BasicUtil.abbreviatePackages(null));
		assertEquals(BasicUtil.abbreviatePackages(""), "");
		assertEquals(BasicUtil.abbreviatePackages("ceri.common.util.BasicUtil"), "c.c.u.BasicUtil");
		assertEquals(BasicUtil.abbreviatePackages("Name.abc.def.Xyz"), "Name.a.d.Xyz");
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
	public void testMicroTime() {
		long t0 = BasicUtil.microTime();
		long t1 = BasicUtil.microTime();
		assertTrue(t1 >= t0);
	}

	@Test
	public void testFind() {
		assertEquals(BasicUtil.find(Align.H.class, t -> t != Align.H.left), Align.H.center);
		assertEquals(BasicUtil.find(Align.H.class, t -> t.name().endsWith("t")), Align.H.left);
		assertNull(BasicUtil.find(Align.H.class, t -> t.name().endsWith("x")));
	}

	@Test
	public void testEnums() {
		assertIterable(BasicUtil.enums(Align.H.class), Align.H.left, Align.H.center, Align.H.right);
		assertIterable(BasicUtil.enums(Align.V.class), Align.V.top, Align.V.middle, Align.V.bottom);
		exerciseEnum(Align.H.class);
		exerciseEnum(Align.V.class);
	}

	@Test
	public void testEnumsReversed() {
		assertIterable(BasicUtil.enumsReversed(Align.H.class), Align.H.right, Align.H.center,
			Align.H.left);
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

	private enum Enum {
		a,
		b,
		c;
	}

	@Test
	public void testValueOf() {
		assertEquals(BasicUtil.valueOf(null, "a", Enum.a), Enum.a);
		assertEquals(BasicUtil.valueOf(Enum.class, "a"), Enum.a);
		assertNull(BasicUtil.valueOf(Enum.class, "ab"));
		assertNull(BasicUtil.valueOf(Enum.class, null, null));
		assertEquals(BasicUtil.valueOf(Enum.class, "b", null), Enum.b);
		assertEquals(BasicUtil.valueOf(Enum.class, null, Enum.a), Enum.a);
		assertEquals(BasicUtil.valueOf(Enum.class, "ab", Enum.c), Enum.c);
		assertNull(BasicUtil.valueOf(Enum.class, "ab", null));
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
		assertEquals(BasicUtil.castOrNull(Date.class, sqlDate), sqlDate);
		Date date = new Date(0);
		assertNull(BasicUtil.castOrNull(java.sql.Date.class, date));
	}

	@Test
	public void testForceInit() {
		assertEquals(ForceInitTestClassHelper.count, 0);
		Class<ForceInitTestClass> cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertEquals(ForceInitTestClassHelper.count, 1);
		cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertNotNull(cls);
		assertEquals(ForceInitTestClassHelper.count, 1);
		assertThrown(() -> BasicUtil.load("", this.getClass().getClassLoader()));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(StringUtil.isBlank((String) null));
		assertTrue(StringUtil.isBlank(" \t\r\n"));
		assertTrue(BasicUtil.isEmpty((Map<?, ?>) null));
		assertTrue(BasicUtil.isEmpty(Collections.emptyMap()));
		assertFalse(BasicUtil.isEmpty(Collections.singletonMap(1, 2)));
		assertTrue(BasicUtil.isEmpty((Collection<?>) null));
		Collection<?> collection = new HashSet<>();
		assertTrue(BasicUtil.isEmpty(collection));
		collection.add(null);
		assertFalse(BasicUtil.isEmpty(collection));
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
