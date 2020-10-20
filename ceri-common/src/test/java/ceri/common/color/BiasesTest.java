package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class BiasesTest {
	private static final Bias square = r -> r < 0.5 ? Bias.MIN_RATIO : Bias.MAX_RATIO;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Biases.class);
	}

	@Test
	public void testSine() {
		assertApprox(Biases.Q1_SINE.bias(0), 0);
		assertApprox(Biases.Q1_SINE.bias(0.5), 0.707);
		assertApprox(Biases.Q1_SINE.bias(1), 1);
		assertApprox(Biases.Q4_SINE.bias(0), 0);
		assertApprox(Biases.Q4_SINE.bias(0.5), 0.293);
		assertApprox(Biases.Q4_SINE.bias(1), 1);
		assertApprox(Biases.HALF_SINE.bias(0), 0);
		assertApprox(Biases.HALF_SINE.bias(0.25), 0.146);
		assertApprox(Biases.HALF_SINE.bias(0.5), 0.5);
		assertApprox(Biases.HALF_SINE.bias(0.75), 0.854);
		assertApprox(Biases.HALF_SINE.bias(1), 1);
	}

	@Test
	public void testCircle() {
		assertApprox(Biases.Q2_CIRCLE.bias(0), 0);
		assertApprox(Biases.Q2_CIRCLE.bias(0.5), 0.134);
		assertApprox(Biases.Q2_CIRCLE.bias(1), 1);
		assertApprox(Biases.Q4_CIRCLE.bias(0), 0);
		assertApprox(Biases.Q4_CIRCLE.bias(0.5), 0.866);
		assertApprox(Biases.Q4_CIRCLE.bias(1), 1);
		assertApprox(Biases.CIRCLE_INFLECTION.bias(0), 0);
		assertApprox(Biases.CIRCLE_INFLECTION.bias(0.25), 0.067);
		assertApprox(Biases.CIRCLE_INFLECTION.bias(0.5), 0.5);
		assertApprox(Biases.CIRCLE_INFLECTION.bias(0.75), 0.933);
		assertApprox(Biases.CIRCLE_INFLECTION.bias(1), 1);
	}

	@Test
	public void testSequence() {
		// step up .25 at .125, .375, .625, .875
		Bias seq = Biases.sequence(square, square, square, square);
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
		assertApprox(Biases.inverse(third).bias(0), 0);
		assertApprox(Biases.inverse(third).bias(0.25), 0);
		assertApprox(Biases.inverse(third).bias(0.5), 0);
		assertApprox(Biases.inverse(third).bias(0.75), 1);
		assertApprox(Biases.inverse(third).bias(1), 1);
		// assertApprox(Biases.inverse(Biases.bad).bias(-0.1), 0);
	}

	@Test
	public void testOffset() {
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0), 0);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.25), 0.25);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.5), 0.5);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.75), 0.75);
		Biases.offset(Biases.NONE, 0.5).bias(1);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(1), 1);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0), 0);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.25), 0.25);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.5), 0.5);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(0.75), 0.75);
		assertApprox(Biases.offset(Biases.NONE, 0.5).bias(1), 1);
	}

	@Test
	public void testLimiter() {
		Bias bad = r -> ((r - 0.5) * 2) + 0.5; // -0.5..1.5
		assertApprox(Biases.limiter(bad).bias(-0.1), 0);
		assertApprox(Biases.limiter(bad).bias(0), 0);
		assertApprox(Biases.limiter(bad).bias(0.2), 0);
		assertApprox(Biases.limiter(bad).bias(0.25), 0);
		assertApprox(Biases.limiter(bad).bias(0.5), 0.5);
		assertApprox(Biases.limiter(bad).bias(0.75), 1);
		assertApprox(Biases.limiter(bad).bias(0.8), 1);
		assertApprox(Biases.limiter(bad).bias(1), 1);
		assertApprox(Biases.limiter(bad).bias(1.1), 1);
	}

	@Test
	public void testPartial() {
		assertApprox(Biases.partial(square, 0.5).bias(0), 0);
		assertApprox(Biases.partial(square, 0.5).bias(0.5), 0);
		assertApprox(Biases.partial(square, 0.5).bias(1), 1);
		assertApprox(Biases.partial(square, 0.4).bias(1), 1);
		assertApprox(Biases.partial(Biases.offset(square, 0.5), 0.4).bias(0), 0);
		assertApprox(Biases.partial(Biases.CIRCLE_INFLECTION, 0).bias(0), 0);
		assertApprox(Biases.partial(Biases.CIRCLE_INFLECTION, 0).bias(1), 1);
	}

}
