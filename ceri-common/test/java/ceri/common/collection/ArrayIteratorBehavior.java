package ceri.common.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Iterator;
import org.junit.Test;


public class ArrayIteratorBehavior {
	
	@Test
	public void shouldIteratePrimitives() {
		boolean[] array = { true, false, true };
		Iterator<Boolean> iterator = ArrayIterator.create(array);
		assertThat(iterator.next(), is(true));
		assertThat(iterator.next(), is(false));
		assertThat(iterator.next(), is(true));
	}

	@Test
	public void shouldIterateObjects() {
		Iterator<String> iterator = ArrayIterator.create("Hello", "Goodbye");
		assertThat(iterator.next(), is("Hello"));
		assertThat(iterator.next(), is("Goodbye"));
	}


}
