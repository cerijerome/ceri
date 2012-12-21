package ceri.common.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class ImmutableIterableBehavior {
	
	@Test
	public void shouldIterateItems() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		int i = 0;
		for (String s : new ImmutableIterable<>(list)) {
			assertThat(s, is(list.get(i++)));
		}
	}

	@Test(expected=UnsupportedOperationException.class)
	public void shouldNotAllowRemovals() {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, "A", "B", "C");
		Iterable<String> iterable = new ImmutableIterable<>(list);
		Iterator<String> iterator = iterable.iterator();
		assertThat(iterator.next(), is("A"));
		iterator.remove();
	}

}
