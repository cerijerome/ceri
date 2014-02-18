package ceri.common.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

public class ArrayIteratorBehavior {

	@Test
	public void shouldIteratePrimitives() {
		boolean[] array = { true, false, true };
		final Iterator<Boolean> iterator = ArrayIterator.create(array);
		assertThat(iterator.next(), is(true));
		assertThat(iterator.next(), is(false));
		assertThat(iterator.next(), is(true));
		try {
			iterator.next();
			fail();
		} catch (NoSuchElementException e) {}
		try {
			iterator.remove();
			fail();
		} catch (Exception e) {}
		assertThat(ArrayIterator.create(Byte.MIN_VALUE).next(), is(Byte.MIN_VALUE));
		assertThat(ArrayIterator.create(Character.MAX_VALUE).next(), is(Character.MAX_VALUE));
		for (int i : ArrayIterator.create(Integer.MIN_VALUE))
			assertThat(i, is(Integer.MIN_VALUE));
		assertThat(ArrayIterator.create(Short.MAX_VALUE).next(), is(Short.MAX_VALUE));
		assertThat(ArrayIterator.create(Long.MIN_VALUE).next(), is(Long.MIN_VALUE));
		assertThat(ArrayIterator.create(Float.MAX_VALUE).next(), is(Float.MAX_VALUE));
		assertThat(ArrayIterator.create(Double.NaN).next(), is(Double.NaN));
	}

	@Test
	public void shouldIterateObjects() {
		Iterator<String> iterator = ArrayIterator.createFrom("Hello", "Goodbye");
		assertThat(iterator.next(), is("Hello"));
		assertThat(iterator.next(), is("Goodbye"));
	}

}
