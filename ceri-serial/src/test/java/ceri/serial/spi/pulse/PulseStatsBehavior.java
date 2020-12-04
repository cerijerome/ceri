package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.spi.pulse.PulseCycles.Std._3;
import static ceri.serial.spi.pulse.PulseCycles.Std._3_9;
import org.junit.Test;

public class PulseStatsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		PulseStats t = PulseCycles.pulseStats(_3_9.cycle, 100000);
		PulseStats eq0 = PulseCycles.pulseStats(_3_9.cycle, 100000);
		PulseStats ne0 = PulseCycles.pulseStats(_3.cycle, 100000);
		PulseStats ne1 = PulseCycles.pulseStats(_3_9.cycle, 80000);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromCycle() {
		var stats = PulseCycles.pulseStats(_3_9.cycle, 100000);
		assertEquals(stats.freqHz, 300000);
		assertApprox(stats.bitNs, 3333.333);
		assertApprox(stats.pulseNs, 10000.0);
		assertApprox(stats.t0Ns, 3333.333);
		assertApprox(stats.t1Ns, 6666.667);
	}

}
