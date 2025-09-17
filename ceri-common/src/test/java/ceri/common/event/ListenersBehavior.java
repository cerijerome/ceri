package ceri.common.event;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.collection.Lists;
import ceri.common.function.Functions;

public class ListenersBehavior {

	@Test
	public void shouldAddAndRemoveListeners() {
		var b = new StringBuilder();
		Functions.Consumer<String> l0 = s -> b.append(s.charAt(0));
		Functions.Consumer<String> l1 = s -> b.append(s.charAt(1));
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
		var b = new StringBuilder();
		Functions.Consumer<String> l0 = s -> b.append(s.charAt(0));
		Functions.Consumer<String> l1 = s -> b.append(s.charAt(1));
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
		var ls = Listeners.<String>of();
		Functions.Consumer<String> l0 = _ -> {};
		Functions.Consumer<String> l1 = _ -> {};
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

	@Test
	public void shouldSendMultipleEvents() {
		var events = Lists.<String>of();
		var listeners = Listeners.<String>of();
		listeners.listen(events::add);
		listeners.acceptAll("abc", "de", "f");
		assertOrdered(events, "abc", "de", "f");
	}
}
