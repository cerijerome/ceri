package ceri.serial.i2c;

import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import static ceri.serial.jna.JnaUtil.size;
import java.io.Closeable;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.ByteUtil;
import ceri.log.util.LogUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.jna.JnaUtil;

/**
 * Encapsulation of I2C bus.
 */
public class I2c implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SCAN_7BIT_MIN = 0x03;
	private static final int SCAN_7BIT_MAX = 0x77;
	private static final int SOFTWARE_RESET = 0x06;
	private final FileDescriptor fd;
	private final State state = new State();

	/**
	 * Keep state to avoid unneeded ioctl calls.
	 */
	private static class State {
		static final int NO_ADDRESS = -1;
		int address = NO_ADDRESS;
		boolean tenBit = false;
		boolean pec = false;
	}

	/**
	 * Open a handle to the I2C bus.
	 */
	public static I2c open(int bus) throws CException {
		if (bus < 0) throw new IllegalArgumentException("Invalid bus number: " + bus);
		return new I2c(I2cDev.i2c_open(bus, O_RDWR.value));
	}

	private I2c(int fd) {
		this.fd = FileDescriptor.of(fd);
	}

	/**
	 * Specify the number of times a device address should be polled when not acknowledging.
	 */
	public I2c retries(int count) throws CException {
		I2cDev.i2c_retries(fd.fd(), count);
		return this;
	}

	/**
	 * Specify the call timeout.
	 */
	public I2c timeout(int timeoutMs) throws CException {
		I2cDev.i2c_timeout(fd.fd(), timeoutMs);
		return this;
	}

	/**
	 * Determine functionality supported by the bus.
	 */
	public Set<i2c_func> functions() throws CException {
		return i2c_func.xcoder.decodeAll(I2cDev.i2c_funcs(fd.fd()));
	}

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
	public boolean exists(I2cAddress address) {
		try {
			smBus(address, false).writeQuick(false);
			return true;
		} catch (CException e) {
			return false;
		}
	}

	/**
	 * Scan 7-bit address range for existing devices.
	 */
	public Set<I2cAddress> scan7Bit() {
		return StreamUtil.toSet(IntStream.rangeClosed(SCAN_7BIT_MIN, SCAN_7BIT_MAX)
			.mapToObj(i -> I2cAddress.of7Bit(i)).filter(this::exists));
	}

	/**
	 * Send a software reset to all devices.
	 */
	public void softwareReset() throws CException {
		writeData(I2cAddress.GENERAL_CALL, ArrayUtil.bytes(SOFTWARE_RESET));
	}

	/**
	 * Read the device id for the address. (Not working with MLX90640)
	 */
	public DeviceId deviceId(I2cAddress address) throws CException {
		validate7Bit(address);
		byte[] read = readData(I2cAddress.DEVICE_ID, address.frames(false), DeviceId.BYTES);
		return DeviceId.decode((int) ByteUtil.fromLsb(read));
	}

	/**
	 * Enable or disable user-mode PEC.
	 */
	public void smBusPec(boolean on) throws CException {
		I2cDev.i2c_smbus_pec(fd.fd(), on);
		state.pec = on;
	}

	/**
	 * Provide SMBus functionality. Emulated SMBus uses ioctl read-writes calls. SMBus only supports
	 * 7-bit addresses.
	 */
	public SmBus smBus(I2cAddress address, boolean emulated) {
		validate7Bit(address);
		return emulated ? new SmBusEmulated(this, address) : new SmBusActual(this, address);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl.
	 */
	public byte[] readData(I2cAddress address, byte[] command, int readLen) throws CException {
		Memory write = CUtil.malloc(command);
		Memory read = new Memory(readLen);
		writeRead(address, write, size(write), read, size(read));
		return JnaUtil.byteArray(read);
	}

	/**
	 * Send byte array command to address, and read byte array response, using ioctl. Read data is
	 * written to data array at offset.
	 */
	public void readData(I2cAddress address, byte[] command, byte[] data, int offset, int length)
		throws CException {
		Memory write = CUtil.malloc(command);
		Memory read = new Memory(length);
		writeRead(address, write, size(write), read, size(read));
		read.read(0, data, offset, length);
	}

	/**
	 * Send byte array data to address, using ioctl.
	 */
	public void writeData(I2cAddress address, byte[] data) throws CException {
		Memory write = CUtil.malloc(data);
		write(address, size(write), write);
	}

	/**
	 * Write using supplied memory buffer for ioctl.
	 */
	public void write(I2cAddress address, Memory writeBuf) throws CException {
		write(address, size(writeBuf), writeBuf);
	}
	
	/**
	 * I2C write using supplied memory buffer for ioctl.
	 */
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws CException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		I2cUtil.populate(msg, address, writeLen, writeBuf);
		I2cDev.i2c_rdwr(fd.fd(), msg);
	}

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	public void writeRead(I2cAddress address, Memory writeBuf, Memory readBuf) throws CException {
		writeRead(address, writeBuf, size(writeBuf), readBuf, size(readBuf));
	}
	
	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws CException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, writeLen, writeBuf);
		I2cUtil.populate(msgs[1], address, readLen, readBuf, I2C_M_RD);
		I2cDev.i2c_rdwr(fd.fd(), msgs);
	}

	@Override
	public void close() {
		LogUtil.close(logger, fd);
	}

	/* Start: Shared with SMBus */

	FileDescriptor fd() {
		return fd;
	}

	void selectDevice(I2cAddress address) throws CException {
		setAddressBits(address.tenBit);
		try {
			setSlaveAddress(address.address);
		} catch (CException e) {
			logger.info("Failed to set slave address, trying force: {}", e);
			forceSlaveAddress(address.address);
		}
	}

	boolean smBusPec() {
		return state.pec;
	}

	/* End: Shared with SMBus */

	private void setSlaveAddress(int address) throws CException {
		if (state.address == address) return;
		I2cDev.i2c_slave(fd.fd(), address);
		state.address = address;
	}

	private void forceSlaveAddress(int address) throws CException {
		I2cDev.i2c_slave_force(fd.fd(), address);
		state.address = address;
	}

	private void setAddressBits(boolean tenBit) throws CException {
		if (state.tenBit == tenBit) return;
		I2cDev.i2c_tenbit(fd.fd(), tenBit);
		state.address = State.NO_ADDRESS;
		state.tenBit = tenBit;
	}

	private void validate7Bit(I2cAddress address) {
		if (address.tenBit) throw new UnsupportedOperationException(
			"Operation not supported for 10-bit addresses: " + address);
	}

}
