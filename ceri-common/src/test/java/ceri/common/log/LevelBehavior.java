package ceri.common.log;

import org.junit.Test;
import ceri.common.test.Assert;

public class LevelBehavior {

	@Test
	public void shouldCompareByLevelValue() {
		Assert.yes(Level.COMPARATOR.compare(Level.ALL, Level.NONE) > 0);
		Assert.yes(Level.COMPARATOR.compare(Level.ALL, Level.ALL) == 0);
		Assert.yes(Level.COMPARATOR.compare(Level.INFO, Level.NONE) > 0);
		Assert.yes(Level.COMPARATOR.compare(Level.INFO, Level.ERROR) < 0);
		Assert.yes(Level.COMPARATOR.compare(Level.ERROR, Level.INFO) > 0);
	}

	@Test
	public void shouldNotValidateLevelOfNone() {
		Assert.no(Level.NONE.valid(Level.NONE));
		Assert.no(Level.NONE.valid(Level.ALL));
		Assert.no(Level.NONE.valid(Level.TRACE));
		Assert.no(Level.NONE.valid(Level.DEBUG));
		Assert.no(Level.NONE.valid(Level.INFO));
		Assert.no(Level.NONE.valid(Level.WARN));
		Assert.no(Level.NONE.valid(Level.ERROR));
		Assert.no(Level.TRACE.valid(Level.NONE));
		Assert.no(Level.DEBUG.valid(Level.NONE));
		Assert.no(Level.INFO.valid(Level.NONE));
		Assert.no(Level.WARN.valid(Level.NONE));
		Assert.no(Level.ERROR.valid(Level.NONE));
	}

	@Test
	public void shouldValidateLevelOfAllExceptNone() {
		Assert.no(Level.ALL.valid(Level.NONE));
		Assert.yes(Level.ALL.valid(Level.ALL));
		Assert.yes(Level.ALL.valid(Level.TRACE));
		Assert.yes(Level.ALL.valid(Level.DEBUG));
		Assert.yes(Level.ALL.valid(Level.INFO));
		Assert.yes(Level.ALL.valid(Level.WARN));
		Assert.yes(Level.ALL.valid(Level.ERROR));
		Assert.yes(Level.TRACE.valid(Level.ALL));
		Assert.yes(Level.DEBUG.valid(Level.ALL));
		Assert.yes(Level.INFO.valid(Level.ALL));
		Assert.yes(Level.WARN.valid(Level.ALL));
		Assert.yes(Level.ERROR.valid(Level.ALL));
	}

	@Test
	public void shouldValidateLevelByComparison() {
		Assert.no(Level.TRACE.valid(null));
		Assert.yes(Level.TRACE.valid(Level.TRACE));
		Assert.yes(Level.TRACE.valid(Level.DEBUG));
		Assert.yes(Level.TRACE.valid(Level.INFO));
		Assert.yes(Level.TRACE.valid(Level.WARN));
		Assert.yes(Level.TRACE.valid(Level.ERROR));
		Assert.no(Level.DEBUG.valid(Level.TRACE));
		Assert.yes(Level.DEBUG.valid(Level.DEBUG));
		Assert.yes(Level.DEBUG.valid(Level.INFO));
		Assert.yes(Level.DEBUG.valid(Level.WARN));
		Assert.yes(Level.DEBUG.valid(Level.ERROR));
		Assert.no(Level.INFO.valid(Level.TRACE));
		Assert.no(Level.INFO.valid(Level.DEBUG));
		Assert.yes(Level.INFO.valid(Level.INFO));
		Assert.yes(Level.INFO.valid(Level.WARN));
		Assert.yes(Level.INFO.valid(Level.ERROR));
		Assert.no(Level.WARN.valid(Level.TRACE));
		Assert.no(Level.WARN.valid(Level.DEBUG));
		Assert.no(Level.WARN.valid(Level.INFO));
		Assert.yes(Level.WARN.valid(Level.WARN));
		Assert.yes(Level.WARN.valid(Level.ERROR));
		Assert.no(Level.ERROR.valid(Level.TRACE));
		Assert.no(Level.ERROR.valid(Level.DEBUG));
		Assert.no(Level.ERROR.valid(Level.INFO));
		Assert.no(Level.ERROR.valid(Level.WARN));
		Assert.yes(Level.ERROR.valid(Level.ERROR));
	}

	@Test
	public void shouldDetermineIfBelowLevel() {
		Assert.equal(Level.ALL.isBelow(null), false);
		Assert.equal(Level.ALL.isBelow(Level.NONE), false);
		Assert.equal(Level.ALL.isBelow(Level.INFO), false);
		Assert.equal(Level.ALL.isBelow(Level.ALL), false);
		Assert.equal(Level.INFO.isBelow(null), false);
		Assert.equal(Level.INFO.isBelow(Level.NONE), false);
		Assert.equal(Level.INFO.isBelow(Level.INFO), false);
		Assert.equal(Level.INFO.isBelow(Level.ALL), true);
		Assert.equal(Level.NONE.isBelow(null), false);
		Assert.equal(Level.NONE.isBelow(Level.NONE), false);
		Assert.equal(Level.NONE.isBelow(Level.INFO), true);
		Assert.equal(Level.NONE.isBelow(Level.ALL), true);
	}

}
