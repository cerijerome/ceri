package ceri.x10.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class UnexpectedByteExceptionBehavior {

	@Test
	public void shouldStoreExpectedAndActualBytes() {
		UnexpectedByteException e = new UnexpectedByteException(1, -1);
		assertThat(e.actual, is((byte) -1));
		assertThat(e.expected, is((byte) 1));
	}

	@Test
	public void shouldStoreActualBytes() {
		UnexpectedByteException e = new UnexpectedByteException(1);
		assertThat(e.actual, is((byte) 1));
	}

}
