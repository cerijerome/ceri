package ceri.common.collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

public class ReverseListIteratorBehavior {
	
	@Test(expected=NoSuchElementException.class)
	public void shouldIterateListInReverse() {
		Iterator<String> iterator = new ReverseListIterator<>(Arrays.asList("A", "B", "C"));
		assertThat(iterator.next(), is("C"));
		assertThat(iterator.next(), is("B"));
		assertThat(iterator.next(), is("A"));
		iterator.next();
	}


}
