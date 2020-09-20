package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
		assertThat(ls.size(), is(3)); // listeners stored in list, not set
		ls.accept("ab");
		assertThat(b.toString(), is("aab"));
		ls.unlisten(l0);
		ls.accept("cd");
		assertThat(b.toString(), is("aabcd"));
		ls.unlisten(l1);
		ls.accept("ef");
		assertThat(b.toString(), is("aabcde"));
		ls.unlisten(l0);
		ls.accept("gh");
		assertThat(b.toString(), is("aabcde"));
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
		assertThat(b.toString(), is("ab"));
		ls.clear();
		ls.accept("cd");
		assertThat(b.toString(), is("ab"));
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
