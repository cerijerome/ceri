package ceri.serial.mlx90640;

import java.util.Objects;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.text.ToStringHelper;

public class I2cConfigRegister implements Register {
	private static final int START_SYNC = 0x30;
	private static final IntAccessor.Typed<I2cConfigRegister> accessor =
		IntAccessor.typed(t -> t.value, (t, i) -> t.value = i);
	private static final IntAccessor.Typed<I2cConfigRegister> fmPlusEnabledField =
		accessor.maskBits(0, 1);
	private static final FieldTranscoder.Typed<I2cConfigRegister, ThresholdMode> thresholdModeField =
		ThresholdMode.xcoder.field(accessor.maskBits(1, 1));
	private static final IntAccessor.Typed<I2cConfigRegister> currentLimitField =
		accessor.maskBits(2, 1);
	private int value;

	public static I2cConfigRegister startSync() {
		return new I2cConfigRegister(START_SYNC);
	}

	static I2cConfigRegister of(int value) {
		return new I2cConfigRegister(value);
	}

	private I2cConfigRegister(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}

	public I2cConfigRegister fmPlusEnabled(boolean enabled) {
		fmPlusEnabledField.set(this, enabled ? 0 : 1); // opposite!
		return this;
	}

	public boolean fmPlusEnabled() {
		return fmPlusEnabledField.get(this) == 0; // opposite!
	}

	public I2cConfigRegister thresholdMode(ThresholdMode thresholdMode) {
		thresholdModeField.set(this, thresholdMode);
		return this;
	}

	public ThresholdMode thresholdMode() {
		return thresholdModeField.get(this);
	}

	public I2cConfigRegister currentLimit(boolean enabled) {
		currentLimitField.set(this, enabled ? 0 : 1); // opposite!
		return this;
	}

	public boolean currentLimit() {
		return currentLimitField.get(this) == 0; // opposite!
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof I2cConfigRegister)) return false;
		I2cConfigRegister other = (I2cConfigRegister) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("0x%04x", value), currentLimit(),
			thresholdMode(), fmPlusEnabled()).toString();
	}

}
