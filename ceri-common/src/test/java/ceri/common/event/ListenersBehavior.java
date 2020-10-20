package ceri.common.event;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.function.Consumer;
import org.junit.Test;

public class ListenersBehavior {

	@Test
	public void shouldAddAndRemoveListeners() {
		StringBuilder b = new StringBuilder();
		Consumer<String> l0 = s -> b.append(s.charAt(0));
		Consumer<String> l1 = s -> b.append(s.charAt(1));
		Listeners<String> ls = Listeners.of();
		assertTrue(ls.isEmpty());
		ls.listen(l0);
		ls.listen(l0);
		ls.listen(l1);
		assertEquals(ls.size(), 3); // listeners stored in list, not set
		ls.accept("ab");
		assertEquals(b.toString(), "aab");
		ls.unlisten(l0);
		ls.accept("cd");
		assertEquals(b.toString(), "aabcd");
		ls.unlisten(l1);
		ls.accept("ef");
		assertEquals(b.toString(), "aabcde");
		ls.unlisten(l0);
		ls.accept("gh");
		assertEquals(b.toString(), "aabcde");
	}

	@Test
	public void shouldClearListeners() {
		StringBuilder b = new StringBuilder();
		Consumer<String> l0 = s -> b.append(s.charAt(0));
		Consumer<String> l1 = s -> b.append(s.charAt(1));
		Listeners<String> ls = Listeners.of();
		ls.listen(l0);
		ls.listen(l1);
		ls.accept("ab");
		assertEquals(b.toString(), "ab");
		ls.clear();
		ls.accept("cd");
		assertEquals(b.toString(), "ab");
	}

	@Test
	public void shouldDuplicateListeners() {
		Listeners<String> ls = Listeners.of();
		Consumer<String> l0 = s -> {};
		Consumer<String> l1 = s -> {};
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
