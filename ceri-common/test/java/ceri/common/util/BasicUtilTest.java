package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import org.junit.Test;

public class BasicUtilTest {

	@Test
	public void testUncheckedCast() {
		Object[] array = new String[3];
		String[] castArray = BasicUtil.uncheckedCast(array);
		BasicUtil.unused((Object) castArray);
	}

	@Test
	public void testChooseNonNull() {
		assertThat(BasicUtil.chooseNonNull(null, "Test"), is("Test"));
		assertThat(BasicUtil.chooseNonNull("Test", null), is("Test"));
		assertThat(BasicUtil.chooseNonNull(null, null), nullValue());
	}

	@Test
	public void testCastOrNull() {
		java.sql.Date sqlDate = new java.sql.Date(0);
		assertThat(BasicUtil.castOrNull(Date.class, sqlDate), is((Date) sqlDate));
		Date date = new Date(0);
		assertThat(BasicUtil.castOrNull(java.sql.Date.class, date), nullValue());
	}

	@Test
	public void testForceInit() {
		assertThat(ForceInitTestClassHelper.count, is(0));
		Class<ForceInitTestClass> cls = BasicUtil.forceInit(ForceInitTestClass.class);
		assertThat(ForceInitTestClassHelper.count, is(1));
		cls = BasicUtil.forceInit(ForceInitTestClass.class);
		BasicUtil.unused(cls);
		assertThat(ForceInitTestClassHelper.count, is(1));
	}

	@Test
	public void testInitCause() {
		IllegalStateException e1 = new IllegalStateException();
		IllegalArgumentException e2 = new IllegalArgumentException();
		IllegalStateException e = BasicUtil.initCause(e1, e2);
		assertThat(e.getCause(), is((Throwable) e2));
	}

	@Test
	public void testStackTrace() {
		String stackTrace = BasicUtil.stackTrace(new Exception());
		String[] lines = stackTrace.split("[\\r\\n]+");
		assertThat(lines[0], is("java.lang.Exception"));
		assertTrue(lines[1].trim().startsWith(
			"at ceri.common.util.BasicUtilTest.testStackTrace(BasicUtilTest.java:"));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(BasicUtil.isEmpty((String) null));
		assertTrue(BasicUtil.isEmpty(" \t\r\n"));
		assertTrue(BasicUtil.isEmpty(Collections.emptyMap()));

		assertTrue(BasicUtil.isEmpty((Collection<?>) null));
		Collection<?> collection = new HashSet<>();
		assertTrue(BasicUtil.isEmpty(collection));
		collection.add(null);
		assertFalse(BasicUtil.isEmpty(collection));

		assertTrue(BasicUtil.isEmpty(new Object[] {}));
		assertFalse(BasicUtil.isEmpty(new Object[] { null }));
	}

}
