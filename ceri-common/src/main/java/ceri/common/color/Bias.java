package ceri.common.color;

import java.util.Arrays;
import java.util.List;
import ceri.common.math.Bound;
import ceri.common.math.Maths;

/**
 * Used to adjust fades between two values. Should return a biased ratio value 0.0 to 1.0 from a
 * given ratio value 0.0 to 1.0.
 */
public interface Bias {
	double MAX_RATIO = 1.0;
	/** No bias. */
	Bias NONE = r -> r;

	/**
	 * Common biases.
	 */
	enum Std implements Bias {
		none(NONE),
		/** Moves any value except min to max. */
		up(r -> r > 0 ? MAX_RATIO : 0),
		/** Moves any value except max to min. */
		down(r -> r < MAX_RATIO ? 0 : MAX_RATIO),
		/** Sine curve from 0 to +PI/2. */
		q1Sine(r -> Math.sin(r * Maths.PI_BY_2)),
		/** Transposed sine curve from -PI/2 to 0. */
		q4Sine(Bias.inverse(q1Sine)),
		/** Transposed sine curve from 0 to PI/2 followed by -PI/2 to 0. */
		q1q4Sine(Bias.sequence(q1Sine, q4Sine)),
		/** Transposed sine curve from -PI/2 to +PI/2. */
		q4q1Sine(r -> (1.0 + Math.sin((r * Math.PI) - Maths.PI_BY_2)) / 2.0),
		/** Transposed circle arc for x > 0, y < 0. */
		q2Circle(r -> MAX_RATIO - Math.sqrt(MAX_RATIO - (r * r))),
		/** Transposed circle arc for x < 0, y > 0. */
		q4Circle(Bias.inverse(q2Circle)),
		/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
		q2q4Circle(Bias.sequence(q2Circle, q4Circle)),
		/** Transposed circle arcs for x < 0, y > 0, followed by x > 0, y < 0. */
		q4q2Circle(Bias.sequence(q4Circle, q2Circle));

		private final Bias bias;

		private Std(Bias bias) {
			this.bias = bias;
		}

		@Override
		public double bias(double ratio) {
			return bias.bias(ratio);
		}
	}

	/**
	 * Integer bias, returning a value 0 to 255 from a given value 0 to 255.
	 */
	public interface Int {
		/** No bias. */
		Int NONE = v -> v;

		enum Std implements Int {
			none(NONE),
			/** Moves any value except min to max. */
			up(v -> v > 0 ? Colors.MAX_VALUE : 0),
			/** Moves any value except max to min. */
			down(v -> v < Colors.MAX_VALUE ? 0 : Colors.MAX_VALUE),
			/** Sine curve from 0 to +PI/2. */
			q1Sine(Int.from(Bias.Std.q1Sine)),
			/** Transposed sine curve from -PI/2 to 0. */
			q4Sine(Int.inverse(q1Sine)),
			/** Transposed sine curve from 0 to PI/2 followed by -PI/2 to 0. */
			q1q4Sine(Int.from(Bias.Std.q1q4Sine)), // more time spent in the middle
			/** Transposed sine curve from -PI/2 to +PI/2. */
			q4q1Sine(Int.from(Bias.Std.q4q1Sine)), // more time spent at the edges
			/** Transposed circle arc for x > 0, y < 0. */
			q2Circle(Int.from(Bias.Std.q2Circle)),
			/** Transposed circle arc for x < 0, y > 0. */
			q4Circle(Int.inverse(q2Circle)),
			/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
			q2q4Circle(Int.from(Bias.Std.q2q4Circle)),
			/** Transposed circle arcs for x > 0, y < 0, followed by x < 0, y > 0. */
			q4q2Circle(Int.from(Bias.Std.q4q2Circle));

			private final Int bias;

			private Std(Int bias) {
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
		default Int limit() {
			return limiter(this);
		}

		/**
		 * Shifts the bias by an offset 0 to MAX_VALUE. Smoother if start and end gradients of the given
		 * bias are equal.
		 */
		default Int offset(int offset) {
			return offset(this, offset);
		}

		/**
		 * Inverts the bias by flipping both axes.
		 */
		default Int invert() {
			return inverse(this);
		}

		/**
		 * Converts a bias into a lookup table int bias.
		 */
		static Int from(Bias bias) {
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
		private static Int limiter(Int bias) {
			return v -> Maths.limit(bias.bias(Maths.limit(v, 0, Colors.MAX_VALUE)), 0,
				Colors.MAX_VALUE);
		}

		/**
		 * Wrapping bias offset. Smoother if start and end gradients of the given bias are equal.
		 */
		private static Int offset(Int bias, int offset) {
			int start = bias.bias(offset);
			return limiter(v -> offset(bias.bias(offset(v, offset)), -start));
		}

		/**
		 * Inverts the bias by flipping both axes.
		 */
		private static Int inverse(Int bias) {
			return v -> Colors.MAX_VALUE - bias.bias(Colors.MAX_VALUE - v);
		}

		private static int offset(int value, int offset) {
			return Maths.periodicLimit(value + offset, Colors.MAX_VALUE + 1, Bound.Type.exc);
		}
	}
	
	/**
	 * Provides a biased value of the given ratio.
	 */
	double bias(double ratio);

	/**
	 * Limits bias between 0 and 1.
	 */
	default Bias limit() {
		return limiter(this);
	}

	/**
	 * Scales to a partial bias starting at 0. If this bias is not increasing, the result may be
	 * clipped.
	 */
	default Bias partial(double len) {
		return partial(this, len);
	}

	/**
	 * Shifts the bias by an offset. Smoother if start and end gradients of the given bias are
	 * equal.
	 */
	default Bias offset(double offset) {
		return offset(this, offset);
	}

	/**
	 * Inverts the bias by flipping both axes.
	 */
	default Bias invert() {
		return inverse(this);
	}

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
		Colors.validateRatio(len, "len");
		if (len == 0) return Std.down;
		double max = bias.bias(len);
		if (max == 0) return Std.up;
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
	 * Inverts the bias by flipping both axes.
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
		return Maths.periodicLimit(ratio + offset, MAX_RATIO, Bound.Type.inc);
	}
}
