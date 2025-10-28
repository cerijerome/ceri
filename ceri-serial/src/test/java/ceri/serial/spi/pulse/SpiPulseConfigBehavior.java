package ceri.serial.spi.pulse;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class SpiPulseConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SpiPulseConfig.builder(3).cycle(PulseCycle.Std._5.cycle).build();
		var eq0 = SpiPulseConfig.builder(3).cycle(PulseCycle.Std._5.cycle).build();
		var ne0 = SpiPulseConfig.of(3);
		var ne1 = SpiPulseConfig.builder(1).cycle(PulseCycle.Std._5.cycle).build();
		var ne2 = SpiPulseConfig.builder(3).cycle(PulseCycle.Std._4_27.cycle).build();
		var ne3 = SpiPulseConfig.builder(3).cycle(PulseCycle.Std._5.cycle).delayMicros(300).build();
		var ne4 = SpiPulseConfig.builder(3).cycle(PulseCycle.Std._5.cycle).resetDelayMs(50).build();
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldCreateFromProperties() {
		var props = Testing.properties("spi");
		var conf0 = new SpiPulseConfig.Properties(props, "spi.0").config();
		var conf1 = new SpiPulseConfig.Properties(props, "spi.1").config();
		var conf2 = new SpiPulseConfig.Properties(props, "spi.2").config();
		Assert.equal(conf0, SpiPulseConfig.builder(5).cycle(PulseCycle.Std._7_27.cycle)
			.delayMicros(50).resetDelayMs(100).build());
		Assert.equal(conf1, SpiPulseConfig.builder(4)
			.cycle(PulseCycle.of(PulseCycle.Type.nbit27, 5, 2, 1, 3)).build());
		Assert.equal(conf2, SpiPulseConfig.of(3));
	}

	@Test
	public void shouldDetermineIfNull() {
		Assert.equal(SpiPulseConfig.of(0).isNull(), true);
		Assert.equal(SpiPulseConfig.of(1).isNull(), false);
	}
}
