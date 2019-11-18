package ceri.common.log;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class LevelBehavior {

	@Test
	public void shouldNotValidateLevelOfNone() {
		assertThat(Level.NONE.valid(Level.NONE), is(false));
		assertThat(Level.NONE.valid(Level.ALL), is(false));
		assertThat(Level.NONE.valid(Level.TRACE), is(false));
		assertThat(Level.NONE.valid(Level.DEBUG), is(false));
		assertThat(Level.NONE.valid(Level.INFO), is(false));
		assertThat(Level.NONE.valid(Level.WARN), is(false));
		assertThat(Level.NONE.valid(Level.ERROR), is(false));
		assertThat(Level.TRACE.valid(Level.NONE), is(false));
		assertThat(Level.DEBUG.valid(Level.NONE), is(false));
		assertThat(Level.INFO.valid(Level.NONE), is(false));
		assertThat(Level.WARN.valid(Level.NONE), is(false));
		assertThat(Level.ERROR.valid(Level.NONE), is(false));
	}

	@Test
	public void shouldValidateLevelOfAllExceptNone() {
		assertThat(Level.ALL.valid(Level.NONE), is(false));
		assertThat(Level.ALL.valid(Level.ALL), is(true));
		assertThat(Level.ALL.valid(Level.TRACE), is(true));
		assertThat(Level.ALL.valid(Level.DEBUG), is(true));
		assertThat(Level.ALL.valid(Level.INFO), is(true));
		assertThat(Level.ALL.valid(Level.WARN), is(true));
		assertThat(Level.ALL.valid(Level.ERROR), is(true));
		assertThat(Level.TRACE.valid(Level.ALL), is(true));
		assertThat(Level.DEBUG.valid(Level.ALL), is(true));
		assertThat(Level.INFO.valid(Level.ALL), is(true));
		assertThat(Level.WARN.valid(Level.ALL), is(true));
		assertThat(Level.ERROR.valid(Level.ALL), is(true));
	}

	@Test
	public void shouldValidateLevelByComparison() {
		assertThat(Level.TRACE.valid(Level.TRACE), is(true));
		assertThat(Level.TRACE.valid(Level.DEBUG), is(true));
		assertThat(Level.TRACE.valid(Level.INFO), is(true));
		assertThat(Level.TRACE.valid(Level.WARN), is(true));
		assertThat(Level.TRACE.valid(Level.ERROR), is(true));
		assertThat(Level.DEBUG.valid(Level.TRACE), is(false));
		assertThat(Level.DEBUG.valid(Level.DEBUG), is(true));
		assertThat(Level.DEBUG.valid(Level.INFO), is(true));
		assertThat(Level.DEBUG.valid(Level.WARN), is(true));
		assertThat(Level.DEBUG.valid(Level.ERROR), is(true));
		assertThat(Level.INFO.valid(Level.TRACE), is(false));
		assertThat(Level.INFO.valid(Level.DEBUG), is(false));
		assertThat(Level.INFO.valid(Level.INFO), is(true));
		assertThat(Level.INFO.valid(Level.WARN), is(true));
		assertThat(Level.INFO.valid(Level.ERROR), is(true));
		assertThat(Level.WARN.valid(Level.TRACE), is(false));
		assertThat(Level.WARN.valid(Level.DEBUG), is(false));
		assertThat(Level.WARN.valid(Level.INFO), is(false));
		assertThat(Level.WARN.valid(Level.WARN), is(true));
		assertThat(Level.WARN.valid(Level.ERROR), is(true));
		assertThat(Level.ERROR.valid(Level.TRACE), is(false));
		assertThat(Level.ERROR.valid(Level.DEBUG), is(false));
		assertThat(Level.ERROR.valid(Level.INFO), is(false));
		assertThat(Level.ERROR.valid(Level.WARN), is(false));
		assertThat(Level.ERROR.valid(Level.ERROR), is(true));
	}

}
