package ceri.serial.ftdi;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiLineParams {
	public static final FtdiLineParams DEFAULT = builder().build();
	public final ftdi_data_bits_type bits;
	public final ftdi_stop_bits_type sbit;
	public final ftdi_parity_type parity;
	public final ftdi_break_type breakType;

	public static class Builder {
		ftdi_data_bits_type bits = ftdi_data_bits_type.BITS_8;
		ftdi_stop_bits_type sbit = ftdi_stop_bits_type.STOP_BIT_1;
		ftdi_parity_type parity = ftdi_parity_type.NONE;
		ftdi_break_type breakType = ftdi_break_type.BREAK_OFF;

		Builder() {}

		public FtdiLineParams.Builder bits(ftdi_data_bits_type bits) {
			this.bits = bits;
			return this;
		}

		public FtdiLineParams.Builder sbit(ftdi_stop_bits_type sbit) {
			this.sbit = sbit;
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
		bits = builder.bits;
		sbit = builder.sbit;
		parity = builder.parity;
		breakType = builder.breakType;
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(bits, sbit, parity, breakType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FtdiLineParams)) return false;
		FtdiLineParams other = (FtdiLineParams) obj;
		if (!EqualsUtil.equals(bits, other.bits)) return false;
		if (!EqualsUtil.equals(sbit, other.sbit)) return false;
		if (!EqualsUtil.equals(parity, other.parity)) return false;
		if (!EqualsUtil.equals(breakType, other.breakType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, bits, sbit, parity, breakType).toString();
	}
	
}