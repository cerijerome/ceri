package ceri.serial.i2c;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.io.IOException;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.jna.CException;
import ceri.serial.i2c.jna.I2cDev;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.smbus.SmBus;
import ceri.serial.i2c.smbus.SmBusDevice;
import ceri.serial.i2c.smbus.SmBusI2c;
import ceri.serial.i2c.util.I2cUtil;

/**
 * Implementation of I2c interface using the standard Linux i2c driver. Not thread-safe.
 */
public class I2cDevice implements I2c {
	private static final Logger logger = LogManager.getLogger();
	private final FileDescriptor fd;
	private final State state = new State();

	/**
	 * Keep state to avoid unneeded ioctl calls for SMBus (direct and emulated).
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
	 * Open a file descriptor to the I2C bus. Can be used as the open function for a SelfHealingFd.
	 */
	public static CFileDescriptor open(int bus) throws IOException {
		validateMin(bus, 0, "Bus number");
		return CFileDescriptor.of(I2cDev.i2c_open(bus, Open.RDWR.value));
	}

	public static I2cDevice of(FileDescriptor fd) {
		return new I2cDevice(fd);
	}

	private I2cDevice(FileDescriptor fd) {
		this.fd = fd;
	}

	/**
	 * Call if file descriptor has been reset, and state is now unknown.
	 */
	@Override
	public void reset() {
		state.reset();
	}

	@Override
	public I2cDevice retries(int count) throws IOException {
		fd.accept(fd -> I2cDev.i2c_retries(fd, count));
		return this;
	}

	@Override
	public I2cDevice timeout(int timeoutMs) throws IOException {
		fd.accept(fd -> I2cDev.i2c_timeout(fd, timeoutMs));
		return this;
	}

	@Override
	public Set<i2c_func> functions() throws IOException {
		return i2c_func.xcoder.decodeAll(fd.apply(I2cDev::i2c_funcs));
	}

	@Override
	public void smBusPec(boolean on) throws IOException {
		fd.accept(fd -> I2cDev.i2c_smbus_pec(fd, on));
		state.pec = on;
	}

	/**
	 * Provide SMBus functionality using direct ioctl calls. Direct SMBus only supports 7-bit
	 * addresses.
	 */
	@Override
	public SmBus smBus(I2cAddress address) throws IOException {
		I2cUtil.validate7Bit(address);
		return SmBusDevice.of(fd, this::selectDevice, address);
	}

	/**
	 * Provide SMBus functionality using I2C read-writes calls instead of SMBus direct calls.
	 * Emulated SMBus supports 7-bit and 10-bit addresses.
	 */
	public SmBus smBusI2c(I2cAddress address) {
		return SmBusI2c.of(this::transfer, this::smBusPec, address);
	}

	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		I2cUtil.populate(msg, address, writeLen, writeBuf);
		transfer(msg);
	}

	@Override
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws IOException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, writeLen, writeBuf);
		I2cUtil.populate(msgs[1], address, readLen, readBuf, I2C_M_RD);
		transfer(msgs);
	}

	/* Shared with emulated SMBus */

	/**
	 * Returns whether user-mode PEC is enabled. If unknown state (null), assume false. If
	 * incorrect, SMBus I2C emulation will fail from invalid PEC. No need to throw an exception here
	 * to force a failure.
	 */
	private boolean smBusPec() {
		return state.pec != null && state.pec;
	}

	private void transfer(i2c_msg.ByReference... msgs) throws IOException {
		fd.accept(fd -> I2cDev.i2c_rdwr(fd, msgs));
	}

	/* Shared with direct SMBus */

	private void selectDevice(I2cAddress address) throws IOException {
		setAddressBits(address.tenBit);
		try {
			setSlaveAddress(address.address);
		} catch (CException e) {
			logger.info("Failed to set slave address, trying force: {}", e);
			forceSlaveAddress(address.address);
		}
	}

	private void setSlaveAddress(int address) throws IOException {
		if (state.address != null && state.address == address) return;
		fd.accept(fd -> I2cDev.i2c_slave(fd, address));
		state.address = address;
	}

	private void forceSlaveAddress(int address) throws IOException {
		fd.accept(fd -> I2cDev.i2c_slave_force(fd, address));
		state.address = address;
	}

	private void setAddressBits(boolean tenBit) throws IOException {
		if (state.tenBit != null && state.tenBit == tenBit) return;
		fd.accept(fd -> I2cDev.i2c_tenbit(fd, tenBit));
		state.address = null;
		state.tenBit = tenBit;
	}

}
