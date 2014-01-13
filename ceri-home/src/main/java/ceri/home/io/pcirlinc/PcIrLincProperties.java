package ceri.home.io.pcirlinc;

import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.util.PrimitiveUtil;

public class PcIrLincProperties extends BaseProperties {
	private static final String SERIAL_PORT = "serialPort";
	private static final String BAUD = "baud";
	private static final String TIMEOUT_MS = "timeoutMs";
	private static final String DELAY_MS = "delayMs";
	private static final String RESPONSE_WAIT_MS = "responseWaitMs";
	private static final String SERIAL_PORT_DEFAULT = "COM1";
	private static final int BAUD_DEFAULT = 19200;
	private static final int TIMEOUT_MS_DEFAULT = 2000;
	private static final int DELAY_MS_DEFAULT = 20;
	private static final int RESPONSE_WAIT_MS_DEFAULT = 200;

	public PcIrLincProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public String serialPort() {
		String serialPort = value(SERIAL_PORT);
		if (serialPort == null) serialPort = SERIAL_PORT_DEFAULT;
		return serialPort;
	}

	public int baud() {
		return PrimitiveUtil.valueOf(value(BAUD), BAUD_DEFAULT);
	}

	public int timeoutMs() {
		return PrimitiveUtil.valueOf(value(TIMEOUT_MS), TIMEOUT_MS_DEFAULT);
	}

	public int delayMs() {
		return PrimitiveUtil.valueOf(value(DELAY_MS), DELAY_MS_DEFAULT);
	}

	public int responseWaitMs() {
		return PrimitiveUtil.valueOf(value(RESPONSE_WAIT_MS), RESPONSE_WAIT_MS_DEFAULT);
	}

}
