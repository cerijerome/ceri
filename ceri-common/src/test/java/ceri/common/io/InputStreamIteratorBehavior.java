package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

public class InputStreamIteratorBehavior {
	private static final byte[] bytes = { Byte.MIN_VALUE, Byte.MAX_VALUE, 0, 1, -1 };
	private InputStream in;
	private InputStreamIterator i;
	
	@Before
	public void init() {
		in = new ByteArrayInputStream(bytes);
		i = new InputStreamIterator(in);
	}
	
	@Test
	public void shouldIterateBytes() {
		assertThat(i.hasNext(), is(true));
		assertThat(i.next(), is(Byte.MIN_VALUE));
		assertThat(i.next(), is(Byte.MAX_VALUE));
		assertThat(i.next(), is((byte)0));
		assertThat(i.next(), is((byte)1));
		assertThat(i.next(), is((byte)-1));
		assertThat(i.hasNext(), is(false));
		try {
			i.next();
			fail();
		} catch (NoSuchElementException e) {}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailToRemove() {
		i.remove();
	}
	
}
