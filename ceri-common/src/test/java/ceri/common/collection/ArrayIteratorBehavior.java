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
		final Iterator<Boolean> iterator = ArrayIterator.createBoolean(array);
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
		assertThat(ArrayIterator.createByte(Byte.MIN_VALUE).next(), is(Byte.MIN_VALUE));
		assertThat(ArrayIterator.createChar(Character.MAX_VALUE).next(), is(Character.MAX_VALUE));
		for (int i : ArrayIterator.createInt(Integer.MIN_VALUE))
			assertThat(i, is(Integer.MIN_VALUE));
		assertThat(ArrayIterator.createShort(Short.MAX_VALUE).next(), is(Short.MAX_VALUE));
		assertThat(ArrayIterator.createLong(Long.MIN_VALUE).next(), is(Long.MIN_VALUE));
		assertThat(ArrayIterator.createFloat(Float.MAX_VALUE).next(), is(Float.MAX_VALUE));
		assertThat(ArrayIterator.createDouble(Double.NaN).next(), is(Double.NaN));
	}

	@Test
	public void shouldIterateObjects() {
		Iterator<String> iterator = ArrayIterator.createFrom("Hello", "Goodbye");
		assertThat(iterator.next(), is("Hello"));
		assertThat(iterator.next(), is("Goodbye"));
	}

}
