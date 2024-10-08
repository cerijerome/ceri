package ceri.serial.ftdi.util;

import static ceri.common.math.MathUtil.approxEqual;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.property.Parser;
import ceri.common.property.TypedProperties;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiProperties extends TypedProperties.Ref {
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

	public FtdiProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public String finder() {
		return parse(FINDER_KEY).get();
	}

	public ftdi_interface iface() {
		return parse(INTERFACE_KEY).mod(s -> INTERFACE_PREFIX + s.toUpperCase())
			.toEnum(ftdi_interface.class);
	}

	public FtdiConfig config() {
		var b = FtdiConfig.builder();
		bitMode().accept(b::bitMode);
		parse(BAUD_KEY).asInt().accept(b::baud);
		FunctionUtil.safeAccept(params(), b::params);
		parse(FLOW_CONTROL_KEY).asEnum(FtdiFlowControl.class).accept(b::flowControl);
		parse(LATENCY_TIMER_MS_KEY).asInt().accept(b::latencyTimer);
		parse(READ_CHUNK_SIZE_KEY).asInt().accept(b::readChunkSize);
		parse(WRITE_CHUNK_SIZE_KEY).asInt().accept(b::writeChunkSize);
		return b.build();
	}

	private Parser.Type<FtdiBitMode> bitMode() {
		return parse(BIT_KEY, MODE_KEY).mod(s -> BITMODE_PREFIX + s.toUpperCase())
			.asEnum(ftdi_mpsse_mode.class).as(mode -> {
				FtdiBitMode.Builder b = FtdiBitMode.builder(mode);
				parse(BIT_KEY, MASK_KEY).asInt().accept(b::mask);
				return b.build();
			});
	}

	private FtdiLineParams params() {
		var dataBits = parse(DATA_BITS_KEY).asInt().to(ftdi_data_bits_type.xcoder::decodeValid);
		var stopBits = stopBits();
		var parity = parse(PARITY_KEY).mod(String::toUpperCase).toEnum(ftdi_parity_type.class);
		var breakType =
			parse(BREAK_KEY).toBool(ftdi_break_type.BREAK_ON, ftdi_break_type.BREAK_OFF);
		if (ArrayUtil.allNull(dataBits, stopBits, parity, breakType)) return null;
		var b = FtdiLineParams.builder();
		if (dataBits != null) b.dataBits(dataBits);
		if (stopBits != null) b.stopBits(stopBits);
		if (parity != null) b.parity(parity);
		if (breakType != null) b.breakType(breakType);
		return b.build();
	}

	private ftdi_stop_bits_type stopBits() {
		return parse(STOP_BITS_KEY).asDouble().to(d -> {
			if (approxEqual(d, 1.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_1;
			if (approxEqual(d, 1.5, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_15;
			if (approxEqual(d, 2.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_2;
			throw new IllegalArgumentException("Invalid stop bits: " + d);
		});
	}
}
