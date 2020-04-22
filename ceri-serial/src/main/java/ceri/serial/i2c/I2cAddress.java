package ceri.serial.i2c;

import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates an I2C address, allowing for 10-bit addresses, and forcing use of address if
 * currently in use by the driver.
 */
public class I2cAddress {
	public static final I2cAddress NULL = new I2cAddress(0, false);
	private static final int HEX_DIGITS_7 = 2;
	private static final int HEX_DIGITS_10 = 3;
	private static final int MASK_7 = ByteUtil.maskInt(7);
	private static final int MASK_10 = ByteUtil.maskInt(10);
	public final int address;
	public final boolean tenBit;

	/**
	 * Creates a 7-bit address if <= 0x7f, otherwise 10-bit.
	 */
	public static I2cAddress of(int value) {
		return (value & MASK_7) == value ? of7Bit(value) : of10Bit(value);
	}

	/**
	 * Encapsulates a 7-bit address. Force allows use of an address even if currently in use by the
	 * driver.
	 */
	public static I2cAddress of7Bit(int address) {
		int masked = address & MASK_7;
		if (masked == address) return new I2cAddress(masked, false);
		throw new IllegalArgumentException(
			"Invalid 7-bit address: 0x" + Integer.toHexString(address));
	}

	/**
	 * Encapsulates a 10-bit address. Force allows use of an address even if currently in use by the
	 * driver.
	 */
	public static I2cAddress of10Bit(int address) {
		int masked = address & MASK_10;
		if (masked == address) return new I2cAddress(masked, true);
		throw new IllegalArgumentException(
			"Invalid 10-bit address: 0x" + Integer.toHexString(address));
	}

	private I2cAddress(int address, boolean tenBit) {
		this.address = address;
		this.tenBit = tenBit;
	}

	public short value() {
		return (short) address;
	}

	public byte addressByte(boolean read) {
		if (tenBit) throw new UnsupportedOperationException("10-bit addresses not supported");
		return (byte) ((address << 1) | (read ? 1 : 0));
	}

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
