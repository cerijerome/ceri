package ceri.common.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NavigableBehavior {

	@Test
	public void shouldAllowPositionMarkAndReset() {
		Navigable nav = navigable(5);
		nav.offset(1).mark();
		assertThat(nav.marked(), is(0));
		nav.skip(2);
		assertThat(nav.marked(), is(2));
		assertThat(nav.offset(), is(3));
		nav.reset();
		assertThat(nav.marked(), is(0));
		assertThat(nav.offset(), is(1));
	}

	@Test
	public void shouldDetermineRemainingElements() {
		Navigable nav = navigable(5);
		assertThat(nav.remaining(), is(5));
		assertTrue(nav.hasNext());
		nav.skip(4);
		assertThat(nav.remaining(), is(1));
		assertTrue(nav.hasNext());
		nav.skip(1);
		assertThat(nav.remaining(), is(0));
		assertFalse(nav.hasNext());
	}

	private Navigable navigable(int length) {
		return new Navigable() {
			int offset = 0;
			int mark = 0;

			@Override
			public int length() {
				return length;
			}

			@Override
			public Navigable mark() {
				mark = offset;
				return this;
			}

			@Override
			public int marked() {
				return offset - mark;
			}

			@Override
			public int offset() {
				return offset;
			}

			@Override
			public Navigable offset(int offset) {
				this.offset = offset;
				return this;
			}
		};
	}
}
