package ceri.common.event;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.function.IntConsumer;
import org.junit.Test;

public class IntListenersBehavior {

	@Test
	public void shouldAddAndRemoveIntListeners() {
		int[] count = new int[1];
		IntConsumer l0 = i -> count[0] += i;
		IntConsumer l1 = i -> count[0] += (i * 100);
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
