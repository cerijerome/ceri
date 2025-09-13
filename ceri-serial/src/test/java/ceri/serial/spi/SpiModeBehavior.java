package ceri.serial.spi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class SpiModeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SpiMode.builder().chipSelectHigh().build();
		var eq0 = SpiMode.builder().chipSelectHigh().build();
		var eq1 = new SpiMode(0x04);
		var ne0 = SpiMode.builder().build();
		var ne1 = SpiMode.builder().chipSelectHigh().clockPhaseHigh().build();
		var ne2 = SpiMode.builder().chipSelectHigh().clockPolarityHigh().build();
		var ne3 = SpiMode.builder().chipSelectHigh().loop().build();
		var ne4 = SpiMode.builder().chipSelectHigh().lsbFirst().build();
		var ne5 = SpiMode.builder().chipSelectHigh().noChipSelect().build();
		var ne6 = SpiMode.builder().chipSelectHigh().ready().build();
		var ne7 = SpiMode.builder().chipSelectHigh().rxDual().build();
		var ne8 = SpiMode.builder().chipSelectHigh().rxQuad().build();
		var ne9 = SpiMode.builder().chipSelectHigh().spi3Wire().build();
		var ne10 = SpiMode.builder().chipSelectHigh().txDual().build();
		var ne11 = SpiMode.builder().chipSelectHigh().txQuad().build();
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7, ne8, ne9, ne10, ne11);
	}

	@Test
	public void shouldAccessFields() {
		var mode = new SpiMode(0xa5f);
		assertEquals(mode.clockPhaseHigh(), true);
		assertEquals(mode.clockPolarityHigh(), true);
		assertEquals(mode.chipSelectHigh(), true);
		assertEquals(mode.lsbFirst(), true);
		assertEquals(mode.spi3Wire(), true);
		assertEquals(mode.loop(), false);
		assertEquals(mode.noChipSelect(), true);
		assertEquals(mode.ready(), false);
		assertEquals(mode.txDual(), false);
		assertEquals(mode.txQuad(), true);
		assertEquals(mode.rxDual(), false);
		assertEquals(mode.rxQuad(), true);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(new SpiMode(0xa5f), "0x00000a5f");
		assertString(new SpiMode(0x5f), "0x5f");
	}
}
