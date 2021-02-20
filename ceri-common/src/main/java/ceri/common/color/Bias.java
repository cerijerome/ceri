package ceri.common.color;

import static ceri.common.math.MathUtil.PI_BY_2;
import static ceri.common.math.MathUtil.periodicLimit;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import static java.lang.Math.PI;
import java.util.Arrays;
import java.util.List;
import ceri.common.math.Bound.Type;

/**
 * Used to adjust fades between two colors. Should return a biased ratio value 0.0 to 1.0 from a
 * given ratio value 0.0 to 1.0.
 */
public interface Bias {
	double MAX_RATIO = 1.0;
	/** No bias. */
	Bias NONE = r -> r;
	/** Moves any value except min to max. */
	Bias UP = r -> r > 0 ? MAX_RATIO : 0;
	/** Moves any value except max to min. */
	Bias DOWN = r -> r < MAX_RATIO ? 0 : MAX_RATIO;
	/** Sine curve from 0 to +PI/2. */
	Bias Q1_SINE = r -> Math.sin(r * PI_BY_2);
	/** Transposed sine curve from -PI/2 to 0. */
	Bias Q4_SINE = inverse(Q1_SINE);
	/** Transposed sine curve from -PI/2 to +PI/2. */
	Bias HALF_SINE = r -> (1.0 + Math.sin((r * PI) - PI_BY_2)) / 2.0;
	/** Transposed circle arc for x > 0, y < 0. */
	Bias Q2_CIRCLE = r -> MAX_RATIO - Math.sqrt(MAX_RATIO - (r * r));
	/** Transposed circle arc for x < 0, y > 0. */
	Bias Q4_CIRCLE = inverse(Q2_CIRCLE);
	/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
	Bias CIRCLE_INFLECTION = sequence(Q2_CIRCLE, Q4_CIRCLE);

	/**
	 * Provides a biased value of the given ratio.
	 */
	double bias(double ratio);

	/**
	 * Makes sure biased ratios within bounds.
	 */
	static Bias limiter(Bias bias) {
		return r -> {
			if (r <= 0) return 0;
			if (r >= MAX_RATIO) return MAX_RATIO;
			r = bias.bias(r);
			if (r < 0) return 0;
			if (r > MAX_RATIO) return MAX_RATIO;
			return r;
		};
	}

	/**
	 * Scales a partial bias starting at 0. If the given bias is not an increasing bias the result
	 * may be clipped.
	 */
	static Bias partial(Bias bias, double len) {
		validateRangeFp(len, 0, MAX_RATIO);
		if (len == 0) return DOWN;
		double max = bias.bias(len);
		if (max == 0) return UP;
		return limiter(r -> r = bias.bias(r * len) / max);
	}

	/**
	 * Wrapping bias offset. Smoother if start and end gradients of the given bias are equal.
	 */
	static Bias offset(Bias bias, double offset) {
		double start = bias.bias(offset);
		return limiter(r -> offset(bias.bias(offset(r, offset)), -start));
	}

	/**
	 * Inverts the bias.
	 */
	static Bias inverse(Bias bias) {
		return r -> MAX_RATIO - bias.bias(MAX_RATIO - r);
	}

	/**
	 * Sequences the biases.
	 */
	static Bias sequence(Bias... biases) {
		return sequence(Arrays.asList(biases));
	}

	/**
	 * Sequences the biases.
	 */
	static Bias sequence(List<Bias> biases) {
		int n = biases.size();
		double partial = MAX_RATIO / n;
		return r -> {
			int i = (int) (r / partial);
			if (i >= n) i = n - 1;
			double start = partial * i;
			return start + (biases.get(i).bias((r - start) * n) * partial);
		};
	}

	private static double offset(double ratio, double offset) {
		return periodicLimit(ratio + offset, MAX_RATIO, Type.inclusive);
	}
}
