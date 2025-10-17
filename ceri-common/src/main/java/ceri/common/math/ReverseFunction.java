package ceri.common.math;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.text.ToString;

/**
 * Provides a lookup for non-reversible functions. Add function values to the lookup, or auto-load
 * based on range and number of steps. Values between points will be linearly approximated.
 */
public class ReverseFunction {
	public final NavigableMap<Double, Double> values;

	/**
	 * Returns a new instance from x range, steps, and function.
	 */
	public static ReverseFunction from(double x0, double x1, int steps,
		Functions.DoubleOperator fn) {
		var b = builder();
		for (int i = 0; i <= steps; i++) {
			double x = steps == 0 ? x0 : x0 + (i * (x1 - x0) / steps);
			double y = fn.applyAsDouble(x);
			b.add(y, x);
		}
		return b.build();
	}

	/**
	 * A builder to constructor the reverse function.
	 */
	public static class Builder {
		final Map<Double, Double> values = Maps.link();

		Builder() {}

		/**
		 * Add a data point.
		 */
		public Builder add(double x, double y) {
			values.put(x, y);
			return this;
		}

		/**
		 * Add a data points.
		 */
		public Builder add(Map<Double, Double> values) {
			this.values.putAll(values);
			return this;
		}

		/**
		 * Builds the instance from data points.
		 */
		public ReverseFunction build() {
			return new ReverseFunction(this);
		}
	}

	/**
	 * Returns an instance builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	private ReverseFunction(Builder builder) {
		values = Maps.tree(builder.values);
	}

	/**
	 * Determines the approximate value of x from y.
	 */
	public double x(double y) {
		var floor = values.floorEntry(y);
		var ceiling = values.ceilingEntry(y);
		if (floor == null && ceiling != null) {
			floor = ceiling;
			ceiling = values.higherEntry(ceiling.getKey());
		} else if (ceiling == null && floor != null) {
			ceiling = floor;
			floor = values.lowerEntry(floor.getKey());
		}
		return x(y, floor, ceiling);
	}

	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof ReverseFunction other) && Objects.equals(values, other.values);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, values);
	}

	private double x(double y, Map.Entry<Double, Double> floor, Map.Entry<Double, Double> ceiling) {
		if (floor == null && ceiling == null) return Double.NaN;
		if (floor == null) return ceiling.getValue();
		if (ceiling == null) return floor.getValue();
		double y0 = floor.getKey();
		double y1 = ceiling.getKey();
		double x0 = floor.getValue();
		double x1 = ceiling.getValue();
		if (y0 == y1) return x0;
		return x0 + ((y - y0) * (x1 - x0) / (y1 - y0));
	}
}
