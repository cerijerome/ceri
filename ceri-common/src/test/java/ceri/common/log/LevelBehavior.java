package ceri.common.log;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import org.junit.Test;

public class LevelBehavior {

	@Test
	public void shouldCompareByLevelValue() {
		assertTrue(Level.COMPARATOR.compare(Level.ALL, Level.NONE) > 0);
		assertTrue(Level.COMPARATOR.compare(Level.ALL, Level.ALL) == 0);
		assertTrue(Level.COMPARATOR.compare(Level.INFO, Level.NONE) > 0);
		assertTrue(Level.COMPARATOR.compare(Level.INFO, Level.ERROR) < 0);
		assertTrue(Level.COMPARATOR.compare(Level.ERROR, Level.INFO) > 0);
	}

	@Test
	public void shouldNotValidateLevelOfNone() {
		assertFalse(Level.NONE.valid(Level.NONE));
		assertFalse(Level.NONE.valid(Level.ALL));
		assertFalse(Level.NONE.valid(Level.TRACE));
		assertFalse(Level.NONE.valid(Level.DEBUG));
		assertFalse(Level.NONE.valid(Level.INFO));
		assertFalse(Level.NONE.valid(Level.WARN));
		assertFalse(Level.NONE.valid(Level.ERROR));
		assertFalse(Level.TRACE.valid(Level.NONE));
		assertFalse(Level.DEBUG.valid(Level.NONE));
		assertFalse(Level.INFO.valid(Level.NONE));
		assertFalse(Level.WARN.valid(Level.NONE));
		assertFalse(Level.ERROR.valid(Level.NONE));
	}

	@Test
	public void shouldValidateLevelOfAllExceptNone() {
		assertFalse(Level.ALL.valid(Level.NONE));
		assertTrue(Level.ALL.valid(Level.ALL));
		assertTrue(Level.ALL.valid(Level.TRACE));
		assertTrue(Level.ALL.valid(Level.DEBUG));
		assertTrue(Level.ALL.valid(Level.INFO));
		assertTrue(Level.ALL.valid(Level.WARN));
		assertTrue(Level.ALL.valid(Level.ERROR));
		assertTrue(Level.TRACE.valid(Level.ALL));
		assertTrue(Level.DEBUG.valid(Level.ALL));
		assertTrue(Level.INFO.valid(Level.ALL));
		assertTrue(Level.WARN.valid(Level.ALL));
		assertTrue(Level.ERROR.valid(Level.ALL));
	}

	@Test
	public void shouldValidateLevelByComparison() {
		assertFalse(Level.TRACE.valid(null));
		assertTrue(Level.TRACE.valid(Level.TRACE));
		assertTrue(Level.TRACE.valid(Level.DEBUG));
		assertTrue(Level.TRACE.valid(Level.INFO));
		assertTrue(Level.TRACE.valid(Level.WARN));
		assertTrue(Level.TRACE.valid(Level.ERROR));
		assertFalse(Level.DEBUG.valid(Level.TRACE));
		assertTrue(Level.DEBUG.valid(Level.DEBUG));
		assertTrue(Level.DEBUG.valid(Level.INFO));
		assertTrue(Level.DEBUG.valid(Level.WARN));
		assertTrue(Level.DEBUG.valid(Level.ERROR));
		assertFalse(Level.INFO.valid(Level.TRACE));
		assertFalse(Level.INFO.valid(Level.DEBUG));
		assertTrue(Level.INFO.valid(Level.INFO));
		assertTrue(Level.INFO.valid(Level.WARN));
		assertTrue(Level.INFO.valid(Level.ERROR));
		assertFalse(Level.WARN.valid(Level.TRACE));
		assertFalse(Level.WARN.valid(Level.DEBUG));
		assertFalse(Level.WARN.valid(Level.INFO));
		assertTrue(Level.WARN.valid(Level.WARN));
		assertTrue(Level.WARN.valid(Level.ERROR));
		assertFalse(Level.ERROR.valid(Level.TRACE));
		assertFalse(Level.ERROR.valid(Level.DEBUG));
		assertFalse(Level.ERROR.valid(Level.INFO));
		assertFalse(Level.ERROR.valid(Level.WARN));
		assertTrue(Level.ERROR.valid(Level.ERROR));
	}

	@Test
	public void shouldDetermineIfBelowLevel() {
		assertEquals(Level.ALL.isBelow(null), false);
		assertEquals(Level.ALL.isBelow(Level.NONE), false);
		assertEquals(Level.ALL.isBelow(Level.INFO), false);
		assertEquals(Level.ALL.isBelow(Level.ALL), false);
		assertEquals(Level.INFO.isBelow(null), false);
		assertEquals(Level.INFO.isBelow(Level.NONE), false);
		assertEquals(Level.INFO.isBelow(Level.INFO), false);
		assertEquals(Level.INFO.isBelow(Level.ALL), true);
		assertEquals(Level.NONE.isBelow(null), false);
		assertEquals(Level.NONE.isBelow(Level.NONE), false);
		assertEquals(Level.NONE.isBelow(Level.INFO), true);
		assertEquals(Level.NONE.isBelow(Level.ALL), true);
	}

}
