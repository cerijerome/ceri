package ceri.common.math;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.DoubleUnaryOperator;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Provides a lookup for non-reversible functions. Add function values to the lookup, or auto-load
 * based on range and number of steps. Values between points will be linearly approximated.
 */
public class ReverseFunction {
	public final TreeMap<Double, Double> values;

	public static ReverseFunction create(double x0, double x1, int steps, DoubleUnaryOperator fn) {
		Builder b = builder();
		for (int i = 0; i <= steps; i++) {
			double x = steps == 0 ? x0 : x0 + (i * (x1 - x0) / steps);
			double y = fn.applyAsDouble(x);
			//noinspection SuspiciousNameCombination
			b.add(y, x);
		}
		return b.build();
	}

	public static class Builder {
		final Map<Double, Double> values = new LinkedHashMap<>();

		Builder() {}

		public Builder add(double x, double y) {
			values.put(x, y);
			return this;
		}

		public Builder add(Map<Double, Double> values) {
			this.values.putAll(values);
			return this;
		}

		public ReverseFunction build() {
			return new ReverseFunction(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	ReverseFunction(Builder builder) {
		values = new TreeMap<>(builder.values);
	}

	public double x(double y) {
		Map.Entry<Double, Double> floor = values.floorEntry(y);
		Map.Entry<Double, Double> ceiling = values.ceilingEntry(y);
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
		return HashCoder.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ReverseFunction)) return false;
		ReverseFunction other = (ReverseFunction) obj;
		if (!EqualsUtil.equals(values, other.values)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, values).toString();
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
