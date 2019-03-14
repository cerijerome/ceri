package ceri.serial.ftdi;

import static ceri.common.function.FunctionUtil.safeAccept;
import static ceri.common.math.MathUtil.approxEqual;
import ceri.common.property.BaseProperties;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiLineProperties extends BaseProperties {
	private static final String DATA_BITS_KEY = "data.bits";
	private static final String STOP_BITS_KEY = "stop.bits";
	private static final String PARITY_KEY = "parity";
	private static final String BREAK_KEY = "break";
	private static final double PRECISION = 0.1;

	public FtdiLineProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public FtdiLineParams params() {
		FtdiLineParams.Builder b = FtdiLineParams.builder();
		safeAccept(dataBits(), b::bits);
		safeAccept(stopBits(), b::sbit);
		safeAccept(parity(), b::parity);
		safeAccept(breakType(), b::breakType);
		return b.build();
	}

	private ftdi_data_bits_type dataBits() {
		return valueFromInt(ftdi_data_bits_type.xcoder::decode, DATA_BITS_KEY);
	}

	private ftdi_stop_bits_type stopBits() {
		Double d = doubleValue(STOP_BITS_KEY);
		if (d == null) return null;
		if (approxEqual(d, 1.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_1;
		if (approxEqual(d, 1.5, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_15;
		if (approxEqual(d, 2.0, PRECISION)) return ftdi_stop_bits_type.STOP_BIT_2;
		throw new IllegalArgumentException("Invalid stop bits: " + d);
	}

	private ftdi_parity_type parity() {
		return value(s -> ftdi_parity_type.valueOf(s.toUpperCase()), PARITY_KEY);
	}

	private ftdi_break_type breakType() {
		return valueFromBoolean(ftdi_break_type.BREAK_ON, ftdi_break_type.BREAK_OFF, BREAK_KEY);
	}

}
