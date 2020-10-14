package ceri.common.data;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class NavigatorBehavior {

	@Test
	public void shouldOnlyAllowNonNegativeLengths() {
		assertThrown(() -> navigator(-1));
		Navigator<?> n = navigator(10);
		assertThat(n.length(), is(10));
		n.length(5).length(20);
		assertThat(n.length(), is(20));
		assertThrown(() -> n.length(-1));
		assertThat(n.length(), is(20));
	}

	@Test
	public void shouldOnlyAllowOffsetWithinLength() {
		Navigator<?> n = navigator(10);
		n.offset(10);
		n.offset(0);
		assertThrown(() -> n.offset(-1));
		assertThrown(() -> n.offset(11));
	}

	@Test
	public void shouldModifyOffsetIfLengthIsReduced() {
		Navigator<?> n = navigator(10);
		n.offset(8);
		assertThat(n.offset(), is(8));
		n.length(5);
		assertThat(n.offset(), is(5));
	}

	@Test
	public void shouldFailToSkipPastLimits() {
		Navigator<?> n = navigator(10);
		n.skip(5).skip(-5).skip(10).skip(-6);
		assertThrown(() -> n.skip(-5));
		assertThrown(() -> n.skip(7));
	}

	@Test
	public void shouldSupportMarkAndReset() {
		Navigator<?> n = navigator(10);
		assertThat(n.offset(5).offset(), is(5));
		assertThat(n.reset().offset(), is(0));
		n.offset(5).mark();
		assertThat(n.offset(8).reset().offset(), is(5));
	}

	@Test
	public void shouldDetermineRemainingLengthFromOffset() {
		Navigator<?> n = navigator(5);
		assertThat(n.remaining(), is(5));
		assertThat(n.hasNext(), is(true));
		assertThat(n.offset(5).remaining(), is(0));
		assertThat(n.hasNext(), is(false));
	}

	private static Navigator<?> navigator(int length) {
		return new Nav(length);
	}

	private static class Nav extends Navigator<Nav> {
		public Nav(int length) {
			super(length);
		}
	}
}
