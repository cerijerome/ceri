package ceri.serial.comm.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ceri.common.property.BaseProperties;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialProperties extends BaseProperties {
	private static final String PORT_KEY = "port";
	private static final String LOCATOR_KEY = "locator";
	private static final String PATTERN_KEY = "pattern";
	private static final String INDEX_KEY = "index";
	private static final String ID_KEY = "id";
	private static final String BAUD_KEY = "baud";
	private static final String DATA_BITS_KEY = "data.bits";
	private static final String STOP_BITS_KEY = "stop.bits";
	private static final String PARITY_KEY = "parity";
	private static final String FLOW_CONTROL_KEY = "flow.control";
	private static final String IN_BUFFER_SIZE_KEY = "in.buffer.size";
	private static final String OUT_BUFFER_SIZE_KEY = "out.buffer.size";

	public SerialProperties(BaseProperties properties, String... prefix) {
		super(properties, prefix);
	}

	public PortSupplier portSupplier() {
		var portSupplier = portDirectSupplier();
		if (portSupplier == null) portSupplier = portPatternSupplier();
		if (portSupplier == null) portSupplier = portLocationIdSupplier();
		return Objects.requireNonNullElse(portSupplier, PortSupplier.NULL);
	}

	public SerialConfig config() {
		SerialConfig.Builder config = SerialConfig.builder();
		config.params(params()).flowControl(flowControl());
		safeAccept(inBufferSize(), config::inBufferSize);
		safeAccept(outBufferSize(), config::outBufferSize);
		return config.build();
	}

	private PortSupplier portDirectSupplier() {
		String port = port();
		return port != null ? PortSupplier.fixed(port) : null;
	}

	private PortSupplier portPatternSupplier() {
		String pattern = portLocatorPattern();
		Integer index = portLocatorIndex();
		if (pattern != null)
			return SerialPortLocator.of().portSupplier(pattern, index == null ? 0 : index);
		if (index != null) return SerialPortLocator.of().usbPortSupplier(index);
		return null;
	}

	private PortSupplier portLocationIdSupplier() {
		Integer locatorId = portLocatorId();
		return locatorId != null ? MacUsbLocator.of().portSupplier(locatorId) : null;
	}

	private String port() {
		return value(PORT_KEY);
	}

	private String portLocatorPattern() {
		return value(PORT_KEY, LOCATOR_KEY, PATTERN_KEY);
	}

	private Integer portLocatorIndex() {
		return intValue(PORT_KEY, LOCATOR_KEY, INDEX_KEY);
	}

	private Integer portLocatorId() {
		return intValue(PORT_KEY, LOCATOR_KEY, ID_KEY);
	}

	private SerialParams params() {
		SerialParams.Builder params = SerialParams.builder();
		safeAccept(baud(), params::baud);
		safeAccept(dataBits(), params::dataBits);
		safeAccept(stopBits(), params::stopBits);
		safeAccept(parity(), params::parity);
		return params.build();
	}

	private Set<FlowControl> flowControl() {
		return Set.copyOf(enumValues(FlowControl.class, List.of(), FLOW_CONTROL_KEY));
	}

	private Integer inBufferSize() {
		return intValue(IN_BUFFER_SIZE_KEY);
	}

	private Integer outBufferSize() {
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
