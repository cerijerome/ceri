package ceri.serial.i2c.util;

import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_TEN;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.math.MathUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cUtil;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg_flag;
import ceri.serial.jna.JnaMemory;

/**
 * Wrapper of i2c_msg. Only one of 'out' and 'in' fields will be non-null, depending on whether the
 * message is read (has the I2C_M_RD flag) or write.
 */
public class I2cMessage {
	public final I2cAddress address;
	public final Set<i2c_msg_flag> flags;
	public final ByteProvider in;
	public final ByteReceiver out;

	public static List<I2cMessage> fromAll(i2c_msg... msgs) {
		return Stream.of(msgs).map(I2cMessage::of).collect(Collectors.toList());
	}

	public static I2cMessage of(i2c_msg msg) {
		return new I2cMessage(msg);
	}

	private I2cMessage(i2c_msg msg) {
		flags = Collections.unmodifiableSet(msg.flags().getAll());
		address = I2cUtil.address(msg.addr, flags.contains(I2C_M_TEN));
		JnaMemory data = JnaMemory.of(msg.buf, 0, MathUtil.ushort(msg.len));
		out = flags.contains(I2C_M_RD) ? data : null;
		in = out == null ? data : null;
	}

	/**
	 * Returns true if this is a read message, and data is expected to be written by the receiver
	 * via the 'out' field.
	 */
	public boolean isRead() {
		return out != null;
	}

	public boolean isAddress(I2cAddress address) {
		return this.address.equals(address);
	}
}
