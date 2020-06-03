package ceri.serial.i2c;

import static ceri.serial.i2c.I2cUtil.SCAN_7BIT_MAX;
import static ceri.serial.i2c.I2cUtil.SCAN_7BIT_MIN;
import static ceri.serial.i2c.I2cUtil.SOFTWARE_RESET;
import static ceri.serial.jna.JnaUtil.size;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.IntStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.ByteUtil;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.jna.JnaUtil;

/**
 * Encapsulation of I2C bus.
 */
public interface I2c extends Closeable {

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
	 * Verify the address is in use. Uses SMBus quick write (off) to check. Returns true if no
	 * exception. Results of various methods:
	 * 
	 * <pre>
	 * (actual and emulated SMBus)
	 * writeQuick(off) => success only for existing device
	 * writeQuick(on) => success whether device exists or not
	 * readByte() => success only for existing device
	 * writeByte(0) => success only for existing device, but may have side-effects
	 * </pre>
	 */
	default boolean exists(I2cAddress address) {
		try {
			smBus(address, false).writeQuick(false);
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
		writeData(I2cAddress.GENERAL_CALL, ArrayUtil.bytes(SOFTWARE_RESET));
	}

	/**
	 * Read the device id for the address. (Not working with MLX90640)
	 */
	default DeviceId deviceId(I2cAddress address) throws IOException {
		//I2cUtil.validate7Bit(address);
		byte[] read = readData(I2cAddress.DEVICE_ID, address.frames(false), DeviceId.BYTES);
		return DeviceId.decode((int) ByteUtil.fromMsb(read));
	}

	/**
	 * Enable or disable user-mode PEC.
	 */
	void smBusPec(boolean on) throws IOException;

	/**
	 * Provide SMBus functionality. The useI2c flag determines whether SMBUS calls should use I2C
	 * read-writes calls instead of SMBUS direct calls. SMBus only supports 7-bit addresses.
	 */
	SmBus smBus(I2cAddress address, boolean useI2c);

	/**
	 * Send byte array command to address, and read byte array response, using ioctl.
	 */
	default byte[] readData(I2cAddress address, byte[] command, int readLen) throws IOException {
		Memory write = CUtil.malloc(command);
		Memory read = new Memory(readLen);
		writeRead(address, write, size(write), read, size(read));
		return JnaUtil.byteArray(read);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl. Read data is
	 * written to data array at offset.
	 */
	default void readData(I2cAddress address, byte[] command, byte[] data, int offset, int length)
		throws IOException {
		Memory write = CUtil.malloc(command);
		Memory read = new Memory(length);
		writeRead(address, write, size(write), read, size(read));
		read.read(0, data, offset, length);
	}

	/**
	 * Send byte array data to address, using ioctl.
	 */
	default void writeData(I2cAddress address, byte[] data) throws IOException {
		Memory write = CUtil.malloc(data);
		write(address, size(write), write);
	}

	/**
	 * Write using supplied memory buffer for ioctl.
	 */
	default void write(I2cAddress address, Memory writeBuf) throws IOException {
		write(address, size(writeBuf), writeBuf);
	}

	/**
	 * I2C write using supplied memory buffer for ioctl.
	 */
	void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException;

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	default void writeRead(I2cAddress address, Memory writeBuf, Memory readBuf) throws IOException {
		writeRead(address, writeBuf, size(writeBuf), readBuf, size(readBuf));
	}

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf, int readLen)
		throws IOException;

}
