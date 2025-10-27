package ceri.common.event;

import org.junit.Test;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

public class ListenersBehavior {

	@Test
	public void shouldAddAndRemoveListeners() {
		var b = new StringBuilder();
		Functions.Consumer<String> l0 = s -> b.append(s.charAt(0));
		Functions.Consumer<String> l1 = s -> b.append(s.charAt(1));
		Listeners<String> ls = Listeners.of();
		Assert.yes(ls.isEmpty());
		ls.listen(l0);
		ls.listen(l0);
		ls.listen(l1);
		Assert.equal(ls.size(), 3); // listeners stored in list, not set
		ls.accept("ab");
		Assert.equal(b.toString(), "aab");
		ls.unlisten(l0);
		ls.accept("cd");
		Assert.equal(b.toString(), "aabcd");
		ls.unlisten(l1);
		ls.accept("ef");
		Assert.equal(b.toString(), "aabcde");
		ls.unlisten(l0);
		ls.accept("gh");
		Assert.equal(b.toString(), "aabcde");
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
		Assert.equal(b.toString(), "ab");
		ls.clear();
		ls.accept("cd");
		Assert.equal(b.toString(), "ab");
	}

	@Test
	public void shouldDuplicateListeners() {
		var ls = Listeners.<String>of();
		Functions.Consumer<String> l0 = _ -> {};
		Functions.Consumer<String> l1 = _ -> {};
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.listen(l1));
		Assert.yes(ls.listen(l1));
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.no(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l1));
		Assert.yes(ls.unlisten(l1));
		Assert.no(ls.unlisten(l1));
	}

	@Test
	public void shouldSendMultipleEvents() {
		var events = Lists.<String>of();
		var listeners = Listeners.<String>of();
		listeners.listen(events::add);
		listeners.acceptAll("abc", "de", "f");
		Assert.ordered(events, "abc", "de", "f");
	}
}
