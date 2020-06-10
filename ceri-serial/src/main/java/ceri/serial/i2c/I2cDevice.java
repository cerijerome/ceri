package ceri.serial.i2c;

import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.util.EqualsUtil;
import ceri.log.util.LogUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.smbus.SmBus;
import ceri.serial.i2c.smbus.SmBusDevice;
import ceri.serial.i2c.smbus.SmBusI2c;
import ceri.serial.i2c.util.I2cUtil;

/**
 * Implementation of I2c interface using the standard Linux i2c driver.
 * Not thread-safe.
 */
public class I2cDevice implements I2c {
	private static final Logger logger = LogManager.getLogger();
	private final FileDescriptor fd;
	private final State state = new State();

	/**
	 * Keep state to avoid unneeded ioctl calls.
	 */
	private static class State {
		Integer address = null;
		Boolean tenBit = null;
		Boolean pec = null;
		
		public void reset() {
			address = null;
			tenBit = null;
			pec = null;
		}
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

	@Override
	public I2cDevice retries(int count) throws CException {
		I2cDev.i2c_retries(fd.fd(), count);
		return this;
	}

	@Override
	public I2cDevice timeout(int timeoutMs) throws CException {
		I2cDev.i2c_timeout(fd.fd(), timeoutMs);
		return this;
	}

	@Override
	public Set<i2c_func> functions() throws CException {
		return i2c_func.xcoder.decodeAll(I2cDev.i2c_funcs(fd.fd()));
	}

	@Override
	public void smBusPec(boolean on) throws CException {
		I2cDev.i2c_smbus_pec(fd.fd(), on);
		state.pec = on;
	}

	@Override
	public SmBus smBus(I2cAddress address) {
		I2cUtil.validate7Bit(address);
		return SmBusDevice.of(fd::fd, this::selectDevice, address);
	}

	/**
	 * Provide SMBus functionality using I2C read-writes calls instead of SMBUS direct calls. SMBus
	 * only supports 7-bit addresses.
	 */
	public SmBus smBusI2c(I2cAddress address) {
		I2cUtil.validate7Bit(address);
		return SmBusI2c.of(this::transfer, this::smBusPec, address);
	}

	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws CException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		I2cUtil.populate(msg, address, writeLen, writeBuf);
		transfer(msg);
	}

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

	private void selectDevice(I2cAddress address) throws CException {
		setAddressBits(address.tenBit);
		try {
			setSlaveAddress(address.address);
		} catch (CException e) {
			logger.info("Failed to set slave address, trying force: {}", e);
			forceSlaveAddress(address.address);
		}
	}

	/**
	 * Returns whether user-mode PEC is enabled. If unknown state (null), assume false.
	 * If incorrect, SMBus I2C emulation will fail from invalid PEC. No need to throw an 
	 * exception here to force a failure.
	 */
	boolean smBusPec() {
		return state.pec != null && state.pec;
	}

	void transfer(i2c_msg.ByReference... msgs) throws CException {
		I2cDev.i2c_rdwr(fd.fd(), msgs);
	}

	/* End: Shared with SMBus */

	private void setSlaveAddress(int address) throws CException {
		if (state.address != null && state.address == address) return;
		I2cDev.i2c_slave(fd.fd(), address);
		state.address = address;
	}

	private void forceSlaveAddress(int address) throws CException {
		I2cDev.i2c_slave_force(fd.fd(), address);
		state.address = address;
	}

	private void setAddressBits(boolean tenBit) throws CException {
		if (state.tenBit != null && state.tenBit == tenBit) return;
		I2cDev.i2c_tenbit(fd.fd(), tenBit);
		state.address = null;
		state.tenBit = tenBit;
	}

}
