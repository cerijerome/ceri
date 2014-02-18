package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.NoSuchElementException;
import org.junit.Test;

public class BitIteratorBehavior {

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowRemove() {
		new BitIterator((byte) 0).remove();
	}

	@Test
	public void shouldIterateSingleByte() {
		BitIterator i = new BitIterator((byte) -1);
		assertThat(i.hasNext(), is(true));
		assertNext(i, true, true, true, true, true, true, true, true);
		assertThat(i.hasNext(), is(false));
		try {
			i.next();
			fail();
		} catch (NoSuchElementException e) {}
	}

	@Test
	public void shouldIterateHighToLow() {
		BitIterator i = new BitIterator(BitIterator.Start.high, Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertThat(i.hasNext(), is(true));
		assertNext(i, false, true, true, true, true, true, true, true);
		assertNext(i, true, false, false, false, false, false, false, false);
		assertThat(i.hasNext(), is(false));
		try {
			i.next();
			fail();
		} catch (NoSuchElementException e) {}
	}

	@Test
	public void shouldIterateLowToHigh() {
		BitIterator i = new BitIterator(BitIterator.Start.low, Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertThat(i.hasNext(), is(true));
		assertNext(i, true, true, true, true, true, true, true, false);
		assertNext(i, false, false, false, false, false, false, false, true);
		assertThat(i.hasNext(), is(false));
		try {
			i.next();
			fail();
		} catch (NoSuchElementException e) {}
	}

	private void assertNext(BitIterator i, boolean... bools) {
		for (boolean bool : bools) {
			assertThat(i.next(), is(bool));
		}
	}

}
