package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class AlignBehavior {

	@Test
	public void shouldDetermineOffset() {
		assertEquals(Align.H.left.offset(5, 10), 0);
		assertEquals(Align.H.center.offset(5, 10), 3);
		assertEquals(Align.H.right.offset(5, 10), 5);
		assertEquals(Align.V.top.offset(4, 11), 0);
		assertEquals(Align.V.middle.offset(4, 11), 4);
		assertEquals(Align.V.bottom.offset(4, 11), 7);
	}

	@Test
	public void shouldDetermineRoundedDownOffset() {
		assertEquals(Align.H.left.offsetFloor(5, 10), 0);
		assertEquals(Align.H.center.offsetFloor(5, 10), 2);
		assertEquals(Align.H.right.offsetFloor(5, 10), 5);
		assertEquals(Align.V.top.offsetFloor(4, 11), 0);
		assertEquals(Align.V.middle.offsetFloor(4, 11), 3);
		assertEquals(Align.V.bottom.offsetFloor(4, 11), 7);
	}

	@Test
	public void shouldHaveNegativeOffsetForSmallLength() {
		assertEquals(Align.H.left.offset(10, 5), 0);
		assertEquals(Align.H.center.offset(10, 5), -2);
		assertEquals(Align.H.right.offset(10, 5), -5);
		assertEquals(Align.V.top.offset(11, 4), 0);
		assertEquals(Align.V.middle.offset(11, 4), -3);
		assertEquals(Align.V.bottom.offset(11, 4), -7);
	}

	@Test
	public void shouldReverseAlignment() {
		assertEquals(Align.H.left.reverse(), Align.H.right);
		assertEquals(Align.H.center.reverse(), Align.H.center);
		assertEquals(Align.H.right.reverse(), Align.H.left);
		assertEquals(Align.V.top.reverse(), Align.V.bottom);
		assertEquals(Align.V.middle.reverse(), Align.V.middle);
		assertEquals(Align.V.bottom.reverse(), Align.V.top);
	}

	@Test
	public void shouldInvertAlignment() {
		assertEquals(Align.H.left.invert(), Align.V.top);
		assertEquals(Align.H.center.invert(), Align.V.middle);
		assertEquals(Align.H.right.invert(), Align.V.bottom);
		assertEquals(Align.V.top.invert(), Align.H.left);
		assertEquals(Align.V.middle.invert(), Align.H.center);
		assertEquals(Align.V.bottom.invert(), Align.H.right);
	}

}
