package ceri.common.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mock;
import ceri.common.test.TestUtil;

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
		assertThat(BasicUtil.abbreviatePackages(""), is(""));
		assertThat(BasicUtil.abbreviatePackages("ceri.common.util.BasicUtil"),
			is("c.c.u.BasicUtil"));
		assertThat(BasicUtil.abbreviatePackages("Name.abc.def.Xyz"), is("Name.a.d.Xyz"));
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
		assertThat(BasicUtil.find(Align.H.class, t -> t != Align.H.left), is(Align.H.center));
		assertThat(BasicUtil.find(Align.H.class, t -> t.name().endsWith("t")), is(Align.H.left));
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
		assertThat(BasicUtil.defaultValue(null, 1), is(1));
		assertThat(BasicUtil.defaultValue(1, null), is(1));
		assertThat(BasicUtil.defaultValue(1, 2), is(1));
	}

	@Test
	public void testConditional() {
		assertNull(BasicUtil.conditional(null, "a", "b"));
		assertThat(BasicUtil.conditional(null, "a", "b", "c"), is("c"));
		assertThat(BasicUtil.conditional(Boolean.TRUE, "a", "b", "c"), is("a"));
		assertThat(BasicUtil.conditional(Boolean.FALSE, "a", "b", "c"), is("b"));
		assertThat(BasicUtil.conditionalInt(true, 1, -1), is(1));
		assertThat(BasicUtil.conditionalInt(false, 1, -1), is(-1));
		assertThat(BasicUtil.conditionalLong(true, 1, -1), is(1L));
		assertThat(BasicUtil.conditionalLong(false, 1, -1), is(-1L));
	}

	private enum Enum {
		a,
		b,
		c;
	}

	@Test
	public void testValueOf() {
		assertThat(BasicUtil.valueOf(null, "a", Enum.a), is(Enum.a));
		assertThat(BasicUtil.valueOf(Enum.class, "a"), is(Enum.a));
		assertNull(BasicUtil.valueOf(Enum.class, "ab"));
		assertNull(BasicUtil.valueOf(Enum.class, null, null));
		assertThat(BasicUtil.valueOf(Enum.class, "b", null), is(Enum.b));
		assertThat(BasicUtil.valueOf(Enum.class, null, Enum.a), is(Enum.a));
		assertThat(BasicUtil.valueOf(Enum.class, "ab", Enum.c), is(Enum.c));
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
		assertThat(BasicUtil.castOrNull(Date.class, sqlDate), is(sqlDate));
		Date date = new Date(0);
		assertThat(BasicUtil.castOrNull(java.sql.Date.class, date), nullValue());
	}

	@Test
	public void testForceInit() {
		assertThat(ForceInitTestClassHelper.count, is(0));
		Class<ForceInitTestClass> cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertThat(ForceInitTestClassHelper.count, is(1));
		cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertNotNull(cls);
		assertThat(ForceInitTestClassHelper.count, is(1));
		TestUtil.assertThrown(() -> BasicUtil.load("", this.getClass().getClassLoader()));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(BasicUtil.isEmpty((String) null));
		assertTrue(BasicUtil.isEmpty((String[]) null));
		assertTrue(BasicUtil.isEmpty(" \t\r\n"));
		assertTrue(BasicUtil.isEmpty((Map<?, ?>) null));
		assertTrue(BasicUtil.isEmpty(Collections.emptyMap()));
		assertFalse(BasicUtil.isEmpty(Collections.singletonMap(1, 2)));
		assertTrue(BasicUtil.isEmpty((Collection<?>) null));
		Collection<?> collection = new HashSet<>();
		assertTrue(BasicUtil.isEmpty(collection));
		collection.add(null);
		assertFalse(BasicUtil.isEmpty(collection));
		assertTrue(BasicUtil.isEmpty(new Object[] {}));
		assertFalse(BasicUtil.isEmpty(new Object[] { null }));
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
