package ceri.home.device.common;

import java.util.Properties;
import ceri.common.collection.ArrayUtil;
import ceri.common.property.BaseProperties;
import ceri.common.util.PrimitiveUtil;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincButton;
import ceri.home.io.pcirlinc.PcIrLincType;

public final class IrProperties extends BaseProperties {
	private static final String TYPE = "type";
	private static final String VENDOR_CODE = "vendorCode";
	private static final String DELAY_MS = "delayMs";
	private static final String OVERRIDE = "override";
	private static final String REPEAT = "repeat";
	private static final String BUTTON = "button";

	public IrProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public PcIrLincType type() {
		String value = value(TYPE);
		return PcIrLincType.valueOf(value);
	}

	public short vendorCode() {
		return shortValue(VENDOR_CODE);
	}

	public int buttonDelayMs(PcIrLincButton button) {
		String value = value(DELAY_MS, button.name());
		if (value == null) value = value(DELAY_MS, BUTTON);
		return PrimitiveUtil.valueOf(value, 0);
	}

	public byte[] buttonOverride(PcIrLincButton button) {
		String value = value(OVERRIDE, button.name());
		if (value == null) return ArrayUtil.EMPTY_BYTE;
		return PcIrLinc.hexToBytes(value);
	}

	public int buttonRepeat(PcIrLincButton button) {
		return intValue(1, REPEAT, button.name());
	}

}
