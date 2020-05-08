package ceri.serial.mlx90640;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import ceri.common.data.ByteUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class StatusRegister {
	private static final int ENABLE_OVERWRITE_BIT = 4;
	private static final int NEW_DATA_AVAILABLE_BIT = 3;
	private final boolean enableOverwrite;
	private final boolean newDataAvailable;
	private final SubPage lastMeasuredSubPage;

	/**
	 * Decode settings from integer value.
	 */
	public static StatusRegister decode(int value) {
		boolean enableOverwrite = ByteUtil.bit(value, ENABLE_OVERWRITE_BIT);
		boolean newDataAvailable = ByteUtil.bit(value, NEW_DATA_AVAILABLE_BIT);
		return of(enableOverwrite, newDataAvailable, SubPage.decode(value));
	}

	public static StatusRegister of(boolean enableOverwrite, boolean newDataAvailable,
		SubPage lastMeasuredSubPage) {
		validateNotNull(lastMeasuredSubPage);
		return new StatusRegister(enableOverwrite, newDataAvailable, lastMeasuredSubPage);
	}

	private StatusRegister(boolean enableOverwrite, boolean newDataAvailable,
		SubPage lastMeasuredSubPage) {
		this.enableOverwrite = enableOverwrite;
		this.newDataAvailable = newDataAvailable;
		this.lastMeasuredSubPage = lastMeasuredSubPage;
	}

	/**
	 * Encodes settings to an integer value.
	 */
	public int encode() {
		return ByteUtil.maskOfBitInt(enableOverwrite, ENABLE_OVERWRITE_BIT) |
			ByteUtil.maskOfBitInt(newDataAvailable, NEW_DATA_AVAILABLE_BIT) |
			lastMeasuredSubPage.encode();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(enableOverwrite, newDataAvailable, lastMeasuredSubPage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof StatusRegister)) return false;
		StatusRegister other = (StatusRegister) obj;
		if (enableOverwrite != other.enableOverwrite) return false;
		if (newDataAvailable != other.newDataAvailable) return false;
		if (!EqualsUtil.equals(lastMeasuredSubPage, other.lastMeasuredSubPage)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, enableOverwrite, newDataAvailable, lastMeasuredSubPage).toString();
	}

}
