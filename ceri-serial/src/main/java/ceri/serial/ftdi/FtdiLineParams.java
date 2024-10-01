package ceri.serial.ftdi;

import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public record FtdiLineParams(ftdi_data_bits_type dataBits, ftdi_stop_bits_type stopBits,
	ftdi_parity_type parity, ftdi_break_type breakType) {

	public static final FtdiLineParams DEFAULT = builder().build();

	public static class Builder {
		ftdi_data_bits_type dataBits = ftdi_data_bits_type.BITS_8;
		ftdi_stop_bits_type stopBits = ftdi_stop_bits_type.STOP_BIT_1;
		ftdi_parity_type parity = ftdi_parity_type.NONE;
		ftdi_break_type breakType = ftdi_break_type.BREAK_OFF;

		Builder() {}

		public FtdiLineParams.Builder dataBits(ftdi_data_bits_type dataBits) {
			this.dataBits = dataBits;
			return this;
		}

		public FtdiLineParams.Builder stopBits(ftdi_stop_bits_type stopBits) {
			this.stopBits = stopBits;
			return this;
		}

		public FtdiLineParams.Builder parity(ftdi_parity_type parity) {
			this.parity = parity;
			return this;
		}

		public FtdiLineParams.Builder breakType(ftdi_break_type breakType) {
			this.breakType = breakType;
			return this;
		}

		public FtdiLineParams build() {
			return new FtdiLineParams(dataBits, stopBits, parity, breakType);
		}
	}

	public static FtdiLineParams.Builder builder() {
		return new Builder();
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s,%s", dataBits, stopBits, parity, breakType);
	}
}