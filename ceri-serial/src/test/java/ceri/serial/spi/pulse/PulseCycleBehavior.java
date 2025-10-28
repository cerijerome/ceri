package ceri.serial.spi.pulse;

import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class PulseCycleBehavior {

	@Test
	public void shouldCreatePulseStatsFromCycle() {
		var stats = PulseCycle.Std._3_9.cycle.pulseStats(100000);
		Assert.equal(stats.freqHz, 300000);
		Assert.approx(stats.bitNs, 3333.333);
		Assert.approx(stats.pulseNs, 10000.0);
		Assert.approx(stats.t0Ns, 3333.333);
		Assert.approx(stats.t1Ns, 6666.667);
	}

	@Test
	public void shouldNotBreachStatsEqualsContract() {
		var t = PulseCycle.Std._5_27.cycle.stats(1000000);
		var eq0 = PulseCycle.Std._5_27.cycle.stats(1000000);
		var ne0 = PulseCycle.Std._4_27.cycle.stats(1000000);
		var ne1 = PulseCycle.Std._5_27.cycle.stats(900000);
		var ne2 = PulseCycle.Std._5_27.cycle.pulseStats(1000000);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
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
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldFailToCreateFromInvalidParameters() {
		Assert.thrown(() -> PulseCycle.of(PulseCycle.Type.nbit, 4, 0, 2, 2));
		Assert.thrown(() -> PulseCycle.of(PulseCycle.Type.nbit, 3, 0, 1, 3));
		Assert.thrown(() -> PulseCycle.of(null, 4, 0, 1, 2));
	}

	@Test
	public void shouldCalculateCycleSignalBits() {
		Assert.equal(PulseCycle.Std._3.cycle.cycleSignalBits(), 24);
		Assert.equal(PulseCycle.Std._3_9.cycle.cycleSignalBits(), 9);
		Assert.equal(PulseCycle.Std._4.cycle.cycleSignalBits(), 8);
		Assert.equal(PulseCycle.Std._4_27.cycle.cycleSignalBits(), 27);
		Assert.equal(PulseCycle.Std._7_27.cycle.cycleSignalBits(), 27);
	}

	@Test
	public void shouldProvideType() {
		Assert.equal(PulseCycle.Std._3_9.cycle.type, PulseCycle.Type.nbit9);
		Assert.equal(PulseCycle.Std._4.cycle.type, PulseCycle.Type.nbit);
		Assert.equal(PulseCycle.Std._4_27.cycle.type, PulseCycle.Type.nbit27);
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
		Assert.array(actuals, values);
	}
}
