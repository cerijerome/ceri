package ceri.common.unit;

import java.util.Objects;
import ceri.common.function.Functions;

/**
 * Encapsulates a temperature with scale and value.
 */
public record Temperature(Scale scale, double value) {
	public static final Temperature ZERO_C = Scale.celsius.temperature(0);
	public static final Temperature ZERO_K = Scale.kelvin.temperature(0);
	public static final Temperature ZERO_F = Scale.fahrenheit.temperature(0);
	public static final Temperature ZERO_R = Scale.rankine.temperature(0);

	/**
	 * Encapsulates temperature scale
	 */
	public enum Scale {
		kelvin("K", k -> k, Conversion.kToC, Conversion.kToR, Conversion.kToF),
		celsius("\u00b0C", Conversion.cToK, c -> c, Conversion.cToR, Conversion.cToF),
		rankine("R", Conversion.rToK, Conversion.rToC, r -> r, Conversion.rToF),
		fahrenheit("\u00b0F", Conversion.fToK, Conversion.fToC, Conversion.fToR, f -> f);

		public final String symbol;
		private final Functions.DoubleOperator toK;
		private final Functions.DoubleOperator toC;
		private final Functions.DoubleOperator toF;
		private final Functions.DoubleOperator toR;

		private static class Conversion {
			private static final double c0ToK = 273.15;
			private static final double f0ToR = 459.67;
			private static final double c0ToF = 32.0;
			private static final double cfRatio = 1.8;
			public static final Functions.DoubleOperator cToK = c -> c + c0ToK;
			public static final Functions.DoubleOperator cToF = c -> c0ToF + (c * cfRatio);
			public static final Functions.DoubleOperator fToC = f -> (f - c0ToF) / cfRatio;
			public static final Functions.DoubleOperator fToR = f -> f + f0ToR;
			public static final Functions.DoubleOperator fToK = and(fToC, cToK);
			public static final Functions.DoubleOperator cToR = and(cToF, fToR);
			public static final Functions.DoubleOperator kToC = k -> k - c0ToK;
			public static final Functions.DoubleOperator kToF = and(kToC, cToF);
			public static final Functions.DoubleOperator kToR = and(kToF, fToR);
			public static final Functions.DoubleOperator rToF = r -> r - f0ToR;
			public static final Functions.DoubleOperator rToC = and(rToF, fToC);
			public static final Functions.DoubleOperator rToK = and(rToC, cToK);

			private Conversion() {}

			private static Functions.DoubleOperator and(Functions.DoubleOperator first,
				Functions.DoubleOperator second) {
				return f -> second.applyAsDouble(first.applyAsDouble(f));
			}
		}

		private Scale(String symbol, Functions.DoubleOperator toK, Functions.DoubleOperator toC,
			Functions.DoubleOperator toR, Functions.DoubleOperator toF) {
			this.symbol = symbol;
			this.toK = toK;
			this.toC = toC;
			this.toR = toR;
			this.toF = toF;
		}

		public Temperature temperature(double value) {
			return new Temperature(this, value);
		}

		public double toK(double t) {
			return toK.applyAsDouble(t);
		}

		public double toC(double t) {
			return toC.applyAsDouble(t);
		}

		public double toR(double t) {
			return toR.applyAsDouble(t);
		}

		public double toF(double t) {
			return toF.applyAsDouble(t);
		}

		public double to(Scale scale, double t) {
			Objects.requireNonNull(scale);
			return switch (scale) {
				case kelvin -> toK(t);
				case rankine -> toR(t);
				case fahrenheit -> toF(t);
				default -> toC(t);
			};
		}
	}

	public Temperature to(Scale scale) {
		return new Temperature(scale, this.scale.to(scale, value));
	}

	@Override
	public String toString() {
		return String.valueOf(value) + scale.symbol;
	}
}
