package ceri.serial.i2c;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.validation.ValidationUtil.validateRange;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.exception.Exceptions;
import ceri.common.math.MathUtil;
import ceri.common.math.Radix;
import ceri.common.text.Format;

/**
 * Encapsulates an I2C address, allowing for 10-bit addresses, and forcing use of address if
 * currently in use by the driver.
 */
public record I2cAddress(int address, boolean tenBit) {
	/** General Call used with Wr bit, Start byte with Rd bit */
	public static final I2cAddress GENERAL_CALL = new I2cAddress(0, false);
	/** Device ID, used with Wr and Rd bits */
	public static final I2cAddress DEVICE_ID = new I2cAddress(0x7c, false);
	private static final Format HEX3 = Format.of(Radix.HEX, 3);
	public static final int MASK_7BIT = ByteUtil.maskInt(7);
	public static final int MASK_10BIT = ByteUtil.maskInt(10);
	private static final int FRAME0_10BIT_PREFIX = 0xf0; // frame[0] prefix of 10-bit address
	private static final int SLAVE_7BIT_MIN = 0x08;
	private static final int SLAVE_7BIT_MAX = 0x77;

	/**
	 * Extracts address from frame bytes. 2 bytes for a 10-bit address, 1 byte for 7-bit.
	 */
	public static I2cAddress fromFrames(byte[] frames) {
		validateRange(frames.length, 1, 2);
		if (frames.length == 1) return of7Bit(MathUtil.ubyte(frames[0]) >>> 1);
		if ((FRAME0_10BIT_PREFIX & frames[0]) != FRAME0_10BIT_PREFIX)
			throw Exceptions.illegalArg("Invalid 10-bit frames: 0x%02x, 0x%02x", frames[0], frames[1]);
		int address =
			(int) ByteUtil.fromMsb((~FRAME0_10BIT_PREFIX & ubyte(frames[0])) >>> 1, frames[1]);
		return of10Bit(address);
	}

	/**
	 * Creates a 7-bit address if <= 0x7f, otherwise 10-bit.
	 */
	public static I2cAddress of(int value) {
		if ((value & MASK_7BIT) == value) return new I2cAddress(value, false);
		if ((value & MASK_10BIT) == value) return new I2cAddress(value, true);
		throw Exceptions.illegalArg("Invalid 7-bit or 10-bit address: 0x%x", value);
	}

	/**
	 * Encapsulates a 7-bit address.
	 */
	public static I2cAddress of7Bit(int address) {
		int masked = address & MASK_7BIT;
		if (masked == address) return new I2cAddress(masked, false);
		throw Exceptions.illegalArg("Invalid 7-bit address: 0x%x", address);
	}

	/**
	 * Encapsulates a 10-bit address.
	 */
	public static I2cAddress of10Bit(int address) {
		int masked = address & MASK_10BIT;
		if (masked == address) return new I2cAddress(masked, true);
		throw Exceptions.illegalArg("Invalid 10-bit address: 0x%x", address);
	}

	/**
	 * Is this address for a specific slave device? Otherwise this is a reserved address.
	 */
	public boolean isSlave() {
		if (tenBit) return true;
		return (address >= SLAVE_7BIT_MIN && address <= SLAVE_7BIT_MAX);
	}

	/**
	 * Returns the address value as short.
	 */
	public short value() {
		return (short) address;
	}

	/**
	 * Returns frame bytes for the address and read/write bit. 2 bytes for a 10-bit address, 1 byte
	 * for 7-bit.
	 */
	public byte[] frames(boolean read) {
		if (!tenBit) return ArrayUtil.bytes.of((address << 1) | (read ? 1 : 0));
		return ArrayUtil.bytes.of(
			FRAME0_10BIT_PREFIX | (ByteUtil.byteAt(address, 1) << 1) | (read ? 1 : 0),
			ByteUtil.byteAt(address, 0));
	}

	@Override
	public String toString() {
		return tenBit ? HEX3.ushort(address) : Format.HEX2.ubyte(address);
	}
}
