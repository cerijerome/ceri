package ceri.common.color;

import ceri.common.math.Bound;
import ceri.common.math.Maths;

/**
 * Used to adjust fades between two values. Should return a biased value 0 to 255 from a given value
 * 0 to 255.
 */
public interface IntBias {
	/** No bias. */
	IntBias NONE = v -> v;

	enum Std implements IntBias {
		none(NONE),
		/** Moves any value except min to max. */
		up(v -> v > 0 ? Colors.MAX_VALUE : 0),
		/** Moves any value except max to min. */
		down(v -> v < Colors.MAX_VALUE ? 0 : Colors.MAX_VALUE),
		/** Sine curve from 0 to +PI/2. */
		q1Sine(IntBias.from(Bias.Std.q1Sine)),
		/** Transposed sine curve from -PI/2 to 0. */
		q4Sine(IntBias.inverse(q1Sine)),
		/** Transposed sine curve from 0 to PI/2 followed by -PI/2 to 0. */
		q1q4Sine(IntBias.from(Bias.Std.q1q4Sine)), // more time spent in the middle
		/** Transposed sine curve from -PI/2 to +PI/2. */
		q4q1Sine(IntBias.from(Bias.Std.q4q1Sine)), // more time spent at the edges
		/** Transposed circle arc for x > 0, y < 0. */
		q2Circle(IntBias.from(Bias.Std.q2Circle)),
		/** Transposed circle arc for x < 0, y > 0. */
		q4Circle(IntBias.inverse(q2Circle)),
		/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
		q2q4Circle(IntBias.from(Bias.Std.q2q4Circle)),
		/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
		q4q2Circle(IntBias.from(Bias.Std.q4q2Circle));

		private final IntBias bias;

		private Std(IntBias bias) {
			this.bias = bias;
		}

		@Override
		public int bias(int value) {
			return bias.bias(value);
		}
	}

	/**
	 * Provides a biased value of the given ratio.
	 */
	int bias(int value);

	/**
	 * Limits bias between 0 and MAX_VALUE (255).
	 */
	default IntBias limit() {
		return limiter(this);
	}

	/**
	 * Shifts the bias by an offset 0 to MAX_VALUE. Smoother if start and end gradients of the given
	 * bias are equal.
	 */
	default IntBias offset(int offset) {
		return offset(this, offset);
	}

	/**
	 * Inverts the bias by flipping both axes.
	 */
	default IntBias invert() {
		return inverse(this);
	}

	/**
	 * Converts a bias into a lookup table int bias.
	 */
	static IntBias from(Bias bias) {
		int[] lookup = new int[Colors.MAX_VALUE + 1];
		for (int i = 0; i < lookup.length; i++)
			lookup[i] = Maths.limit(
				Maths.intRound(bias.bias((double) i / Colors.MAX_VALUE) * Colors.MAX_VALUE), 0,
				Colors.MAX_VALUE);
		return v -> lookup[Maths.limit(v, 0, Colors.MAX_VALUE)];
	}

	/**
	 * Makes sure biased ratios within bounds.
	 */
	private static IntBias limiter(IntBias bias) {
		return v -> Maths.limit(bias.bias(Maths.limit(v, 0, Colors.MAX_VALUE)), 0,
			Colors.MAX_VALUE);
	}

	/**
	 * Wrapping bias offset. Smoother if start and end gradients of the given bias are equal.
	 */
	private static IntBias offset(IntBias bias, int offset) {
		int start = bias.bias(offset);
		return limiter(v -> offset(bias.bias(offset(v, offset)), -start));
	}

	/**
	 * Inverts the bias by flipping both axes.
	 */
	private static IntBias inverse(IntBias bias) {
		return v -> Colors.MAX_VALUE - bias.bias(Colors.MAX_VALUE - v);
	}

	private static int offset(int value, int offset) {
		return Maths.periodicLimit(value + offset, Colors.MAX_VALUE + 1, Bound.Type.exc);
	}
}
