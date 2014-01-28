package ceri.home.device.common;

import java.io.IOException;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.BasicUtil;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincButton;

public class IrSupport {
	private final IrProperties properties;
	private final PcIrLinc pcIrLinc;

	public IrSupport(IrProperties properties, PcIrLinc pcIrLinc) {
		this.properties = properties;
		this.pcIrLinc = pcIrLinc;
	}

	public void sendButton(PcIrLincButton button) {
		try {
			byte[] override = properties.buttonOverride(button);
			for (int i = properties.buttonRepeat(button); i > 0; i--) {
				if (override.length > 0) pcIrLinc.sendLearnedIr(override, 1);
				else pcIrLinc.sendPreset(properties.type(), properties.vendorCode(), button);
				BasicUtil.delay(properties.buttonDelayMs(button));
			}
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
