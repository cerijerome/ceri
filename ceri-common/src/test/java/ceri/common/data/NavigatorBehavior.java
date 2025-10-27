package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class NavigatorBehavior {

	@Test
	public void shouldOnlyAllowNonNegativeLengths() {
		Assert.thrown(() -> navigator(-1));
		Navigator<?> n = navigator(10);
		Assert.equal(n.length(), 10);
		n.length(5).length(20);
		Assert.equal(n.length(), 20);
		Assert.thrown(() -> n.length(-1));
		Assert.equal(n.length(), 20);
	}

	@Test
	public void shouldOnlyAllowOffsetWithinLength() {
		Navigator<?> n = navigator(10);
		n.offset(10);
		n.offset(0);
		Assert.thrown(() -> n.offset(-1));
		Assert.thrown(() -> n.offset(11));
	}

	@Test
	public void shouldModifyOffsetIfLengthIsReduced() {
		Navigator<?> n = navigator(10);
		n.offset(8);
		Assert.equal(n.offset(), 8);
		n.length(5);
		Assert.equal(n.offset(), 5);
	}

	@Test
	public void shouldFailToSkipPastLimits() {
		Navigator<?> n = navigator(10);
		n.skip(5).skip(-5).skip(10).skip(-6);
		Assert.thrown(() -> n.skip(-5));
		Assert.thrown(() -> n.skip(7));
	}

	@Test
	public void shouldSupportMarkAndReset() {
		Navigator<?> n = navigator(10);
		Assert.equal(n.offset(5).offset(), 5);
		Assert.equal(n.reset().offset(), 0);
		n.offset(5).mark();
		Assert.equal(n.offset(8).reset().offset(), 5);
	}

	@Test
	public void shouldDetermineRemainingLengthFromOffset() {
		Navigator<?> n = navigator(5);
		Assert.equal(n.remaining(), 5);
		Assert.yes(n.hasNext());
		Assert.equal(n.offset(5).remaining(), 0);
		Assert.no(n.hasNext());
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
