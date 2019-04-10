package ceri.serial.pigpio;

import static ceri.common.util.BasicUtil.exceptionf;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.MaskTranscoder;
import ceri.common.data.TypeTranscoder;

/**
 * spiFlags: consists of the least significant 22 bits.
 * 
 * <pre>
 * 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
 *  b  b  b  b  b  b  R  T  n  n  n  n  W  A u2 u1 u0 p2 p1 p0  m  m
 * </pre>
 * 
 * mm defines the SPI mode. Warning: modes 1 and 3 do not appear to work on the auxiliary device.
 * 
 * px is 0 if CEx is active low (default) and 1 for active high.
 * 
 * ux is 0 if the CEx GPIO is reserved for SPI (default) and 1 otherwise.
 * 
 * A is 0 for the standard SPI device, 1 for the auxiliary SPI.
 * 
 * W is 0 if the device is not 3-wire, 1 if the device is 3-wire. Standard SPI device only.
 * 
 * nnnn defines the number of bytes (0-15) to write before switching the MOSI line to MISO to read
 * data. This field is ignored if W is not set. Standard SPI device only.
 * 
 * T is 1 if the least significant bit is transmitted on MOSI first, the default (0) shifts the most
 * significant bit out first. Auxiliary SPI device only.
 * 
 * R is 1 if the least significant bit is received on MISO first, the default (0) receives the most
 * significant bit first. Auxiliary SPI device only.
 * 
 * bbbbbb defines the word size in bits (0-32). The default (0) sets 8 bits per word. Auxiliary SPI
 * device only.
 */
public class PigpioSpiFlags {
	private static final int VALUE_MAX = (1 << 22) - 1;
	private static final int MODE_MAX = 3;
	private static final int WRITE_BEFORE_READ_MAX = 15;
	private static final int WORD_SIZE_MAX = 32;
	static final MaskTranscoder modeXcoder = MaskTranscoder.shiftBits(0, 2);
	static final MaskTranscoder writeBeforeReadXcoder = MaskTranscoder.shiftBits(10, 4);
	static final MaskTranscoder wordSizeXcoder = MaskTranscoder.shiftBits(16, 6);
	public final int value;

	public static enum Bit {
		cpolHigh(0),
		cphaHigh(1),
		ce0High(2),
		ce1High(3),
		ce2High(4),
		ce0NonSpi(5),
		ce1NonSpi(6),
		ce2NonSpi(7),
		useAux(8),
		wire3(9),
		auxLsbTransmit(14),
		auxLsbReceive(15);

		static final TypeTranscoder.Flag<Bit> xcoder = TypeTranscoder.flag(t -> t.value, Bit.class);
		public final int value;

		private Bit(int bit) {
			value = 1 << bit;
		}
	}

	public static class Builder {
		int value;

		Builder(int value) {
			this.value = value;
		}

		public Builder mode(int mode) {
			validateRange(mode, 0, MODE_MAX);
			value = modeXcoder.encodeInt(mode, value);
			return this;
		}

		public Builder flags(Bit... flags) {
			return flags(Arrays.asList(flags));
		}

		public Builder flags(Collection<Bit> flags) {
			value |= Bit.xcoder.encode(flags);
			return this;
		}

		public Builder writeBeforeRead(int bytes) {
			validateRange(bytes, 0, WRITE_BEFORE_READ_MAX);
			value = writeBeforeReadXcoder.encodeInt(bytes, value);
			return this;
		}

		public Builder wordSize(int bits) {
			validateRange(bits, 0, WORD_SIZE_MAX);
			value = wordSizeXcoder.encodeInt(bits, value);
			return this;
		}

		public PigpioSpiFlags build() {
			return new PigpioSpiFlags(value);
		}
	}

	public static Builder builder() {
		return builder(0);
	}

	public static Builder builder(int value) {
		return new Builder(value);
	}

	public static PigpioSpiFlags of(int value) {
		return new PigpioSpiFlags(value);
	}

	public static PigpioSpiFlags ofSpi0(int mode) {
		return builder().mode(mode).build();
	}

	public static PigpioSpiFlags ofSpi1(int mode) {
		return builder().mode(mode).flags(Bit.useAux).build();
	}

	PigpioSpiFlags(int value) {
		this.value = value;
	}

	public int mode() {
		return modeXcoder.decodeInt(value);
	}

	public Set<Bit> flags() {
		return Bit.xcoder.decode(value);
	}

	public int writeBeforeRead() {
		return writeBeforeReadXcoder.decodeInt(value);
	}

	public int wordSize() {
		return wordSizeXcoder.decodeInt(value);
	}

	public String binary() {
		return Integer.toBinaryString(value);
	}

	public PigpioSpiFlags validate() {
		if (value >= 0 && value <= VALUE_MAX) return this;
		throw exceptionf(IllegalStateException::new, "Value must be <= 0x%x: 0x%x", VALUE_MAX,
			value);
	}

	@Override
	public String toString() {
		return String.format("0x%04x(wbr=%d, word=%d, %s)", value, writeBeforeRead(), wordSize(),
			flags());
	}
}
