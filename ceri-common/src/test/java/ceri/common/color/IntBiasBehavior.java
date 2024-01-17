package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class IntBiasBehavior {

	@Test
	public void shouldProvideStandardInstances() {
		assertEquals(IntBias.Std.up.bias(0), 0);
		assertEquals(IntBias.Std.up.bias(127), 255);
		assertEquals(IntBias.Std.up.bias(255), 255);
		assertEquals(IntBias.Std.down.bias(0), 0);
		assertEquals(IntBias.Std.down.bias(127), 0);
		assertEquals(IntBias.Std.down.bias(255), 255);
	}

	@Test
	public void shouldInvertGraph() {
		IntBias third = v -> v < 255 / 3 ? 0 : 255;
		assertEquals(third.bias(0), 0);
		assertEquals(third.bias(84), 0);
		assertEquals(third.bias(85), 255);
		assertApprox(third.bias(255), 255);
		var inverted = third.invert();
		assertEquals(inverted.bias(0), 0);
		assertEquals(inverted.bias(170), 0);
		assertEquals(inverted.bias(171), 255);
		assertApprox(inverted.bias(255), 255);
	}

	@Test
	public void shouldCreateFromDoubleBias() {
		var bias = IntBias.from(r -> r * 2);
		assertEquals(bias.bias(0), 0);
		assertEquals(bias.bias(127), 254);
		assertEquals(bias.bias(128), 255);
		assertEquals(bias.bias(255), 255);

	}

	@Test
	public void shouldLimitOutput() {
		IntBias bias = v -> v << 1;
		assertEquals(bias.bias(128), 256);
		assertEquals(bias.bias(256), 512);
		assertEquals(bias.limit().bias(128), 255);
		assertEquals(bias.limit().bias(256), 255);
	}

	@Test
	public void shouldOffsetSimpleGraph() {
		IntBias off = IntBias.NONE.offset(127);
		// var bias = IntBias.Std.halfSine.offset(127);
		assertEquals(off.bias(0), 0);
		assertEquals(off.bias(1), 1);
		assertEquals(off.bias(127), 127);
		assertEquals(off.bias(254), 254);
		assertEquals(off.bias(255), 255);
	}

	@Test
	public void shouldOffsetComplexGraph() {
		IntBias bias = IntBias.Std.q4q1Sine.offset(127);
		assertEquals(bias.bias(0), 0);
		assertEquals(bias.bias(64), 90);
		assertEquals(bias.bias(127), 128);
		assertEquals(bias.bias(191), 164);
		assertEquals(bias.bias(255), 254);
	}

}
