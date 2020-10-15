package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.util.OsUtil.macInt;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class SeekBehavior {

	@Test
	public void shouldLookupByValue() {
		assertThat(Seek.from(0), is(Seek.SEEK_SET));
		assertThat(Seek.from(1), is(Seek.SEEK_CUR));
		assertThat(Seek.from(2), is(Seek.SEEK_END));
		assertThat(Seek.from(macInt(4, 3)), is(Seek.SEEK_DATA));
		assertThat(Seek.from(macInt(3, 4)), is(Seek.SEEK_HOLE));
		assertNull(Seek.from(-1));
	}

}
