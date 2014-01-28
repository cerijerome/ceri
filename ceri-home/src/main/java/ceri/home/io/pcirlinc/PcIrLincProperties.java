package ceri.home.io.pcirlinc;

import java.util.Properties;
import ceri.common.property.BaseProperties;

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
		return stringValue(SERIAL_PORT_DEFAULT, SERIAL_PORT);
	}

	public int baud() {
		return intValue(BAUD_DEFAULT, BAUD);
	}

	public int timeoutMs() {
		return intValue(TIMEOUT_MS_DEFAULT, TIMEOUT_MS);
	}

	public int delayMs() {
		return intValue(DELAY_MS_DEFAULT, DELAY_MS);
	}

	public int responseWaitMs() {
		return intValue(RESPONSE_WAIT_MS_DEFAULT, RESPONSE_WAIT_MS);
	}

}
