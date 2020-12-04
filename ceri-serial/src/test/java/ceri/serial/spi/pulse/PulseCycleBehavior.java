package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit9;
import static ceri.serial.spi.pulse.PulseCycles.Std._3;
import static ceri.serial.spi.pulse.PulseCycles.Std._3_9;
import static ceri.serial.spi.pulse.PulseCycles.Std._4;
import static ceri.serial.spi.pulse.PulseCycles.Std._4_27;
import static ceri.serial.spi.pulse.PulseCycles.Std._4_9;
import static ceri.serial.spi.pulse.PulseCycles.Std._7_27;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import org.junit.Test;

public class PulseCycleBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		PulseCycle t = PulseCycles.cycle(nbit, 6, 1, 2, 4);
		PulseCycle eq0 = PulseCycles.cycle(nbit, 6, 1, 2, 4);
		PulseCycle ne0 = PulseCycles.cycle(nbit9, 6, 0, 1, 2);
		PulseCycle ne1 = PulseCycles.cycle(nbit27, 6, 2, 2, 4);
		PulseCycle ne2 = PulseCycles.cycle(nbit, 12, 1, 2, 4);
		PulseCycle ne3 = PulseCycles.cycle(nbit, 6, 0, 2, 4);
		PulseCycle ne4 = PulseCycles.cycle(nbit, 6, 1, 1, 4);
		PulseCycle ne5 = PulseCycles.cycle(nbit, 6, 1, 2, 3);
		PulseCycle ne6 = PulseCycles.cycle(null, 6, 1, 2, 3);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
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
		assertEquals(_3_9.cycle.type(), nbit9);
		assertEquals(_4.cycle.type(), nbit);
		assertEquals(_4_27.cycle.type(), nbit27);
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
