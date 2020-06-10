package ceri.serial.mlx90640;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.mlx90640.register.ReadingPattern;
import ceri.serial.mlx90640.register.RefreshRate;
import ceri.serial.mlx90640.register.Resolution;

/**
 * Configuration for continuously extracting a temperature grid from a MLX90640 device.
 */
public class MlxRunnerConfig {
	public final RefreshRate refreshRate;
	public final Resolution resolution;
	public final ReadingPattern pattern;
	public final double emissivity;
	public final Double tr;

	public static MlxRunnerConfig of() {
		return builder().build();
	}
	
	public static class Builder {
		RefreshRate refreshRate = RefreshRate._4Hz;
		Resolution resolution = Resolution._18bit;
		ReadingPattern pattern = ReadingPattern.chess;
		double emissivity = 1.0;
		Double tr = null;

		Builder() {}

		public Builder refreshRate(RefreshRate refreshRate) {
			this.refreshRate = refreshRate;
			return this;
		}

		public Builder resolution(Resolution resolution) {
			this.resolution = resolution;
			return this;
		}

		public Builder pattern(ReadingPattern pattern) {
			this.pattern = pattern;
			return this;
		}

		public Builder emissivity(double emissivity) {
			this.emissivity = emissivity;
			return this;
		}

		/**
		 * Sets an explicit reflective temperature. If left as null, it uses the calculated ambient
		 * temperature (ta) with a fixed -8C offset.
		 */
		public Builder tr(double tr) {
			this.tr = tr;
			return this;
		}

		public MlxRunnerConfig build() {
			return new MlxRunnerConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	MlxRunnerConfig(Builder builder) {
		refreshRate = builder.refreshRate;
		resolution = builder.resolution;
		pattern = builder.pattern;
		emissivity = builder.emissivity;
		tr = builder.tr;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(refreshRate, resolution, pattern, emissivity, tr);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MlxRunnerConfig)) return false;
		MlxRunnerConfig other = (MlxRunnerConfig) obj;
		if (!EqualsUtil.equals(refreshRate, other.refreshRate)) return false;
		if (!EqualsUtil.equals(resolution, other.resolution)) return false;
		if (!EqualsUtil.equals(pattern, other.pattern)) return false;
		if (!EqualsUtil.equals(emissivity, other.emissivity)) return false;
		if (!EqualsUtil.equals(tr, other.tr)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, refreshRate, resolution, pattern, emissivity, tr)
			.toString();
	}

}
