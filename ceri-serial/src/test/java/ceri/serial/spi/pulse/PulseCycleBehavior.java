package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.spi.pulse.PulseCycle.Std._3;
import static ceri.serial.spi.pulse.PulseCycle.Std._3_9;
import static ceri.serial.spi.pulse.PulseCycle.Std._4;
import static ceri.serial.spi.pulse.PulseCycle.Std._4_27;
import static ceri.serial.spi.pulse.PulseCycle.Std._4_9;
import static ceri.serial.spi.pulse.PulseCycle.Std._5_27;
import static ceri.serial.spi.pulse.PulseCycle.Std._7_27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit9;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import org.junit.Test;

public class PulseCycleBehavior {

	@Test
	public void shouldCreatePulseStatsFromCycle() {
		var stats = _3_9.cycle.pulseStats(100000);
		assertEquals(stats.freqHz, 300000);
		assertApprox(stats.bitNs, 3333.333);
		assertApprox(stats.pulseNs, 10000.0);
		assertApprox(stats.t0Ns, 3333.333);
		assertApprox(stats.t1Ns, 6666.667);
	}

	@Test
	public void shouldNotBreachStatsEqualsContract() {
		var t = _5_27.cycle.stats(1000000);
		var eq0 = _5_27.cycle.stats(1000000);
		var ne0 = _4_27.cycle.stats(1000000);
		var ne1 = _5_27.cycle.stats(900000);
		var ne2 = _5_27.cycle.pulseStats(1000000);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		PulseCycle t = PulseCycle.of(nbit, 6, 1, 2, 4);
		PulseCycle eq0 = PulseCycle.of(nbit, 6, 1, 2, 4);
		PulseCycle ne0 = PulseCycle.of(nbit9, 6, 0, 1, 2);
		PulseCycle ne1 = PulseCycle.of(nbit27, 6, 2, 2, 4);
		PulseCycle ne2 = PulseCycle.of(nbit, 12, 1, 2, 4);
		PulseCycle ne3 = PulseCycle.of(nbit, 6, 0, 2, 4);
		PulseCycle ne4 = PulseCycle.of(nbit, 6, 1, 1, 4);
		PulseCycle ne5 = PulseCycle.of(nbit, 6, 1, 2, 3);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldFailToCreateFromInvalidParameters() {
		assertThrown(() -> PulseCycle.of(nbit, 4, 0, 2, 2));
		assertThrown(() -> PulseCycle.of(nbit, 3, 0, 1, 3));
		assertThrown(() -> PulseCycle.of(null, 4, 0, 1, 2));
	}

	@Test
	public void shouldCalculateCycleSignalBits() {
		assertEquals(_3.cycle.cycleSignalBits(), 24);
		assertEquals(_3_9.cycle.cycleSignalBits(), 9);
		assertEquals(_4.cycle.cycleSignalBits(), 8);
		assertEquals(_4_27.cycle.cycleSignalBits(), 27);
		assertEquals(_7_27.cycle.cycleSignalBits(), 27);
	}

	@Test
	public void shouldProvideType() {
		assertEquals(_3_9.cycle.type, nbit9);
		assertEquals(_4.cycle.type, nbit);
		assertEquals(_4_27.cycle.type, nbit27);
	}

	@Test
	public void shouldCalculatePulseStartPosition() {
		assertSequence(_4.cycle::t0Pos, 0, 4, 8, 12, 16, 20, 24, 28, 32, 36);
		assertSequence(_4_9.cycle::t0Pos, 0, 4, 8, 12, 16, 20, 24, 28, 32, 36);
		assertSequence(_4_27.cycle::t0Pos, 1, 5, 9, 13, 17, 21, 25, 29, 33, 37);
		assertSequence(_7_27.cycle::t0Pos, 0, 6, 13, 20, 24, 30, 37, 44, 48, 54);
	}

	@Test
	public void shouldCalculatePulseWidth() {
		assertSequence(_4.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(_4_9.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(_4_27.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(_7_27.cycle::t0Bits, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2);
		assertSequence(_4.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(_4_9.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(_4_27.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(_7_27.cycle::t1Bits, 3, 4, 4, 4, 3, 4, 4, 4, 3, 4);
	}

	private static void assertSequence(IntUnaryOperator op, int... values) {
		int[] actuals = IntStream.range(0, values.length).map(op).toArray();
		assertArray(actuals, values);
	}

}
