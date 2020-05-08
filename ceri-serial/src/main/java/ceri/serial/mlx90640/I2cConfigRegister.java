package ceri.serial.mlx90640;

import ceri.common.data.ByteUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class I2cConfigRegister {
	public static final I2cConfigRegister DEFAULT = builder().build();
	private static final int CURRENT_LIMIT_BIT = 2;
	private static final int THRESHOLD_LEVEL_BIT = 1;
	private static final int FM_PLUS_MODE_BIT = 0;
	private static final int RESERVED_MASK = 0xfff0;
	public final boolean currentLimit;
	public final ThresholdMode thresholdMode;
	public final boolean fmPlusMode;
	public final int reserved; // preserve reserved bits

	/**
	 * Decode settings from integer value.
	 */
	public static I2cConfigRegister decode(int value) {
		return builder().currentLimit(!ByteUtil.bit(value, CURRENT_LIMIT_BIT))
			.thresholdMode(ThresholdMode.decode(value >>> THRESHOLD_LEVEL_BIT))
			.fmPlusMode(!ByteUtil.bit(value, FM_PLUS_MODE_BIT)).reserved(value).build();
	}

	public static class Builder {
		boolean currentLimit = true;
		ThresholdMode thresholdMode = ThresholdMode.normal;
		boolean fmPlusMode = true;
		int reserved = 0;

		Builder() {}

		public Builder currentLimit(boolean currentLimit) {
			this.currentLimit = currentLimit;
			return this;
		}

		public Builder thresholdMode(ThresholdMode thresholdMode) {
			this.thresholdMode = thresholdMode;
			return this;
		}

		public Builder fmPlusMode(boolean fmPlusMode) {
			this.fmPlusMode = fmPlusMode;
			return this;
		}

		public Builder reserved(int reserved) {
			this.reserved = reserved & RESERVED_MASK;
			return this;
		}

		public I2cConfigRegister build() {
			return new I2cConfigRegister(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	I2cConfigRegister(Builder builder) {
		currentLimit = builder.currentLimit;
		thresholdMode = builder.thresholdMode;
		fmPlusMode = builder.fmPlusMode;
		reserved = builder.reserved;
	}

	/**
	 * Encodes settings to an integer value.
	 */
	public int encode() {
		return ByteUtil.maskOfBitInt(!currentLimit, CURRENT_LIMIT_BIT) |
			(thresholdMode.encode() << THRESHOLD_LEVEL_BIT) |
			ByteUtil.maskOfBitInt(!fmPlusMode, FM_PLUS_MODE_BIT);
	}

	/**
	 * Returns a builder to modify settings.
	 */
	public Builder modify() {
		return builder().currentLimit(currentLimit).thresholdMode(thresholdMode)
			.fmPlusMode(fmPlusMode);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(currentLimit, thresholdMode, fmPlusMode, reserved);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof I2cConfigRegister)) return false;
		I2cConfigRegister other = (I2cConfigRegister) obj;
		if (currentLimit != other.currentLimit) return false;
		if (!EqualsUtil.equals(thresholdMode, other.thresholdMode)) return false;
		if (fmPlusMode != other.fmPlusMode) return false;
		if (reserved != other.reserved) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, currentLimit, thresholdMode, fmPlusMode,
			"0x" + Integer.toHexString(reserved)).toString();
	}

}
