package ceri.serial.ftdi;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiLineProperties {
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

		public FtdiLineProperties.Builder bits(ftdi_data_bits_type bits) {
			this.bits = bits;
			return this;
		}

		public FtdiLineProperties.Builder sbit(ftdi_stop_bits_type sbit) {
			this.sbit = sbit;
			return this;
		}

		public FtdiLineProperties.Builder parity(ftdi_parity_type parity) {
			this.parity = parity;
			return this;
		}

		public FtdiLineProperties.Builder breakType(ftdi_break_type breakType) {
			this.breakType = breakType;
			return this;
		}

		public FtdiLineProperties build() {
			return new FtdiLineProperties(this);
		}
	}

	public static FtdiLineProperties.Builder builder() {
		return new Builder();
	}

	FtdiLineProperties(FtdiLineProperties.Builder builder) {
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
		if (!(obj instanceof FtdiLineProperties)) return false;
		FtdiLineProperties other = (FtdiLineProperties) obj;
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