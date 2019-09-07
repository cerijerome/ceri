package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_RESET;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;

public class FtdiBitmode {
	public static final FtdiBitmode OFF = builder(BITMODE_RESET).build();
	public static final FtdiBitmode BITBANG =
		builder(BITMODE_BITBANG).allLines(LineDirection.out).build();
	public final ftdi_mpsse_mode mode;
	public final int bitmask;

	public enum LineDirection {
		in,
		out;
	}

	public static FtdiBitmode of(ftdi_mpsse_mode mode) {
		return builder(mode).allLines(LineDirection.out).build();
	}

	public static class Builder {
		final ftdi_mpsse_mode mode;
		int bitmask = 0;

		Builder(ftdi_mpsse_mode mode) {
			this.mode = mode;
		}

		public Builder bitmask(int bitmask) {
			this.bitmask = bitmask;
			return this;
		}

		public Builder allLines(LineDirection dir) {
			if (dir == LineDirection.out) bitmask = 0xff;
			else bitmask = 0;
			return this;
		}

		public Builder line(LineDirection dir, int... bits) {
			int mask = 0;
			for (int bit : bits)
				if (bit < Byte.SIZE) mask |= 1L << bit;
			if (dir == LineDirection.out) bitmask |= mask;
			else bitmask &= ~mask;
			return this;
		}

		public FtdiBitmode build() {
			return new FtdiBitmode(this);
		}
	}

	public static Builder builder(ftdi_mpsse_mode mode) {
		return new Builder(mode);
	}

	FtdiBitmode(Builder builder) {
		mode = builder.mode;
		bitmask = builder.bitmask;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(mode, bitmask);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FtdiBitmode)) return false;
		FtdiBitmode other = (FtdiBitmode) obj;
		if (!EqualsUtil.equals(mode, other.mode)) return false;
		if (bitmask != other.bitmask) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, mode, bitmask).toString();
	}

}
