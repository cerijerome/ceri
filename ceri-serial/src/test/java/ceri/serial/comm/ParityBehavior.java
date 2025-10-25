package ceri.serial.comm;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class ParityBehavior {

	@Test
	public void shouldLookupByChar() {
		Assert.isNull(Parity.from(' '));
		assertEquals(Parity.from('n'), Parity.none);
		assertEquals(Parity.from('N'), Parity.none);
		assertEquals(Parity.from('o'), Parity.odd);
		assertEquals(Parity.from('O'), Parity.odd);
		assertEquals(Parity.from('e'), Parity.even);
		assertEquals(Parity.from('E'), Parity.even);
		assertEquals(Parity.from('m'), Parity.mark);
		assertEquals(Parity.from('M'), Parity.mark);
		assertEquals(Parity.from('s'), Parity.space);
		assertEquals(Parity.from('S'), Parity.space);
	}

	@Test
	public void shouldLookupByValue() {
		Assert.isNull(Parity.from(5));
		assertEquals(Parity.from(0), Parity.none);
		assertEquals(Parity.from(1), Parity.odd);
		assertEquals(Parity.from(2), Parity.even);
		assertEquals(Parity.from(3), Parity.mark);
		assertEquals(Parity.from(4), Parity.space);
	}

	@Test
	public void shouldDetermineBits() {
		assertEquals(Parity.none.bits(), 0);
		assertEquals(Parity.odd.bits(), 1);
		assertEquals(Parity.even.bits(), 1);
		assertEquals(Parity.mark.bits(), 1);
		assertEquals(Parity.space.bits(), 1);
	}

}
