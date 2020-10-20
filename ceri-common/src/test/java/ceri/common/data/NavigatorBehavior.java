package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class NavigatorBehavior {

	@Test
	public void shouldOnlyAllowNonNegativeLengths() {
		assertThrown(() -> navigator(-1));
		Navigator<?> n = navigator(10);
		assertEquals(n.length(), 10);
		n.length(5).length(20);
		assertEquals(n.length(), 20);
		assertThrown(() -> n.length(-1));
		assertEquals(n.length(), 20);
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
		assertEquals(n.offset(), 8);
		n.length(5);
		assertEquals(n.offset(), 5);
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
		assertEquals(n.offset(5).offset(), 5);
		assertEquals(n.reset().offset(), 0);
		n.offset(5).mark();
		assertEquals(n.offset(8).reset().offset(), 5);
	}

	@Test
	public void shouldDetermineRemainingLengthFromOffset() {
		Navigator<?> n = navigator(5);
		assertEquals(n.remaining(), 5);
		assertTrue(n.hasNext());
		assertEquals(n.offset(5).remaining(), 0);
		assertFalse(n.hasNext());
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
