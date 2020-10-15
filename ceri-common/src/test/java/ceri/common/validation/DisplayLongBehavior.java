package ceri.common.validation;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.validation.DisplayLong.bin;
import static ceri.common.validation.DisplayLong.dec;
import static ceri.common.validation.DisplayLong.hex;
import static ceri.common.validation.DisplayLong.hex16;
import static ceri.common.validation.DisplayLong.hex2;
import static ceri.common.validation.DisplayLong.hex4;
import static ceri.common.validation.DisplayLong.hex8;
import static ceri.common.validation.DisplayLong.udec;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class DisplayLongBehavior {

	@Test
	public void shouldDisplayMultipleFormats() {
		assertThat(DisplayLong.format(-1, dec, hex), is("(-1, 0xffffffffffffffff)"));
	}

	@Test
	public void shouldDisplayformat() {
		assertThat(dec.format(-1), is("-1"));
		assertThat(udec.format(-1), is("18446744073709551615"));
		assertThat(bin.format(0xa1), is("0b10100001"));
		assertThat(hex.format(0xf000000000000000L), is("0xf000000000000000"));
		assertThat(hex2.format(-1), is("0xff"));
		assertThat(hex2.format(0xf), is("0x0f"));
		assertThat(hex2.format(0x123), is("0x23"));
		assertThat(hex4.format(-1), is("0xffff"));
		assertThat(hex4.format(0xf), is("0x000f"));
		assertThat(hex4.format(0x12345), is("0x2345"));
		assertThat(hex8.format(-1), is("0xffffffff"));
		assertThat(hex8.format(0xf), is("0x0000000f"));
		assertThat(hex8.format(0x123456789L), is("0x23456789"));
		assertThat(hex16.format(-1), is("0xffffffffffffffff"));
		assertThat(hex16.format(Long.MIN_VALUE), is("0x8000000000000000"));
	}

}
