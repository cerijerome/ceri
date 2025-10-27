package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;

public class BiasTest {
	private static final Bias square = r -> r < 0.5 ? 0 : Bias.MAX_RATIO;

	@Test
	public void testIntUpDown() {
		Assert.equal(Bias.Int.Std.up.bias(0), 0);
		Assert.equal(Bias.Int.Std.up.bias(127), 255);
		Assert.equal(Bias.Int.Std.up.bias(255), 255);
		Assert.equal(Bias.Int.Std.down.bias(0), 0);
		Assert.equal(Bias.Int.Std.down.bias(127), 0);
		Assert.equal(Bias.Int.Std.down.bias(255), 255);
	}

	@Test
	public void testIntInvert() {
		Bias.Int third = v -> v < 255 / 3 ? 0 : 255;
		Assert.equal(third.bias(0), 0);
		Assert.equal(third.bias(84), 0);
		Assert.equal(third.bias(85), 255);
		Assert.approx(third.bias(255), 255);
		var inverted = third.invert();
		Assert.equal(inverted.bias(0), 0);
		Assert.equal(inverted.bias(170), 0);
		Assert.equal(inverted.bias(171), 255);
		Assert.approx(inverted.bias(255), 255);
	}

	@Test
	public void testIntFromDouble() {
		var bias = Bias.Int.from(r -> r * 2);
		Assert.equal(bias.bias(0), 0);
		Assert.equal(bias.bias(127), 254);
		Assert.equal(bias.bias(128), 255);
		Assert.equal(bias.bias(255), 255);

	}

	@Test
	public void testIntLimit() {
		Bias.Int bias = v -> v << 1;
		Assert.equal(bias.bias(128), 256);
		Assert.equal(bias.bias(256), 512);
		Assert.equal(bias.limit().bias(128), 255);
		Assert.equal(bias.limit().bias(256), 255);
	}

	@Test
	public void testIntOffsetSimpleGraph() {
		Bias.Int off = Bias.Int.NONE.offset(127);
		// var bias = Bias.Int.Std.halfSine.offset(127);
		Assert.equal(off.bias(0), 0);
		Assert.equal(off.bias(1), 1);
		Assert.equal(off.bias(127), 127);
		Assert.equal(off.bias(254), 254);
		Assert.equal(off.bias(255), 255);
	}

	@Test
	public void testIntOffsetComplexGraph() {
		Bias.Int bias = Bias.Int.Std.q4q1Sine.offset(127);
		Assert.equal(bias.bias(0), 0);
		Assert.equal(bias.bias(64), 90);
		Assert.equal(bias.bias(127), 128);
		Assert.equal(bias.bias(191), 164);
		Assert.equal(bias.bias(255), 254);
	}
	
	@Test
	public void testSine() {
		Assert.approx(Bias.Std.q1Sine.bias(0), 0);
		Assert.approx(Bias.Std.q1Sine.bias(0.5), 0.707);
		Assert.approx(Bias.Std.q1Sine.bias(1), 1);
		Assert.approx(Bias.Std.q4Sine.bias(0), 0);
		Assert.approx(Bias.Std.q4Sine.bias(0.5), 0.293);
		Assert.approx(Bias.Std.q4Sine.bias(1), 1);
		Assert.approx(Bias.Std.q4q1Sine.bias(0), 0);
		Assert.approx(Bias.Std.q4q1Sine.bias(0.25), 0.146);
		Assert.approx(Bias.Std.q4q1Sine.bias(0.5), 0.5);
		Assert.approx(Bias.Std.q4q1Sine.bias(0.75), 0.854);
		Assert.approx(Bias.Std.q4q1Sine.bias(1), 1);
		Assert.approx(Bias.Std.q1q4Sine.bias(0), 0);
		Assert.approx(Bias.Std.q1q4Sine.bias(0.25), 0.354);
		Assert.approx(Bias.Std.q1q4Sine.bias(0.5), 0.5);
		Assert.approx(Bias.Std.q1q4Sine.bias(0.75), 0.646);
		Assert.approx(Bias.Std.q1q4Sine.bias(1), 1);
	}

	@Test
	public void testCircle() {
		Assert.approx(Bias.Std.q2Circle.bias(0), 0);
		Assert.approx(Bias.Std.q2Circle.bias(0.5), 0.134);
		Assert.approx(Bias.Std.q2Circle.bias(1), 1);
		Assert.approx(Bias.Std.q4Circle.bias(0), 0);
		Assert.approx(Bias.Std.q4Circle.bias(0.5), 0.866);
		Assert.approx(Bias.Std.q4Circle.bias(1), 1);
		Assert.approx(Bias.Std.q2q4Circle.bias(0), 0);
		Assert.approx(Bias.Std.q2q4Circle.bias(0.25), 0.067);
		Assert.approx(Bias.Std.q2q4Circle.bias(0.5), 0.5);
		Assert.approx(Bias.Std.q2q4Circle.bias(0.75), 0.933);
		Assert.approx(Bias.Std.q2q4Circle.bias(1), 1);
	}

	@Test
	public void testSequence() {
		// step up .25 at .125, .375, .625, .875
		Bias seq = Bias.sequence(square, square, square, square);
		Assert.approx(seq.bias(0), 0);
		Assert.approx(seq.bias(0.1), 0);
		Assert.approx(seq.bias(0.2), 0.25);
		Assert.approx(seq.bias(0.3), 0.25);
		Assert.approx(seq.bias(0.4), 0.5);
		Assert.approx(seq.bias(0.5), 0.5);
		Assert.approx(seq.bias(0.6), 0.5);
		Assert.approx(seq.bias(0.7), 0.75);
		Assert.approx(seq.bias(0.8), 0.75);
		Assert.approx(seq.bias(0.9), 1);
		Assert.approx(seq.bias(1), 1);

	}

	@Test
	public void testInverse() {
		Bias third = r -> r < 0.333 ? 0 : 1;
		Assert.approx(third.bias(0), 0);
		Assert.approx(third.bias(0.25), 0);
		Assert.approx(third.bias(0.5), 1);
		Assert.approx(third.bias(0.75), 1);
		Assert.approx(third.bias(1), 1);
		Assert.approx(third.invert().bias(0), 0);
		Assert.approx(Bias.inverse(third).bias(0), 0);
		Assert.approx(Bias.inverse(third).bias(0.25), 0);
		Assert.approx(Bias.inverse(third).bias(0.5), 0);
		Assert.approx(Bias.inverse(third).bias(0.75), 1);
		Assert.approx(Bias.inverse(third).bias(1), 1);
		Assert.approx(third.invert().bias(1), 1);
	}

	@Test
	public void testOffset() {
		Assert.approx(Bias.NONE.offset(0.5).bias(0), 0);
		Assert.approx(Bias.offset(Bias.NONE, 0.5).bias(0), 0);
		Assert.approx(Bias.offset(Bias.NONE, 0.5).bias(0.25), 0.25);
		Assert.approx(Bias.offset(Bias.NONE, 0.5).bias(0.5), 0.5);
		Assert.approx(Bias.offset(Bias.NONE, 0.5).bias(0.75), 0.75);
		Assert.approx(Bias.offset(Bias.NONE, 0.5).bias(1), 1);
	}

	@Test
	public void testLimiter() {
		Bias bad = r -> ((r - 0.5) * 2) + 0.5; // -0.5..1.5
		Assert.approx(bad.limit().bias(-0.1), 0);
		Assert.approx(Bias.limiter(bad).bias(-0.1), 0);
		Assert.approx(Bias.limiter(bad).bias(0), 0);
		Assert.approx(Bias.limiter(bad).bias(0.2), 0);
		Assert.approx(Bias.limiter(bad).bias(0.25), 0);
		Assert.approx(Bias.limiter(bad).bias(0.5), 0.5);
		Assert.approx(Bias.limiter(bad).bias(0.75), 1);
		Assert.approx(Bias.limiter(bad).bias(0.8), 1);
		Assert.approx(Bias.limiter(bad).bias(1), 1);
		Assert.approx(Bias.limiter(bad).bias(1.1), 1);
	}

	@Test
	public void testPartial() {
		Assert.approx(square.partial(0.5).bias(0), 0);
		Assert.approx(Bias.partial(square, 0.5).bias(0), 0);
		Assert.approx(Bias.partial(square, 0.5).bias(0.5), 0);
		Assert.approx(Bias.partial(square, 0.5).bias(1), 1);
		Assert.approx(Bias.partial(square, 0.4).bias(1), 1);
		Assert.approx(Bias.partial(Bias.offset(square, 0.5), 0.4).bias(0), 0);
		Assert.approx(Bias.partial(Bias.Std.q2q4Circle, 0).bias(0), 0);
		Assert.approx(Bias.partial(Bias.Std.q2q4Circle, 0).bias(1), 1);
	}
}
