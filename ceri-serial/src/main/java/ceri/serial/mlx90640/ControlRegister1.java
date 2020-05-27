package ceri.serial.mlx90640;

import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.text.ToStringHelper;

public class ControlRegister1 {
	private static final IntAccessor.Typed<ControlRegister1> accessor =
		IntAccessor.typed(t -> t.value, (t, i) -> t.value = i);
	private static final IntAccessor.Typed<ControlRegister1> subPagesModeField =
		accessor.maskBits(0, 1);
	private static final IntAccessor.Typed<ControlRegister1> dataHoldField = //
		accessor.maskBits(2, 1);
	private static final IntAccessor.Typed<ControlRegister1> subPagesRepeatField =
		accessor.maskBits(3, 1);
	private static final FieldTranscoder.Typed<ControlRegister1, SubPage> subPageField =
		SubPage.xcoder.field(accessor.maskBits(4, 3));
	private static final FieldTranscoder.Typed<ControlRegister1, RefreshRate> refreshRateField =
		RefreshRate.xcoder.field(accessor.maskBits(7, 3));
	private static final FieldTranscoder.Typed<ControlRegister1, Resolution> resolutionField =
		Resolution.xcoder.field(accessor.maskBits(10, 2));
	private static final FieldTranscoder.Typed<ControlRegister1, ReadingPattern> patternField =
		ReadingPattern.xcoder.field(accessor.maskBits(12, 1));
	private static final IntAccessor.Typed<ControlRegister1> startMeasurementField =
		accessor.maskBits(15, 1);
	private int value;

	static ControlRegister1 of(int value) {
		return new ControlRegister1(value);
	}

	private ControlRegister1(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public ControlRegister1 subPagesMode(boolean enable) {
		subPagesModeField.set(this, enable ? 1 : 0);
		return this;
	}

	public boolean subPagesMode() {
		return subPagesModeField.get(this) != 0;
	}

	public ControlRegister1 dataHold(boolean enable) {
		dataHoldField.set(this, enable ? 1 : 0);
		return this;
	}

	public boolean dataHold() {
		return dataHoldField.get(this) != 0;
	}

	public ControlRegister1 subPagesRepeat(boolean enable) {
		subPagesRepeatField.set(this, enable ? 1 : 0);
		return this;
	}

	public boolean subPagesRepeat() {
		return subPagesRepeatField.get(this) != 0;
	}

	ControlRegister1 subPage(SubPage subPage) {
		subPageField.set(this, subPage);
		return this;
	}

	public SubPage subPage() {
		return subPageField.get(this);
	}

	public ControlRegister1 refreshRate(RefreshRate refreshRate) {
		refreshRateField.set(this, refreshRate);
		return this;
	}

	public RefreshRate refreshRate() {
		return refreshRateField.get(this);
	}

	public ControlRegister1 resolution(Resolution resolution) {
		resolutionField.set(this, resolution);
		return this;
	}

	public Resolution resolution() {
		return resolutionField.get(this);
	}

	public ControlRegister1 pattern(ReadingPattern pattern) {
		patternField.set(this, pattern);
		return this;
	}

	public ReadingPattern pattern() {
		return patternField.get(this);
	}

	ControlRegister1 startMeasurement(boolean enable) {
		startMeasurementField.set(this, enable ? 1 : 0);
		return this;
	}

	boolean startMeasurement() {
		return startMeasurementField.get(this) != 0;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("0x%04x", value),
			startMeasurement(), pattern(), resolution(), refreshRate(), subPage(), subPagesRepeat(),
			dataHold(), subPagesMode()).toString();
	}

}
