package ceri.serial.comm.util;

import java.nio.file.Path;
import java.util.Objects;
import ceri.common.property.TypedProperties;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialProperties extends TypedProperties.Ref {
	private static final String PORT_KEY = "port";
	private static final String LOCATOR_KEY = "locator";
	private static final String PATH_KEY = "path";
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

	public SerialProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public PortSupplier portSupplier() {
		var portSupplier = portDirectSupplier();
		if (portSupplier == null) portSupplier = portPatternSupplier();
		if (portSupplier == null) portSupplier = portLocationIdSupplier();
		return Objects.requireNonNullElse(portSupplier, PortSupplier.NULL);
	}

	public SerialConfig config() {
		SerialConfig.Builder b = SerialConfig.builder().params(params());
		parse(FLOW_CONTROL_KEY).split().asEnums(FlowControl.class).accept(b::flowControl);
		parse(IN_BUFFER_SIZE_KEY).asInt().accept(b::inBufferSize);
		parse(OUT_BUFFER_SIZE_KEY).asInt().accept(b::outBufferSize);
		return b.build();
	}

	private PortSupplier portDirectSupplier() {
		return parse(PORT_KEY).to(PortSupplier::fixed);
	}

	private PortSupplier portPatternSupplier() {
		String pattern = parse(PORT_KEY, LOCATOR_KEY, PATTERN_KEY).get();
		Integer index = parse(PORT_KEY, LOCATOR_KEY, INDEX_KEY).toInt();
		if (pattern != null) return portLocator().portSupplier(pattern, index == null ? 0 : index);
		if (index != null) return portLocator().usbPortSupplier(index);
		return null;
	}

	private SerialPortLocator portLocator() {
		return SerialPortLocator.of(parse(PORT_KEY, LOCATOR_KEY, PATH_KEY).to(Path::of));
	}

	private PortSupplier portLocationIdSupplier() {
		return parse(PORT_KEY, LOCATOR_KEY, ID_KEY).asInt().to(MacUsbLocator.of()::portSupplier);
	}

	private SerialParams params() {
		SerialParams.Builder b = SerialParams.builder();
		parse(BAUD_KEY).asInt().accept(b::baud);
		parse(DATA_BITS_KEY).asInt().as(DataBits::from).accept(b::dataBits);
		parse(STOP_BITS_KEY).asDouble().as(StopBits::fromBits).accept(b::stopBits);
		parse(PARITY_KEY).asEnum(Parity.class).accept(b::parity);
		return b.build();
	}
}
