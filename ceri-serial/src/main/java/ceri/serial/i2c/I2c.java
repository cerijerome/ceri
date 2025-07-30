package ceri.serial.i2c;

import static ceri.serial.i2c.util.I2cUtil.SCAN_7BIT_MAX;
import static ceri.serial.i2c.util.I2cUtil.SCAN_7BIT_MIN;
import static ceri.serial.i2c.util.I2cUtil.SOFTWARE_RESET;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.IntStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.stream.StreamUtil;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaUtil;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.smbus.SmBus;

/**
 * Encapsulation of I2C bus.
 */
public interface I2c {
	/** A stateless, no-op instance. */
	I2c NULL = new Null() {
		@Override
		public String toString() {
			return I2c.class.getSimpleName() + ".NULL";
		}
	};

	/**
	 * Specify the number of times a device address should be polled when not acknowledging.
	 */
	I2c retries(int count) throws IOException;

	/**
	 * Specify the call timeout in milliseconds.
	 */
	I2c timeout(int timeoutMs) throws IOException;

	/**
	 * Determine functionality supported by the bus.
	 */
	Collection<i2c_func> functions() throws IOException;

	/**
	 * Reset internal state.
	 */
	default void reset() {}

	/**
	 * Verify the address is in use. Uses SMBus quick write (off) to check. Returns true if no
	 * exception. Results of various methods:
	 *
	 * <pre>
	 * Direct and i2c-emulated SMBus call results tested against MLX90640 device:
	 * - writeQuick(off) => success only for existing device
	 * - writeQuick(on) => success whether device exists or not
	 * - readByte() => success only for existing device
	 * - writeByte(0) => success only for existing device, but may have side-effects
	 * </pre>
	 */
	default boolean exists(I2cAddress address) {
		try {
			smBus(address).writeQuick(false);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Scan 7-bit address range for existing devices.
	 */
	default Set<I2cAddress> scan7Bit() {
		return StreamUtil.toSet(IntStream.rangeClosed(SCAN_7BIT_MIN, SCAN_7BIT_MAX)
			.mapToObj(i -> I2cAddress.of7Bit(i)).filter(this::exists));
	}

	/**
	 * Send a software reset to all devices.
	 */
	default void softwareReset() throws IOException {
		writeData(I2cAddress.GENERAL_CALL, ArrayUtil.bytes.of(SOFTWARE_RESET));
	}

	/**
	 * Read the device id for the address. (Not working with MLX90640)
	 */
	default DeviceId deviceId(I2cAddress address) throws IOException {
		// I2cUtil.validate7Bit(address);
		byte[] read = readData(I2cAddress.DEVICE_ID, address.frames(false), DeviceId.BYTES);
		return DeviceId.decode((int) ByteUtil.fromMsb(read));
	}

	/**
	 * Enable or disable user-mode PEC.
	 */
	void smBusPec(boolean on) throws IOException;

	/**
	 * Provide SMBus functionality. SMBus only supports 7-bit addresses.
	 */
	SmBus smBus(I2cAddress address) throws IOException;

	/**
	 * Send byte array command to address, and read byte array response, using ioctl.
	 */
	default byte[] readData(I2cAddress address, byte[] command, int readLen) throws IOException {
		byte[] receive = new byte[readLen];
		readData(address, command, receive);
		return receive;
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl. Read data is
	 * written to receiving byte array.
	 */
	default void readData(I2cAddress address, byte[] command, byte[] receive) throws IOException {
		readData(address, command, receive, 0);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl. Read data is
	 * written to receiving byte array.
	 */
	default void readData(I2cAddress address, byte[] command, byte[] receive, int offset)
		throws IOException {
		readData(address, command, receive, offset, receive.length - offset);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl. Read data is
	 * written to receiving byte array.
	 */
	default void readData(I2cAddress address, byte[] command, byte[] receive, int offset,
		int length) throws IOException {
		var write = GcMemory.mallocBytes(command);
		var read = GcMemory.malloc(length).clear();
		writeRead(address, write.m, write.intSize(), read.m, read.intSize());
		read.m.read(0, receive, offset, length);
	}

	/**
	 * Send byte array data to address, using ioctl.
	 */
	default void writeData(I2cAddress address, int... bytes) throws IOException {
		writeData(address, ArrayUtil.bytes.of(bytes));
	}

	/**
	 * Send byte array data to address, using ioctl.
	 */
	default void writeData(I2cAddress address, byte[] data) throws IOException {
		write(address, GcMemory.mallocBytes(data).m);
	}

	/**
	 * Write using supplied memory buffer for ioctl.
	 */
	default void write(I2cAddress address, Memory writeBuf) throws IOException {
		write(address, JnaUtil.intSize(writeBuf), writeBuf);
	}

	/**
	 * I2C write using supplied memory buffer for ioctl.
	 */
	void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException;

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	default void writeRead(I2cAddress address, Memory writeBuf, Memory readBuf) throws IOException {
		writeRead(address, writeBuf, JnaUtil.intSize(writeBuf), readBuf, JnaUtil.intSize(readBuf));
	}

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf, int readLen)
		throws IOException;

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends I2c {
		@Override
		default I2c retries(int count) throws IOException {
			return this;
		}

		@Override
		default I2c timeout(int timeoutMs) throws IOException {
			return this;
		}

		@Override
		default Collection<i2c_func> functions() throws IOException {
			return i2c_func.xcoder.all();
		}

		@Override
		default void smBusPec(boolean on) throws IOException {}

		@Override
		default SmBus smBus(I2cAddress address) {
			return SmBus.NULL;
		}

		@Override
		default void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException {}

		@Override
		default void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
			int readLen) throws IOException {}
	}
}
