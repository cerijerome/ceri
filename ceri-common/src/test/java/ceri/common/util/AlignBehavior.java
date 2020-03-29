package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class AlignBehavior {

	@Test
	public void shouldDetermineOffset() {
		assertThat(Align.H.left.offset(5, 10), is(0));
		assertThat(Align.H.center.offset(5, 10), is(3));
		assertThat(Align.H.right.offset(5, 10), is(5));
		assertThat(Align.V.top.offset(4, 11), is(0));
		assertThat(Align.V.middle.offset(4, 11), is(4));
		assertThat(Align.V.bottom.offset(4, 11), is(7));
	}

	@Test
	public void shouldDetermineRoundedDownOffset() {
		assertThat(Align.H.left.offsetFloor(5, 10), is(0));
		assertThat(Align.H.center.offsetFloor(5, 10), is(2));
		assertThat(Align.H.right.offsetFloor(5, 10), is(5));
		assertThat(Align.V.top.offsetFloor(4, 11), is(0));
		assertThat(Align.V.middle.offsetFloor(4, 11), is(3));
		assertThat(Align.V.bottom.offsetFloor(4, 11), is(7));
	}

	@Test
	public void shouldHaveNegativeOffsetForSmallLength() {
		assertThat(Align.H.left.offset(10, 5), is(0));
		assertThat(Align.H.center.offset(10, 5), is(-2));
		assertThat(Align.H.right.offset(10, 5), is(-5));
		assertThat(Align.V.top.offset(11, 4), is(0));
		assertThat(Align.V.middle.offset(11, 4), is(-3));
		assertThat(Align.V.bottom.offset(11, 4), is(-7));
	}

	@Test
	public void shouldReverseAlignment() {
		assertThat(Align.H.left.reverse(), is(Align.H.right));
		assertThat(Align.H.center.reverse(), is(Align.H.center));
		assertThat(Align.H.right.reverse(), is(Align.H.left));
		assertThat(Align.V.top.reverse(), is(Align.V.bottom));
		assertThat(Align.V.middle.reverse(), is(Align.V.middle));
		assertThat(Align.V.bottom.reverse(), is(Align.V.top));
	}

	@Test
	public void shouldInvertAlignment() {
		assertThat(Align.H.left.invert(), is(Align.V.top));
		assertThat(Align.H.center.invert(), is(Align.V.middle));
		assertThat(Align.H.right.invert(), is(Align.V.bottom));
		assertThat(Align.V.top.invert(), is(Align.H.left));
		assertThat(Align.V.middle.invert(), is(Align.H.center));
		assertThat(Align.V.bottom.invert(), is(Align.H.right));
	}

}
