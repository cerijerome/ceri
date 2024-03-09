package ceri.common.validation;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.validation.DisplayLong.bin;
import static ceri.common.validation.DisplayLong.dec;
import static ceri.common.validation.DisplayLong.hex;
import static ceri.common.validation.DisplayLong.hex16;
import static ceri.common.validation.DisplayLong.hex2;
import static ceri.common.validation.DisplayLong.hex4;
import static ceri.common.validation.DisplayLong.hex8;
import static ceri.common.validation.DisplayLong.udec;
import org.junit.Test;

public class DisplayLongBehavior {

	@Test
	public void shouldDisplayMultipleFormats() {
		assertEquals(DisplayLong.format(-1, dec, hex), "(-1, 0xffffffffffffffff)");
	}

	@Test
	public void shouldDisplayformat() {
		assertEquals(dec.apply(-1), "-1");
		assertEquals(udec.apply(-1), "18446744073709551615");
		assertEquals(bin.apply(0xa1), "0b10100001");
		assertEquals(hex.apply(0xf000000000000000L), "0xf000000000000000");
		assertEquals(hex2.apply(-1), "0xff");
		assertEquals(hex2.apply(0xf), "0x0f");
		assertEquals(hex2.apply(0x123), "0x23");
		assertEquals(hex4.apply(-1), "0xffff");
		assertEquals(hex4.apply(0xf), "0x000f");
		assertEquals(hex4.apply(0x12345), "0x2345");
		assertEquals(hex8.apply(-1), "0xffffffff");
		assertEquals(hex8.apply(0xf), "0x0000000f");
		assertEquals(hex8.apply(0x123456789L), "0x23456789");
		assertEquals(hex16.apply(-1), "0xffffffffffffffff");
		assertEquals(hex16.apply(Long.MIN_VALUE), "0x8000000000000000");
	}

}
