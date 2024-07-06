package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import static ceri.common.math.MathUtil.approxEqual;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiProperties extends Ref<BaseProperties> {
	private static final String FINDER_KEY = "finder";
	private static final String INTERFACE_KEY = "interface";
	private static final String BIT_KEY = "bit";
	private static final String MODE_KEY = "mode";
	private static final String MASK_KEY = "mask";
	private static final String BAUD_KEY = "baud";
	private static final String DATA_BITS_KEY = "data.bits";
	private static final String STOP_BITS_KEY = "stop.bits";
	private static final String PARITY_KEY = "parity";
	private static final String BREAK_KEY = "break";
	private static final String FLOW_CONTROL_KEY = "flow.control";
	private static final String LATENCY_TIMER_MS_KEY = "latency.timer.ms";
	private static final String READ_CHUNK_SIZE_KEY = "read.chunk.size";
	private static final String WRITE_CHUNK_SIZE_KEY = "write.chunk.size";
	private static final String INTERFACE_PREFIX = "INTERFACE_";
	private static final String BITMODE_PREFIX = "BITMODE_";
	private static final double PRECISION = 0.1;

	public FtdiProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
	}

	public String finder() {
		return ref.value(FINDER_KEY);
	}

	public ftdi_interface iface() {
		String name = ref.value(INTERFACE_KEY);
		if (name == null) return null;
		return ftdi_interface.valueOf(INTERFACE_PREFIX + name.toUpperCase());
	}

	public FtdiConfig config() {
		var b = FtdiConfig.builder();
		FunctionUtil.safeAccept(bitMode(), b::bitMode);
		FunctionUtil.safeAccept(baud(), b::baud);
		FunctionUtil.safeAccept(params(), b::params);
		FunctionUtil.safeAccept(flowControl(), b::flowControl);
		FunctionUtil.safeAccept(latencyTimerMs(), b::latencyTimer);
		FunctionUtil.safeAccept(readChunkSize(), b::readChunkSize);
		FunctionUtil.safeAccept(writeChunkSize(), b::writeChunkSize);
		return b.build();
	}

	private FtdiBitMode bitMode() {
		ftdi_mpsse_mode mode = bitModeEnum();
		if (mode == null) return null;
		FtdiBitMode.Builder b = FtdiBitMode.builder(mode);
		safeAccept(bitMask(), b::mask);
		return b.build();
	}

	private ftdi_mpsse_mode bitModeEnum() {
		String name = ref.value(BIT_KEY, MODE_KEY);
		if (name == null) return null;
		return ftdi_mpsse_mode.valueOf(BITMODE_PREFIX + name.toUpperCase());
	}

	private Integer bitMask() {
		return ref.intValue(BIT_KEY, MASK_KEY);
	}

	private Integer baud() {
		return ref.intValue(BAUD_KEY);
	}

	private FtdiLineParams params() {
		var dataBits = dataBits();
		var stopBits = stopBits();
		var parity = parity();
		var breakType = breakType();
		if (ArrayUtil.allNull(dataBits, stopBits, parity, breakType)) return null;
		var b = FtdiLineParams.builder();
		if (dataBits != null) b.dataBits(dataBits);
		if (stopBits != null) b.stopBits(stopBits);
		if (parity != null) b.parity(parity);
		if (breakType != null) b.breakType(breakType);
		return b.build();
	}

	private ftdi_data_bits_type dataBits() {
		return ref.valueFromInt(ftdi_data_bits_type.xcoder::decodeValid, DATA_BITS_KEY);
	}

	private ftdi_stop_bits_type stopBits() {
		Double d = ref.doubleValue(STOP_BITS_KEY);
		if (d == null) return null;
		if (approxEqual(d, 1.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_1;
		if (approxEqual(d, 1.5, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_15;
		if (approxEqual(d, 2.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_2;
		throw new IllegalArgumentException("Invalid stop bits: " + d);
	}

	private ftdi_parity_type parity() {
		return ref.value(s -> ftdi_parity_type.valueOf(s.toUpperCase()), PARITY_KEY);
	}

	private ftdi_break_type breakType() {
		return ref.valueFromBoolean(ftdi_break_type.BREAK_ON, ftdi_break_type.BREAK_OFF, BREAK_KEY);
	}

	private FtdiFlowControl flowControl() {
		return ref.enumValue(FtdiFlowControl.class, FLOW_CONTROL_KEY);
	}

	private Integer latencyTimerMs() {
		return ref.intValue(LATENCY_TIMER_MS_KEY);
	}

	private Integer readChunkSize() {
		return ref.intValue(READ_CHUNK_SIZE_KEY);
	}

	private Integer writeChunkSize() {
		return ref.intValue(WRITE_CHUNK_SIZE_KEY);
	}
}
