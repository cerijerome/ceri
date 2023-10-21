package ceri.serial.ftdi;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_RESET;
import java.util.Objects;
import ceri.common.io.Direction;
import ceri.common.text.ToString;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;

public class FtdiBitMode {
	public static final FtdiBitMode OFF = builder(BITMODE_RESET).build();
	public static final FtdiBitMode BITBANG =
		builder(BITMODE_BITBANG).allLines(Direction.out).build();
	public final ftdi_mpsse_mode mode;
	public final int mask;

	public static FtdiBitMode of(ftdi_mpsse_mode mode) {
		return builder(mode).allLines(Direction.out).build();
	}

	public static class Builder {
		final ftdi_mpsse_mode mode;
		int mask = 0;

		Builder(ftdi_mpsse_mode mode) {
			this.mode = mode;
		}

		public Builder mask(int mask) {
			this.mask = ubyte(mask);
			return this;
		}

		public Builder allLines(Direction dir) {
			if (dir == Direction.out) mask = 0xff;
			else mask = 0;
			return this;
		}

		public Builder line(Direction dir, int... bits) {
			int mask = 0;
			for (int bit : bits)
				if (bit < Byte.SIZE) mask |= 1L << bit;
			if (dir == Direction.out) this.mask |= mask;
			else this.mask &= ubyte(~mask);
			return this;
		}

		public FtdiBitMode build() {
			return new FtdiBitMode(this);
		}
	}

	public static Builder builder(ftdi_mpsse_mode mode) {
		return new Builder(mode);
	}

	FtdiBitMode(Builder builder) {
		mode = builder.mode;
		mask = builder.mask;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mode, mask);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FtdiBitMode)) return false;
		FtdiBitMode other = (FtdiBitMode) obj;
		if (!Objects.equals(mode, other.mode)) return false;
		if (mask != other.mask) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, mode, mask);
	}

}
