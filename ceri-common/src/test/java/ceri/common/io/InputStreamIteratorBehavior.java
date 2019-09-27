package ceri.common.io;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class InputStreamIteratorBehavior {
	private static final byte[] bytes = { Byte.MIN_VALUE, Byte.MAX_VALUE, 0, 1, -1 };
	private InputStreamIterator i;

	@Before
	public void init() {
		InputStream in = new ByteArrayInputStream(bytes);
		i = new InputStreamIterator(in);
	}

	@Test
	public void shouldThrowRuntimeExceptionForInputStreamFailures() throws IOException {
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.read()).thenThrow(new IOException());
			i = new InputStreamIterator(in);
			assertThrown(RuntimeException.class, () -> i.hasNext());
			assertThrown(RuntimeException.class, () -> i.next());
		}
	}

	@Test
	public void shouldIterateBytes() {
		assertThat(i.hasNext(), is(true));
		assertThat(i.next(), is(Byte.MIN_VALUE));
		assertThat(i.next(), is(Byte.MAX_VALUE));
		assertThat(i.next(), is((byte) 0));
		assertThat(i.next(), is((byte) 1));
		assertThat(i.next(), is((byte) -1));
		assertThat(i.hasNext(), is(false));
		try {
			i.next();
			fail();
		} catch (NoSuchElementException e) {
			// expected
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailToRemove() {
		i.remove();
	}

}
