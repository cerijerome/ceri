package ceri.serial.i2c;

import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.io.IOException;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.log.util.LogUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_msg.ByReference;

/**
 * Implementation of I2c interface using the standard Linux i2c driver.
 */
public class I2cDevice implements I2c {
	private static final Logger logger = LogManager.getLogger();
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
	public static I2cDevice open(int bus) throws CException {
		if (bus < 0) throw new IllegalArgumentException("Invalid bus number: " + bus);
		return new I2cDevice(I2cDev.i2c_open(bus, O_RDWR.value));
	}

	private I2cDevice(int fd) {
		this.fd = FileDescriptor.of(fd);
	}

	/**
	 * Specify the number of times a device address should be polled when not acknowledging.
	 */
	@Override
	public I2cDevice retries(int count) throws CException {
		I2cDev.i2c_retries(fd.fd(), count);
		return this;
	}

	/**
	 * Specify the call timeout.
	 */
	@Override
	public I2cDevice timeout(int timeoutMs) throws CException {
		I2cDev.i2c_timeout(fd.fd(), timeoutMs);
		return this;
	}

	/**
	 * Determine functionality supported by the bus.
	 */
	@Override
	public Set<i2c_func> functions() throws CException {
		return i2c_func.xcoder.decodeAll(I2cDev.i2c_funcs(fd.fd()));
	}

	/**
	 * Enable or disable user-mode PEC.
	 */
	@Override
	public void smBusPec(boolean on) throws CException {
		I2cDev.i2c_smbus_pec(fd.fd(), on);
		state.pec = on;
	}

	/**
	 * Provide SMBus functionality. Emulated SMBus uses ioctl read-writes calls. SMBus only supports
	 * 7-bit addresses.
	 */
	@Override
	public SmBus smBus(I2cAddress address, boolean useI2c) {
		I2cUtil.validate7Bit(address);
		return useI2c ? SmBusI2c.of(smBusSupport(), address) : new SmBusDevice(this, address);
	}

	/**
	 * I2C write using supplied memory buffer for ioctl.
	 */
	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws CException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		I2cUtil.populate(msg, address, writeLen, writeBuf);
		transfer(msg);
	}

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	@Override
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws CException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, writeLen, writeBuf);
		I2cUtil.populate(msgs[1], address, readLen, readBuf, I2C_M_RD);
		transfer(msgs);
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

	/**
	 * Returns whether user-mode PEC is enabled.
	 */
	boolean smBusPec() {
		return state.pec;
	}

	void transfer(i2c_msg.ByReference... msgs) throws CException {
		I2cDev.i2c_rdwr(fd.fd(), msgs);
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

	private SmBusI2c.I2cSupport smBusSupport() {
		return new SmBusI2c.I2cSupport() {
			@Override
			public void transfer(ByReference... msgs) throws IOException {
				I2cDevice.this.transfer(msgs);
			}

			@Override
			public boolean smBusPec() {
				return I2cDevice.this.smBusPec();
			}
		};
	}

}
