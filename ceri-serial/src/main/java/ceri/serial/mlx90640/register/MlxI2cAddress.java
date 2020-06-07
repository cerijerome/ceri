package ceri.serial.mlx90640.register;

import java.util.Objects;
import ceri.common.data.IntAccessor;
import ceri.common.text.ToStringHelper;
import ceri.serial.i2c.I2cAddress;

public class MlxI2cAddress {
	private static final IntAccessor.Typed<MlxI2cAddress> accessor =
		IntAccessor.typed(t -> t.value, (t, i) -> t.value = i);
	private static final IntAccessor.Typed<MlxI2cAddress> addressField = accessor.maskBits(0, 8);
	private int value;

	public static MlxI2cAddress of(int value) {
		return new MlxI2cAddress(value);
	}

	private MlxI2cAddress(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public I2cAddress address() {
		return I2cAddress.of7Bit(addressField.get(this));
	}

	public MlxI2cAddress address(I2cAddress address) {
		if (address.tenBit)
			throw new IllegalArgumentException("10-bit addresses are not supported: " + address);
		addressField.set(this, address.address);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MlxI2cAddress)) return false;
		MlxI2cAddress other = (MlxI2cAddress) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("0x%04x", value), address())
			.toString();
	}

}
