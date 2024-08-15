package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.typedProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.spi.pulse.PulseCycle.Std._4_27;
import static ceri.serial.spi.pulse.PulseCycle.Std._5;
import static ceri.serial.spi.pulse.PulseCycle.Std._7_27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import org.junit.Test;

public class SpiPulseConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		SpiPulseConfig t = SpiPulseConfig.builder(3).cycle(_5.cycle).build();
		SpiPulseConfig eq0 = SpiPulseConfig.builder(3).cycle(_5.cycle).build();
		SpiPulseConfig ne0 = SpiPulseConfig.of(3);
		SpiPulseConfig ne1 = SpiPulseConfig.builder(1).cycle(_5.cycle).build();
		SpiPulseConfig ne2 = SpiPulseConfig.builder(3).cycle(_4_27.cycle).build();
		SpiPulseConfig ne3 = SpiPulseConfig.builder(3).cycle(_5.cycle).delayMicros(300).build();
		SpiPulseConfig ne4 = SpiPulseConfig.builder(3).cycle(_5.cycle).resetDelayMs(50).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldCreateFromProperties() {
		var props = typedProperties("spi");
		var conf0 = new SpiPulseProperties(props, "spi.0").config();
		var conf1 = new SpiPulseProperties(props, "spi.1").config();
		var conf2 = new SpiPulseProperties(props, "spi.2").config();
		assertEquals(conf0,
			SpiPulseConfig.builder(5).cycle(_7_27.cycle).delayMicros(50).resetDelayMs(100).build());
		assertEquals(conf1,
			SpiPulseConfig.builder(4).cycle(PulseCycle.of(nbit27, 5, 2, 1, 3)).build());
		assertEquals(conf2, SpiPulseConfig.of(3));
	}

	@Test
	public void shouldDetermineIfNull() {
		assertEquals(SpiPulseConfig.of(0).isNull(), true);
		assertEquals(SpiPulseConfig.of(1).isNull(), false);
	}

}
