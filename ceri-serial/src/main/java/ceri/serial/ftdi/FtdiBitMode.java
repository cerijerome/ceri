package ceri.serial.ftdi;

import static ceri.common.math.Maths.ubyte;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_BITBANG;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_RESET;
import ceri.common.io.Io;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;

public record FtdiBitMode(ftdi_mpsse_mode mode, int mask) {

	public static final FtdiBitMode OFF = builder(BITMODE_RESET).build();
	public static final FtdiBitMode BITBANG =
		builder(BITMODE_BITBANG).allLines(Io.Direction.out).build();

	public static FtdiBitMode of(ftdi_mpsse_mode mode) {
		return builder(mode).allLines(Io.Direction.out).build();
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

		public Builder allLines(Io.Direction dir) {
			if (dir == Io.Direction.out) mask = 0xff;
			else mask = 0;
			return this;
		}

		public Builder line(Io.Direction dir, int... bits) {
			int mask = 0;
			for (int bit : bits)
				if (bit < Byte.SIZE) mask |= 1L << bit;
			if (dir == Io.Direction.out) this.mask |= mask;
			else this.mask &= ubyte(~mask);
			return this;
		}

		public FtdiBitMode build() {
			return new FtdiBitMode(mode, mask);
		}
	}

	public static Builder builder(ftdi_mpsse_mode mode) {
		return new Builder(mode);
	}
}
