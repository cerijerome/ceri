package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import org.junit.Test;

public class BiasTest {
	private static final Bias square = r -> r < 0.5 ? 0 : Bias.MAX_RATIO;

	@Test
	public void testSine() {
		assertApprox(Bias.Std.q1Sine.bias(0), 0);
		assertApprox(Bias.Std.q1Sine.bias(0.5), 0.707);
		assertApprox(Bias.Std.q1Sine.bias(1), 1);
		assertApprox(Bias.Std.q4Sine.bias(0), 0);
		assertApprox(Bias.Std.q4Sine.bias(0.5), 0.293);
		assertApprox(Bias.Std.q4Sine.bias(1), 1);
		assertApprox(Bias.Std.halfSine.bias(0), 0);
		assertApprox(Bias.Std.halfSine.bias(0.25), 0.146);
		assertApprox(Bias.Std.halfSine.bias(0.5), 0.5);
		assertApprox(Bias.Std.halfSine.bias(0.75), 0.854);
		assertApprox(Bias.Std.halfSine.bias(1), 1);
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
