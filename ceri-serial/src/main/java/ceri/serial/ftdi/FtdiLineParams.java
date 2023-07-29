package ceri.serial.ftdi;

import java.util.Objects;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiLineParams {
	public static final FtdiLineParams DEFAULT = builder().build();
	public final ftdi_data_bits_type dataBits;
	public final ftdi_stop_bits_type stopBits;
	public final ftdi_parity_type parity;
	public final ftdi_break_type breakType;

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
			return new FtdiLineParams(this);
		}
	}

	public static FtdiLineParams.Builder builder() {
		return new Builder();
	}

	FtdiLineParams(FtdiLineParams.Builder builder) {
		dataBits = builder.dataBits;
		stopBits = builder.stopBits;
		parity = builder.parity;
		breakType = builder.breakType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataBits, stopBits, parity, breakType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FtdiLineParams)) return false;
		FtdiLineParams other = (FtdiLineParams) obj;
		if (!Objects.equals(dataBits, other.dataBits)) return false;
		if (!Objects.equals(stopBits, other.stopBits)) return false;
		if (!Objects.equals(parity, other.parity)) return false;
		if (!Objects.equals(breakType, other.breakType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s,%s", dataBits, stopBits, parity, breakType);
	}
}