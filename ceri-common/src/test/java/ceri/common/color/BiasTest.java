package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class BiasTest {
	private static final Bias square = r -> r < 0.5 ? 0 : Bias.MAX_RATIO;

	@Test
	public void testIntUpDown() {
		assertEquals(Bias.Int.Std.up.bias(0), 0);
		assertEquals(Bias.Int.Std.up.bias(127), 255);
		assertEquals(Bias.Int.Std.up.bias(255), 255);
		assertEquals(Bias.Int.Std.down.bias(0), 0);
		assertEquals(Bias.Int.Std.down.bias(127), 0);
		assertEquals(Bias.Int.Std.down.bias(255), 255);
	}

	@Test
	public void testIntInvert() {
		Bias.Int third = v -> v < 255 / 3 ? 0 : 255;
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
	public void testIntFromDouble() {
		var bias = Bias.Int.from(r -> r * 2);
		assertEquals(bias.bias(0), 0);
		assertEquals(bias.bias(127), 254);
		assertEquals(bias.bias(128), 255);
		assertEquals(bias.bias(255), 255);

	}

	@Test
	public void testIntLimit() {
		Bias.Int bias = v -> v << 1;
		assertEquals(bias.bias(128), 256);
		assertEquals(bias.bias(256), 512);
		assertEquals(bias.limit().bias(128), 255);
		assertEquals(bias.limit().bias(256), 255);
	}

	@Test
	public void testIntOffsetSimpleGraph() {
		Bias.Int off = Bias.Int.NONE.offset(127);
		// var bias = Bias.Int.Std.halfSine.offset(127);
		assertEquals(off.bias(0), 0);
		assertEquals(off.bias(1), 1);
		assertEquals(off.bias(127), 127);
		assertEquals(off.bias(254), 254);
		assertEquals(off.bias(255), 255);
	}

	@Test
	public void testIntOffsetComplexGraph() {
		Bias.Int bias = Bias.Int.Std.q4q1Sine.offset(127);
		assertEquals(bias.bias(0), 0);
		assertEquals(bias.bias(64), 90);
		assertEquals(bias.bias(127), 128);
		assertEquals(bias.bias(191), 164);
		assertEquals(bias.bias(255), 254);
	}
	
	@Test
	public void testSine() {
		assertApprox(Bias.Std.q1Sine.bias(0), 0);
		assertApprox(Bias.Std.q1Sine.bias(0.5), 0.707);
		assertApprox(Bias.Std.q1Sine.bias(1), 1);
		assertApprox(Bias.Std.q4Sine.bias(0), 0);
		assertApprox(Bias.Std.q4Sine.bias(0.5), 0.293);
		assertApprox(Bias.Std.q4Sine.bias(1), 1);
		assertApprox(Bias.Std.q4q1Sine.bias(0), 0);
		assertApprox(Bias.Std.q4q1Sine.bias(0.25), 0.146);
		assertApprox(Bias.Std.q4q1Sine.bias(0.5), 0.5);
		assertApprox(Bias.Std.q4q1Sine.bias(0.75), 0.854);
		assertApprox(Bias.Std.q4q1Sine.bias(1), 1);
		assertApprox(Bias.Std.q1q4Sine.bias(0), 0);
		assertApprox(Bias.Std.q1q4Sine.bias(0.25), 0.354);
		assertApprox(Bias.Std.q1q4Sine.bias(0.5), 0.5);
		assertApprox(Bias.Std.q1q4Sine.bias(0.75), 0.646);
		assertApprox(Bias.Std.q1q4Sine.bias(1), 1);
	}

	@Test
	public void testCircle() {
		assertApprox(Bias.Std.q2Circle.bias(0), 0);
		assertApprox(Bias.Std.q2Circle.bias(0.5), 0.134);
		assertApprox(Bias.Std.q2Circle.bias(1), 1);
		assertApprox(Bias.Std.q4Circle.bias(0), 0);
		assertApprox(Bias.Std.q4Circle.bias(0.5), 0.866);
		assertApprox(Bias.Std.q4Circle.bias(1), 1);
		assertApprox(Bias.Std.q2q4Circle.bias(0), 0);
		assertApprox(Bias.Std.q2q4Circle.bias(0.25), 0.067);
		assertApprox(Bias.Std.q2q4Circle.bias(0.5), 0.5);
		assertApprox(Bias.Std.q2q4Circle.bias(0.75), 0.933);
		assertApprox(Bias.Std.q2q4Circle.bias(1), 1);
	}

	@Test
	public void testSequence() {
		// step up .25 at .125, .375, .625, .875
		Bias seq = Bias.sequence(square, square, square, square);
		assertApprox(seq.bias(0), 0);
		assertApprox(seq.bias(0.1), 0);
		assertApprox(seq.bias(0.2), 0.25);
		assertApprox(seq.bias(0.3), 0.25);
		assertApprox(seq.bias(0.4), 0.5);
		assertApprox(seq.bias(0.5), 0.5);
		assertApprox(seq.bias(0.6), 0.5);
		assertApprox(seq.bias(0.7), 0.75);
		assertApprox(seq.bias(0.8), 0.75);
		assertApprox(seq.bias(0.9), 1);
		assertApprox(seq.bias(1), 1);

	}

	@Test
	public void testInverse() {
		Bias third = r -> r < 0.333 ? 0 : 1;
		assertApprox(third.bias(0), 0);
		assertApprox(third.bias(0.25), 0);
		assertApprox(third.bias(0.5), 1);
		assertApprox(third.bias(0.75), 1);
		assertApprox(third.bias(1), 1);
		assertApprox(third.invert().bias(0), 0);
		assertApprox(Bias.inverse(third).bias(0), 0);
		assertApprox(Bias.inverse(third).bias(0.25), 0);
		assertApprox(Bias.inverse(third).bias(0.5), 0);
		assertApprox(Bias.inverse(third).bias(0.75), 1);
		assertApprox(Bias.inverse(third).bias(1), 1);
		assertApprox(third.invert().bias(1), 1);
	}

	@Test
	public void testOffset() {
		assertApprox(Bias.NONE.offset(0.5).bias(0), 0);
		assertApprox(Bias.offset(Bias.NONE, 0.5).bias(0), 0);
		assertApprox(Bias.offset(Bias.NONE, 0.5).bias(0.25), 0.25);
		assertApprox(Bias.offset(Bias.NONE, 0.5).bias(0.5), 0.5);
		assertApprox(Bias.offset(Bias.NONE, 0.5).bias(0.75), 0.75);
		assertApprox(Bias.offset(Bias.NONE, 0.5).bias(1), 1);
	}

	@Test
	public void testLimiter() {
		Bias bad = r -> ((r - 0.5) * 2) + 0.5; // -0.5..1.5
		assertApprox(bad.limit().bias(-0.1), 0);
		assertApprox(Bias.limiter(bad).bias(-0.1), 0);
		assertApprox(Bias.limiter(bad).bias(0), 0);
		assertApprox(Bias.limiter(bad).bias(0.2), 0);
		assertApprox(Bias.limiter(bad).bias(0.25), 0);
		assertApprox(Bias.limiter(bad).bias(0.5), 0.5);
		assertApprox(Bias.limiter(bad).bias(0.75), 1);
		assertApprox(Bias.limiter(bad).bias(0.8), 1);
		assertApprox(Bias.limiter(bad).bias(1), 1);
		assertApprox(Bias.limiter(bad).bias(1.1), 1);
	}

	@Test
	public void testPartial() {
		assertApprox(square.partial(0.5).bias(0), 0);
		assertApprox(Bias.partial(square, 0.5).bias(0), 0);
		assertApprox(Bias.partial(square, 0.5).bias(0.5), 0);
		assertApprox(Bias.partial(square, 0.5).bias(1), 1);
		assertApprox(Bias.partial(square, 0.4).bias(1), 1);
		assertApprox(Bias.partial(Bias.offset(square, 0.5), 0.4).bias(0), 0);
		assertApprox(Bias.partial(Bias.Std.q2q4Circle, 0).bias(0), 0);
		assertApprox(Bias.partial(Bias.Std.q2q4Circle, 0).bias(1), 1);
	}
}
