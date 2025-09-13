package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.stream.Streams;
import ceri.common.test.TestUtil;

public class PulseCycleBehavior {

	@Test
	public void shouldCreatePulseStatsFromCycle() {
		var stats = PulseCycle.Std._3_9.cycle.pulseStats(100000);
		assertEquals(stats.freqHz, 300000);
		assertApprox(stats.bitNs, 3333.333);
		assertApprox(stats.pulseNs, 10000.0);
		assertApprox(stats.t0Ns, 3333.333);
		assertApprox(stats.t1Ns, 6666.667);
	}

	@Test
	public void shouldNotBreachStatsEqualsContract() {
		var t = PulseCycle.Std._5_27.cycle.stats(1000000);
		var eq0 = PulseCycle.Std._5_27.cycle.stats(1000000);
		var ne0 = PulseCycle.Std._4_27.cycle.stats(1000000);
		var ne1 = PulseCycle.Std._5_27.cycle.stats(900000);
		var ne2 = PulseCycle.Std._5_27.cycle.pulseStats(1000000);
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = PulseCycle.of(PulseCycle.Type.nbit, 6, 1, 2, 4);
		var eq0 = PulseCycle.of(PulseCycle.Type.nbit, 6, 1, 2, 4);
		var ne0 = PulseCycle.of(PulseCycle.Type.nbit9, 6, 0, 1, 2);
		var ne1 = PulseCycle.of(PulseCycle.Type.nbit27, 6, 2, 2, 4);
		var ne2 = PulseCycle.of(PulseCycle.Type.nbit, 12, 1, 2, 4);
		var ne3 = PulseCycle.of(PulseCycle.Type.nbit, 6, 0, 2, 4);
		var ne4 = PulseCycle.of(PulseCycle.Type.nbit, 6, 1, 1, 4);
		var ne5 = PulseCycle.of(PulseCycle.Type.nbit, 6, 1, 2, 3);
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldFailToCreateFromInvalidParameters() {
		assertThrown(() -> PulseCycle.of(PulseCycle.Type.nbit, 4, 0, 2, 2));
		assertThrown(() -> PulseCycle.of(PulseCycle.Type.nbit, 3, 0, 1, 3));
		assertThrown(() -> PulseCycle.of(null, 4, 0, 1, 2));
	}

	@Test
	public void shouldCalculateCycleSignalBits() {
		assertEquals(PulseCycle.Std._3.cycle.cycleSignalBits(), 24);
		assertEquals(PulseCycle.Std._3_9.cycle.cycleSignalBits(), 9);
		assertEquals(PulseCycle.Std._4.cycle.cycleSignalBits(), 8);
		assertEquals(PulseCycle.Std._4_27.cycle.cycleSignalBits(), 27);
		assertEquals(PulseCycle.Std._7_27.cycle.cycleSignalBits(), 27);
	}

	@Test
	public void shouldProvideType() {
		assertEquals(PulseCycle.Std._3_9.cycle.type, PulseCycle.Type.nbit9);
		assertEquals(PulseCycle.Std._4.cycle.type, PulseCycle.Type.nbit);
		assertEquals(PulseCycle.Std._4_27.cycle.type, PulseCycle.Type.nbit27);
	}

	@Test
	public void shouldCalculatePulseStartPosition() {
		assertSequence(PulseCycle.Std._4.cycle::t0Pos, 0, 4, 8, 12, 16, 20, 24, 28, 32, 36);
		assertSequence(PulseCycle.Std._4_9.cycle::t0Pos, 0, 4, 8, 12, 16, 20, 24, 28, 32, 36);
		assertSequence(PulseCycle.Std._4_27.cycle::t0Pos, 1, 5, 9, 13, 17, 21, 25, 29, 33, 37);
		assertSequence(PulseCycle.Std._7_27.cycle::t0Pos, 0, 6, 13, 20, 24, 30, 37, 44, 48, 54);
	}

	@Test
	public void shouldCalculatePulseWidth() {
		assertSequence(PulseCycle.Std._4.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(PulseCycle.Std._4_9.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(PulseCycle.Std._4_27.cycle::t0Bits, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
		assertSequence(PulseCycle.Std._7_27.cycle::t0Bits, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2);
		assertSequence(PulseCycle.Std._4.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(PulseCycle.Std._4_9.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(PulseCycle.Std._4_27.cycle::t1Bits, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
		assertSequence(PulseCycle.Std._7_27.cycle::t1Bits, 3, 4, 4, 4, 3, 4, 4, 4, 3, 4);
	}

	private static void assertSequence(Functions.IntOperator op, int... values) {
		int[] actuals = Streams.slice(0, values.length).map(op).toArray();
		assertArray(actuals, values);
	}
}
