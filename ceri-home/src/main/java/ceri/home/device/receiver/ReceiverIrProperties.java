package ceri.home.device.receiver;

import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.home.device.common.IrProperties;
import ceri.home.io.pcirlinc.PcIrLincButton;

public class ReceiverIrProperties extends BaseProperties {
	private static final String INPUT = "input";
	private static final String SURROUND_MODE = "surroundMode";
	private static final String BUTTON = "button";
	private final IrProperties irProperties;

	public ReceiverIrProperties(Properties properties, String prefix) {
		super(properties, prefix);
		irProperties = new IrProperties(properties, prefix);
	}

	public IrProperties common() {
		return irProperties;
	}

	public PcIrLincButton inputButton(int index) {
		String value = value(INPUT, String.valueOf(index), BUTTON);
		return PcIrLincButton.valueOf(value);
	}

	public PcIrLincButton surroundModeButton(int index) {
		String value = value(SURROUND_MODE, String.valueOf(index), BUTTON);
		return PcIrLincButton.valueOf(value);
	}

}
