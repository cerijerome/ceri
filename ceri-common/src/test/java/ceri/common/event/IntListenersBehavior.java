package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.function.IntConsumer;
import org.junit.Test;

public class IntListenersBehavior {

	@Test
	public void shouldAddAndRemoveIntListeners() {
		int[] count = new int[1];
		IntConsumer l0 = i -> count[0] += i;
		IntConsumer l1 = i -> count[0] += (i * 100);
		IntListeners ls = new IntListeners();
		ls.listen(l0);
		ls.listen(l0);
		ls.listen(l1);
		ls.accept(1);
		assertThat(count[0], is(102));
		ls.unlisten(l0);
		ls.accept(2);
		assertThat(count[0], is(102 + 202));
		ls.unlisten(l1);
		ls.accept(3);
		assertThat(count[0], is(102 + 202 + 3));
		ls.unlisten(l0);
		ls.accept(4);
		assertThat(count[0], is(102 + 202 + 3));
	}

	@Test
	public void shouldDuplicateIntListeners() {
		IntListeners ls = new IntListeners();
		IntConsumer l0 = s -> {};
		IntConsumer l1 = s -> {};
		assertTrue(ls.listen(l0));
		assertTrue(ls.listen(l0));
		assertTrue(ls.listen(l1));
		assertTrue(ls.listen(l1));
		assertTrue(ls.listen(l0));
		assertTrue(ls.unlisten(l0));
		assertTrue(ls.unlisten(l0));
		assertTrue(ls.unlisten(l0));
		assertFalse(ls.unlisten(l0));
		assertTrue(ls.unlisten(l1));
		assertTrue(ls.unlisten(l1));
		assertFalse(ls.unlisten(l1));
	}

}