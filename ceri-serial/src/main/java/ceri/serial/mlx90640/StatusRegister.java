package ceri.serial.mlx90640;

import java.util.Objects;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.text.ToStringHelper;

public class StatusRegister implements Register {
	private static final int DATA_RESET = 0x30;
	private static final IntAccessor.Typed<StatusRegister> accessor =
		IntAccessor.typed(t -> t.value, (t, i) -> t.value = i);
	private static final FieldTranscoder.Typed<StatusRegister, SubPage> lastSubPageField =
		SubPage.xcoder.field(accessor.maskBits(0, 3));
	private static final IntAccessor.Typed<StatusRegister> dataAvailableField =
		accessor.maskBits(3, 1);
	private static final IntAccessor.Typed<StatusRegister> overwriteRamField =
		accessor.maskBits(4, 1);
	private static final IntAccessor.Typed<StatusRegister> startOfMeasurementField =
		accessor.maskBits(5, 1);
	private int value;

	public static StatusRegister dataReset() {
		return new StatusRegister(DATA_RESET);
	}

	static StatusRegister of(int value) {
		return new StatusRegister(value);
	}

	private StatusRegister(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}

	public SubPage lastSubPage() {
		return lastSubPageField.get(this);
	}

	public int lastSubPageNumber() {
		return lastSubPageField.accessor().get(this) & 1;
	}

	public boolean dataAvailable() {
		return dataAvailableField.get(this) != 0;
	}

	public StatusRegister dataAvailable(boolean available) {
		dataAvailableField.set(this, available ? 1 : 0);
		return this;
	}

	public boolean overwriteRam() {
		return overwriteRamField.get(this) != 0;
	}

	public StatusRegister overwriteRam(boolean enable) {
		overwriteRamField.set(this, enable ? 1 : 0);
		return this;
	}

	public boolean startMeasurement() {
		return startOfMeasurementField.get(this) != 0;
	}

	public StatusRegister startMeasurement(boolean start) {
		startOfMeasurementField.set(this, start ? 1 : 0);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof StatusRegister)) return false;
		StatusRegister other = (StatusRegister) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("0x%04x", value),
			startMeasurement(), overwriteRam(), dataAvailable(), lastSubPage()).toString();
	}

}
