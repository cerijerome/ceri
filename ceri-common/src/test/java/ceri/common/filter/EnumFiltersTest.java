package ceri.common.filter;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EnumFiltersTest {

	private static enum T {
		a,
		ab,
		bc,
		abc
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EnumFilters.class);
	}

	@Test
	public void testByName() {
		assertFalse(EnumFilters.<T>byName("ab").filter(null));
		assertFalse(EnumFilters.<T>byName("ab").filter(T.abc));
		assertTrue(EnumFilters.<T>byName("ab").filter(T.ab));
	}

}
