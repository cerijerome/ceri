package ceri.serial.ftdi.util;

import java.io.IOException;

/**
 * Encapsulates the dynamic properties of an Ftdi device.
 */
import java.util.Objects;
import ceri.common.text.ToString;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiConnector;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;

public class FtdiConfig {
	public static final FtdiConfig NULL = builder().build();
	private final FtdiBitMode bitMode;
	private final Integer baud;
	private final FtdiLineParams params;
	private final FtdiFlowControl flowControl;
	private final Integer latencyTimer;
	private final Integer readChunkSize;
	private final Integer writeChunkSize;

	public static class Builder {
		// need to keep state in multi-threaded context
		volatile FtdiBitMode bitMode = null;
		volatile Integer baud = null;
		volatile FtdiLineParams params = null;
		volatile FtdiFlowControl flowControl = null;
		volatile Integer latencyTimer = null;
		volatile Integer readChunkSize = null;
		volatile Integer writeChunkSize = null;

		Builder() {}

		public Builder bitMode(FtdiBitMode bitMode) {
			this.bitMode = bitMode;
			return this;
		}

		public Builder baud(Integer baud) {
			this.baud = baud;
			return this;
		}

		public Builder params(FtdiLineParams params) {
			this.params = params;
			return this;
		}

		public Builder flowControl(FtdiFlowControl flowControl) {
			this.flowControl = flowControl;
			return this;
		}

		public Builder latencyTimer(Integer latencyTimer) {
			this.latencyTimer = latencyTimer;
			return this;
		}

		public Builder readChunkSize(Integer readChunkSize) {
			this.readChunkSize = readChunkSize;
			return this;
		}

		public Builder writeChunkSize(Integer writeChunkSize) {
			this.writeChunkSize = writeChunkSize;
			return this;
		}

		public FtdiConfig build() {
			return new FtdiConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(FtdiConfig config) {
		return builder().bitMode(config.bitMode).baud(config.baud).params(config.params)
			.flowControl(config.flowControl).latencyTimer(config.latencyTimer)
			.readChunkSize(config.readChunkSize).writeChunkSize(config.writeChunkSize);
	}

	FtdiConfig(Builder builder) {
		bitMode = builder.bitMode;
		baud = builder.baud;
		params = builder.params;
		flowControl = builder.flowControl;
		latencyTimer = builder.latencyTimer;
		readChunkSize = builder.readChunkSize;
		writeChunkSize = builder.writeChunkSize;
	}

	public void apply(FtdiConnector ftdi) throws IOException {
		if (bitMode != null) ftdi.bitMode(bitMode);
		if (baud != null) ftdi.baud(baud);
		if (params != null) ftdi.lineParams(params);
		if (flowControl != null) ftdi.flowControl(flowControl);
		if (latencyTimer != null) ftdi.latencyTimer(latencyTimer);
		if (readChunkSize != null) ftdi.readChunkSize(readChunkSize);
		if (writeChunkSize != null) ftdi.writeChunkSize(writeChunkSize);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bitMode, baud, params, flowControl, latencyTimer, readChunkSize,
			writeChunkSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FtdiConfig other)) return false;
		if (!Objects.equals(bitMode, other.bitMode)) return false;
		if (!Objects.equals(baud, other.baud)) return false;
		if (!Objects.equals(params, other.params)) return false;
		if (!Objects.equals(flowControl, other.flowControl)) return false;
		if (!Objects.equals(latencyTimer, other.latencyTimer)) return false;
		if (!Objects.equals(readChunkSize, other.readChunkSize)) return false;
		if (!Objects.equals(writeChunkSize, other.writeChunkSize)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, bitMode, baud, params, flowControl, latencyTimer,
			readChunkSize, writeChunkSize);
	}
}
