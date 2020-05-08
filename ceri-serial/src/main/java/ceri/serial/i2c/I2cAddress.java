package ceri.serial.i2c;

import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates an I2C address, allowing for 10-bit addresses, and forcing use of address if
 * currently in use by the driver.
 */
public class I2cAddress {
	/** General Call used with Wr bit, Start byte with Rd bit */
	public static final I2cAddress GENERAL_CALL = new I2cAddress(0, false);
	/** Device ID, used with Wr and Rd bits */
	public static final I2cAddress DEVICE_ID = new I2cAddress(0x7c, false);
	private static final int HEX_DIGITS_7 = 2;
	private static final int HEX_DIGITS_10 = 3;
	private static final int MASK_7BIT = ByteUtil.maskInt(7);
	private static final int MASK_10BIT = ByteUtil.maskInt(10);
	private static final int FRAME1_10BIT_PREFIX = 0xf0; // 1st frame prefix of 10-bit address
	public final int address;
	public final boolean tenBit;

	/**
	 * Creates a 7-bit address if <= 0x7f, otherwise 10-bit.
	 */
	public static I2cAddress of(int value) {
		return (value & MASK_7BIT) == value ? of7Bit(value) : of10Bit(value);
	}

	/**
	 * Encapsulates a 7-bit address.
	 */
	public static I2cAddress of7Bit(int address) {
		int masked = address & MASK_7BIT;
		if (masked == address) return new I2cAddress(masked, false);
		throw new IllegalArgumentException(
			"Invalid 7-bit address: 0x" + Integer.toHexString(address));
	}

	/**
	 * Encapsulates a 10-bit address.
	 */
	public static I2cAddress of10Bit(int address) {
		int masked = address & MASK_10BIT;
		if (masked == address) return new I2cAddress(masked, true);
		throw new IllegalArgumentException(
			"Invalid 10-bit address: 0x" + Integer.toHexString(address));
	}

	private I2cAddress(int address, boolean tenBit) {
		this.address = address;
		this.tenBit = tenBit;
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
		if (!tenBit) return ArrayUtil.bytes((address << 1) | (read ? 1 : 0));
		return ArrayUtil.bytes(
			FRAME1_10BIT_PREFIX | (ByteUtil.byteAt(address, 1) << 1) | (read ? 1 : 0),
			ByteUtil.byteAt(address, 0));
	}

	// /**
	// * Returns a byte for the address and read/write bit.
	// */
	// public byte addressByte(boolean read) {
	// if (tenBit) throw new UnsupportedOperationException("Not available for 10-bit addresses");
	// return (byte) ((address << 1) | (read ? 1 : 0));
	// }

	@Override
	public int hashCode() {
		return HashCoder.hash(address, tenBit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof I2cAddress)) return false;
		I2cAddress other = (I2cAddress) obj;
		if (address != other.address) return false;
		if (tenBit != other.tenBit) return false;
		return true;
	}

	@Override
	public String toString() {
		return "0x" + StringUtil.toHex(address, tenBit ? HEX_DIGITS_10 : HEX_DIGITS_7);
	}

}
