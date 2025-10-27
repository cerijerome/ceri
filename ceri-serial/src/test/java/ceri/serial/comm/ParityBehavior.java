package ceri.serial.comm;

import org.junit.Test;
import ceri.common.test.Assert;

public class ParityBehavior {

	@Test
	public void shouldLookupByChar() {
		Assert.isNull(Parity.from(' '));
		Assert.equal(Parity.from('n'), Parity.none);
		Assert.equal(Parity.from('N'), Parity.none);
		Assert.equal(Parity.from('o'), Parity.odd);
		Assert.equal(Parity.from('O'), Parity.odd);
		Assert.equal(Parity.from('e'), Parity.even);
		Assert.equal(Parity.from('E'), Parity.even);
		Assert.equal(Parity.from('m'), Parity.mark);
		Assert.equal(Parity.from('M'), Parity.mark);
		Assert.equal(Parity.from('s'), Parity.space);
		Assert.equal(Parity.from('S'), Parity.space);
	}

	@Test
	public void shouldLookupByValue() {
		Assert.isNull(Parity.from(5));
		Assert.equal(Parity.from(0), Parity.none);
		Assert.equal(Parity.from(1), Parity.odd);
		Assert.equal(Parity.from(2), Parity.even);
		Assert.equal(Parity.from(3), Parity.mark);
		Assert.equal(Parity.from(4), Parity.space);
	}

	@Test
	public void shouldDetermineBits() {
		Assert.equal(Parity.none.bits(), 0);
		Assert.equal(Parity.odd.bits(), 1);
		Assert.equal(Parity.even.bits(), 1);
		Assert.equal(Parity.mark.bits(), 1);
		Assert.equal(Parity.space.bits(), 1);
	}

}
