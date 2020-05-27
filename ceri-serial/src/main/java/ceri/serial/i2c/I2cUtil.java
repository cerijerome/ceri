package ceri.serial.i2c;

import static ceri.common.math.MathUtil.ushortExact;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_TEN;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg_flag;
import ceri.serial.jna.JnaUtil;

public class I2cUtil {

	private I2cUtil() {}

	public static i2c_msg.ByReference populate(i2c_msg.ByReference msg, I2cAddress address,
		Memory m, i2c_msg_flag... flags) {
		return populate(msg, address, m == null ? 0 : JnaUtil.size(m), m, flags);
	}

	public static i2c_msg.ByReference populate(i2c_msg.ByReference msg, I2cAddress address,
		int size, Pointer p, i2c_msg_flag... flags) {
		if (msg == null) msg = new i2c_msg.ByReference();
		msg.addr = address.value();
		msg.len = ushortExact(size);
		msg.buf = p;
		if (address.tenBit) msg.flags().add(flags).add(I2C_M_TEN);
		else if (flags.length > 0) msg.flags().add(flags);
		return msg;
	}

}
