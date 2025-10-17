package ceri.serial.i2c.util;

import static ceri.common.math.Maths.ushortExact;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_TEN;
import java.util.concurrent.TimeUnit;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.util.Validate;
import ceri.jna.util.JnaUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg_flag;

public class I2cUtil {
	public static final int SCAN_7BIT_MIN = 0x03;
	public static final int SCAN_7BIT_MAX = 0x77;
	public static final int SOFTWARE_RESET = 0x06;
	private static final int BITS_PER_FRAME = Byte.SIZE + 1;

	private I2cUtil() {}

	/**
	 * Calculates the minimum time to send/receive the address and given number of bytes at the
	 * given frequency. A zero frequency represents no delay.
	 */
	public static long micros(int hz, I2cAddress address, int bytes) {
		int addrBytes = address.tenBit() ? 2 : 1;
		return micros(hz, addrBytes + bytes);
	}

	/**
	 * Calculates the minimum time to send/receive the given number of bytes at the given frequency.
	 * A zero frequency represents no delay.
	 */
	public static long micros(int hz, int bytes) {
		if (hz == 0) return 0;
		long bits = bytes * BITS_PER_FRAME;
		return (long) Math.ceil(((double) bits * TimeUnit.SECONDS.toMicros(1)) / hz);
	}

	/**
	 * Creates an I2C address from address value and 10-bit flag.
	 */
	public static I2cAddress address(int address, boolean bit10) {
		return bit10 ? I2cAddress.of10Bit(address) : I2cAddress.of7Bit(address);
	}

	public static I2cAddress valid7Bit(I2cAddress address) {
		Validate.nonNull(address);
		if (!address.tenBit()) return address;
		throw new UnsupportedOperationException("Only 7-bit addresses are supported: " + address);
	}

	public static i2c_msg.ByReference populate(i2c_msg.ByReference msg, I2cAddress address,
		Memory m, i2c_msg_flag... flags) {
		return populate(msg, address, m == null ? 0 : JnaUtil.intSize(m), m, flags);
	}

	public static i2c_msg.ByReference populate(i2c_msg.ByReference msg, I2cAddress address,
		int size, Pointer p, i2c_msg_flag... flags) {
		if (msg == null) msg = new i2c_msg.ByReference();
		msg.addr = address.value();
		msg.len = ushortExact(size);
		msg.buf = p;
		i2c_msg.FLAGS.add(msg, flags);
		if (address.tenBit()) i2c_msg.FLAGS.add(msg, I2C_M_TEN);
		return msg;
	}
}
