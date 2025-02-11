package ceri.serial.comm.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.jna.util.ThreadBuffers;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

/**
 * Encapsulates the dynamic properties of a serial connector.
 */
public class SerialConfig {
	public static final SerialConfig DEFAULT = new Builder().build();
	public final SerialParams params;
	public final Set<FlowControl> flowControl;
	public final int inBufferSize;
	public final int outBufferSize;

	public static SerialConfig of(int baud) {
		return of(SerialParams.of(baud));
	}

	public static SerialConfig of(SerialParams params) {
		return builder().params(params).build();
	}

	public static class Properties extends TypedProperties.Ref {
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

		public Properties(TypedProperties properties, String... groups) {
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
			if (pattern != null)
				return portLocator().portSupplier(pattern, index == null ? 0 : index);
			if (index != null) return portLocator().usbPortSupplier(index);
			return null;
		}

		private PortSupplier.Locator portLocator() {
			return PortSupplier.locator(parse(PORT_KEY, LOCATOR_KEY, PATH_KEY).to(Path::of));
		}

		private PortSupplier portLocationIdSupplier() {
			return parse(PORT_KEY, LOCATOR_KEY, ID_KEY).asInt()
				.to(MacUsbLocator.of()::portSupplier);
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

	public static class Builder {
		// need to keep state in multi-threaded context
		volatile SerialParams params = SerialParams.DEFAULT;
		final Set<FlowControl> flowControl = ConcurrentHashMap.newKeySet();
		volatile int inBufferSize = ThreadBuffers.SIZE_DEF;
		volatile int outBufferSize = ThreadBuffers.SIZE_DEF;

		Builder() {}

		public Builder params(SerialParams params) {
			this.params = params;
			return this;
		}

		public Builder flowControl(FlowControl... flowControl) {
			return flowControl(Arrays.asList(flowControl));
		}

		public Builder flowControl(Collection<FlowControl> flowControl) {
			this.flowControl.clear();
			this.flowControl.addAll(flowControl);
			return this;
		}

		public Builder inBufferSize(int inBufferSize) {
			this.inBufferSize = inBufferSize;
			return this;
		}

		public Builder outBufferSize(int outBufferSize) {
			this.outBufferSize = outBufferSize;
			return this;
		}

		public SerialConfig build() {
			return new SerialConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(SerialConfig config) {
		return builder().params(config.params).flowControl(config.flowControl)
			.inBufferSize(config.inBufferSize).outBufferSize(config.outBufferSize);
	}

	SerialConfig(Builder builder) {
		params = builder.params;
		flowControl = Set.copyOf(builder.flowControl);
		inBufferSize = builder.inBufferSize;
		outBufferSize = builder.outBufferSize;
	}

	/**
	 * Override serial params.
	 */
	public SerialConfig replace(SerialParams params) {
		if (params == null || this.params.equals(params)) return this;
		return builder(this).params(params).build();
	}

	public void applyTo(Serial serial) throws IOException {
		serial.inBufferSize(inBufferSize);
		serial.outBufferSize(outBufferSize);
		serial.flowControl(flowControl);
		serial.params(params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(params, flowControl, inBufferSize, outBufferSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SerialConfig other)) return false;
		return Objects.equals(params, other.params)
			&& Objects.equals(flowControl, other.flowControl) && inBufferSize == other.inBufferSize
			&& outBufferSize == other.outBufferSize;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, params, flowControl, inBufferSize, outBufferSize);
	}
}