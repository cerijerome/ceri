package ceri.serial.mlx90640;

import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.text.ToStringHelper;

public class StatusRegister {
	private static final int START_SYNC = 0x30;
	private static final IntAccessor.Typed<StatusRegister> accessor =
		IntAccessor.typed(t -> t.value, (t, i) -> t.value = i);
	private static final FieldTranscoder.Typed<StatusRegister, SubPage> subPageField =
		SubPage.xcoder.field(accessor.maskBits(0, 3));
	private static final IntAccessor.Typed<StatusRegister> dataAvailableField =
		accessor.maskBits(3, 1);
	private static final IntAccessor.Typed<StatusRegister> overwriteEnabledField =
		accessor.maskBits(4, 1);
	private int value;

	public static StatusRegister startSync() {
		return new StatusRegister(START_SYNC);
	}

	static StatusRegister of(int value) {
		return new StatusRegister(value);
	}

	private StatusRegister(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public boolean overwriteEnabled() {
		return overwriteEnabledField.get(this) != 0;
	}

	public boolean dataAvailable() {
		return dataAvailableField.get(this) != 0;
	}

	public SubPage lastSubPage() {
		return subPageField.get(this);
	}

	public int lastSubPageBit() {
		return subPageField.accessor().get(this) & 1;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("0x%04x", value),
			overwriteEnabled(), dataAvailable(), lastSubPage()).toString();
	}

}
