package ceri.common.collection;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Iterator;
import org.junit.Test;


public class ArrayIteratorBehavior {
	
	@Test
	public void shouldIteratePrimitives() {
		boolean[] array = { true, false, true };
		final Iterator<Boolean> iterator = ArrayIterator.create(array);
		assertThat(iterator.next(), is(true));
		assertThat(iterator.next(), is(false));
		assertThat(iterator.next(), is(true));
		assertException(IndexOutOfBoundsException.class, new Runnable() {
			@Override
			public void run() {
				iterator.next();
			}
		});
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				iterator.remove();
			}
		});
		for (int i : ArrayIterator.create(Integer.MIN_VALUE)) assertThat(i, is(Integer.MIN_VALUE));
		assertThat(ArrayIterator.create(Short.MAX_VALUE).next(), is(Short.MAX_VALUE));
		assertThat(ArrayIterator.create(Long.MIN_VALUE).next(), is(Long.MIN_VALUE));
		assertThat(ArrayIterator.create(Double.NaN).next(), is(Double.NaN));
	}

	@Test
	public void shouldIterateObjects() {
		Iterator<String> iterator = ArrayIterator.createFrom("Hello", "Goodbye");
		assertThat(iterator.next(), is("Hello"));
		assertThat(iterator.next(), is("Goodbye"));
	}


}
