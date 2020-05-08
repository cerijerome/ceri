package ceri.serial.mlx90640;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.i2c.I2cAddress;

public class MlxI2cAddress {
	private static final int ADDRESS_MASK = 0xff;
	private static final int RESERVED_MASK = 0xff00;
	public final I2cAddress address;
	public final int reserved;

	public static MlxI2cAddress decode(int value) {
		I2cAddress address = I2cAddress.of7Bit(value & ADDRESS_MASK);
		int reserved = value & RESERVED_MASK;
		return of(address, reserved);
	}

	public static MlxI2cAddress of(I2cAddress address, int reserved) {
		if (address.tenBit)
			throw new IllegalArgumentException("10-bit addresses are not supported: " + address);
		return new MlxI2cAddress(address, reserved);
	}

	private MlxI2cAddress(I2cAddress address, int reserved) {
		this.address = address;
		this.reserved = reserved;
	}

	public int encode() {
		return address.address | reserved;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(address, reserved);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MlxI2cAddress)) return false;
		MlxI2cAddress other = (MlxI2cAddress) obj;
		if (!EqualsUtil.equals(address, other.address)) return false;
		if (reserved != other.reserved) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, address, "0x" + Integer.toHexString(reserved))
			.toString();
	}

}
