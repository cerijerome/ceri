package ceri.common.event;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import org.junit.Test;
import ceri.common.function.Functions;

public class IntListenersBehavior {

	@Test
	public void shouldAddAndRemoveIntListeners() {
		int[] count = new int[1];
		Functions.IntConsumer l0 = i -> count[0] += i;
		Functions.IntConsumer l1 = i -> count[0] += (i * 100);
		IntListeners ls = IntListeners.of();
		assertTrue(ls.isEmpty());
		ls.listen(l0);
		ls.listen(l0);
		ls.listen(l1);
		assertEquals(ls.size(), 3);
		ls.accept(1);
		assertEquals(count[0], 102);
		ls.unlisten(l0);
		ls.accept(2);
		assertEquals(count[0], 102 + 202);
		ls.unlisten(l1);
		ls.accept(3);
		assertEquals(count[0], 102 + 202 + 3);
		ls.unlisten(l0);
		ls.accept(4);
		assertEquals(count[0], 102 + 202 + 3);
	}

	@Test
	public void shouldDuplicateIntListeners() {
		IntListeners ls = IntListeners.of();
		Functions.IntConsumer l0 = _ -> {};
		Functions.IntConsumer l1 = _ -> {};
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
