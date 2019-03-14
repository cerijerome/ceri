package ceri.serial.javax;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;

public class SerialPortProperties extends BaseProperties {
	private static final String BAUD_RATE_KEY = "baud.rate";
	private static final String DATA_BITS_KEY = "data.bits";
	private static final String STOP_BITS_KEY = "stop.bits";
	private static final String PARITY_KEY = "parity";

	public SerialPortProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public SerialPortParams params() {
		SerialPortParams.Builder params = SerialPortParams.builder();
		safeAccept(baudRate(), params::baudRate);
		safeAccept(dataBits(), params::dataBits);
		safeAccept(stopBits(), params::stopBits);
		safeAccept(parity(), params::parity);
		return params.build();
	}

	private Integer baudRate() {
		return intValue(BAUD_RATE_KEY);
	}

	private DataBits dataBits() {
		return valueFromInt(DataBits::from, DATA_BITS_KEY);
	}

	private StopBits stopBits() {
		return valueFromDouble(StopBits::fromActual, STOP_BITS_KEY);
	}

	private Parity parity() {
		return enumValue(Parity.class, PARITY_KEY);
	}

}
