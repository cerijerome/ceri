package ceri.serial.comm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import ceri.common.text.ToString;
import ceri.jna.util.ThreadBuffers;

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

	public static class Builder {
		SerialParams params = SerialParams.DEFAULT;
		final Set<FlowControl> flowControl = new HashSet<>();
		int inBufferSize = ThreadBuffers.SIZE_DEF;
		int outBufferSize = ThreadBuffers.SIZE_DEF;

		Builder() {}

		public Builder params(SerialParams params) {
			this.params = params;
			return this;
		}

		public Builder flowControl(FlowControl... flowControl) {
			return flowControl(Arrays.asList(flowControl));
		}

		public Builder flowControl(Collection<FlowControl> flowControl) {
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