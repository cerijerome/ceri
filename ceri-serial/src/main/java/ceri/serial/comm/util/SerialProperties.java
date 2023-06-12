package ceri.serial.comm;

import static ceri.common.function.FunctionUtil.safeAccept;
import java.util.List;
import java.util.Set;
import ceri.common.property.BaseProperties;

public class SerialProperties extends BaseProperties {
	private static final String PORT_KEY = "port";
	private static final String BAUD_KEY = "baud";
	private static final String DATA_BITS_KEY = "data.bits";
	private static final String STOP_BITS_KEY = "stop.bits";
	private static final String PARITY_KEY = "parity";
	private static final String FLOW_CONTROL_KEY = "flow.control";
	private static final String IN_BUFFER_SIZE_KEY = "in.buffer.size";
	private static final String OUT_BUFFER_SIZE_KEY = "out.buffer.size";

	public SerialProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public String port() {
		return value(PORT_KEY);
	}
	
	public SerialParams params() {
		SerialParams.Builder params = SerialParams.builder();
		safeAccept(baud(), params::baud);
		safeAccept(dataBits(), params::dataBits);
		safeAccept(stopBits(), params::stopBits);
		safeAccept(parity(), params::parity);
		return params.build();
	}

	public Set<FlowControl> flowControl() {
		return Set.copyOf(enumValues(FlowControl.class, List.of(), FLOW_CONTROL_KEY));
	}

	public Integer inBufferSize() {
		return intValue(IN_BUFFER_SIZE_KEY);
	}

	public Integer outBufferSize() {
		return intValue(OUT_BUFFER_SIZE_KEY);
	}

	private Integer baud() {
		return intValue(BAUD_KEY);
	}

	private DataBits dataBits() {
		return valueFromInt(DataBits::from, DATA_BITS_KEY);
	}

	private StopBits stopBits() {
		return valueFromDouble(StopBits::fromBits, STOP_BITS_KEY);
	}

	private Parity parity() {
		return enumValue(Parity.class, PARITY_KEY);
	}
}
