package ceri.common.color;

import static ceri.common.color.Bias.MAX_RATIO;
import static ceri.common.color.Bias.MIN_RATIO;
import static ceri.common.validation.ValidationUtil.validateRange;
import static java.lang.Math.PI;
import java.util.Arrays;
import java.util.List;

public class Biases {
	private static final double PI_2 = PI / 2;
	private static final Bias UP = r -> r > MIN_RATIO ? MAX_RATIO : MIN_RATIO;
	private static final Bias DOWN = r -> r < MAX_RATIO ? MIN_RATIO : MAX_RATIO;

	/**
	 * Sine curve from 0 to +PI/2.
	 */
	public static final Bias Q1_SINE = r -> Math.sin(r * PI_2);

	/**
	 * Transposed sine curve from -PI/2 to 0.
	 */
	public static final Bias Q4_SINE = inverse(Q1_SINE);

	/**
	 * Transposed sine curve from -PI/2 to +PI/2.
	 */
	public static final Bias HALF_SINE = r -> (1.0 + Math.sin((r * PI) - PI_2)) / 2.0;

	/**
	 * Transposed circle arc for x > 0, y < 0.
	 */
	public static final Bias Q2_CIRCLE = r -> MAX_RATIO - Math.sqrt(MAX_RATIO - (r * r));

	/**
	 * Transposed circle arc for x < 0, y > 0.
	 */
	public static final Bias Q4_CIRCLE = inverse(Q2_CIRCLE);

	/**
	 * Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0.
	 */
	public static final Bias CIRCLE_INFLECTION = sequence(Q2_CIRCLE, Q4_CIRCLE);

	/**
	 * Pass-through bias.
	 */
	public static final Bias NONE = r -> r;

	private Biases() {}

	/**
	 * Makes sure biased ratios within bounds.
	 */
	public static Bias limiter(Bias bias) {
		return r -> {
			if (r == MAX_RATIO) return MAX_RATIO;
			if (r == MIN_RATIO) return MIN_RATIO;
			r = bias.bias(r);
			if (r < MIN_RATIO) return MIN_RATIO;
			if (r > MAX_RATIO) return MAX_RATIO;
			return r;
		};
	}

	/**
	 * Scales a partial bias starting at 0.
	 * If the given bias is not an increasing bias the result may be clipped.
	 */
	public static Bias partial(Bias bias, double len) {
		validateRange(len, MIN_RATIO, MAX_RATIO);
		if (len == MIN_RATIO) return DOWN;
		double max = bias.bias(len);
		if (max == MIN_RATIO) return UP;
		return limiter(r -> r = bias.bias(r * len) / max);
	}

	/**
	 * Wrapping bias offset.
	 */
	public static Bias offset(Bias bias, double offset) {
		return ratio -> {
			ratio = offset(ratio, offset);
			ratio = bias.bias(ratio);
			return offset(ratio, -offset);
		};
	}

	private static double offset(double ratio, double offset) {
		ratio = ratio - offset;
		while (ratio < MIN_RATIO) ratio += MAX_RATIO;
		while (ratio > MAX_RATIO) ratio -= MAX_RATIO;
		return ratio;
	}

	/**
	 * Inverts the bias.
	 */
	public static Bias inverse(Bias bias) {
		return r -> MAX_RATIO - bias.bias(MAX_RATIO - r);
	}

	/**
	 * Sequences the biases.
	 */
	public static Bias sequence(Bias...biases) {
		return sequence(Arrays.asList(biases));
	}

	/**
	 * Sequences the biases.
	 */
	public static Bias sequence(List<Bias> biases) {
		int n = biases.size();
		double partial = MAX_RATIO / n;
		return r -> {
			int i = (int)(r / partial);
			if (i >= n) i = n - 1;
			return biases.get(i).bias(r / partial) * partial;
		};
	}

}
