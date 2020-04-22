package ceri.serial.clib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertThat(Seek.from(0), is(Seek.SEEK_SET));
		assertThat(Seek.from(1), is(Seek.SEEK_CUR));
		assertThat(Seek.from(2), is(Seek.SEEK_END));
		assertNull(Seek.from(-1));
		assertNull(Seek.from(4));
	}

}
