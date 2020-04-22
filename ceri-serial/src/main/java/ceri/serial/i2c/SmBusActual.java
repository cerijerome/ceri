package ceri.serial.i2c;

import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.jna.I2cDev;

/**
 * Provides SMBus functionality directly. Sets address if needed before SMBus action.
 */
class SmBusActual implements SmBus {
	private final I2c i2c;
	private final I2cAddress address;

	SmBusActual(I2c i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_quick(fd(), on);
	}

	@Override
	public int readByte() throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_read_byte(fd());
	}

	@Override
	public void writeByte(int value) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_byte(fd(), value);
	}

	@Override
	public int readByteData(int command) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_read_byte_data(fd(), command);
	}

	@Override
	public void writeByteData(int command, int value) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_byte_data(fd(), command, value);
	}

	@Override
	public int readWordData(int command) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_read_word_data(fd(), command);
	}

	@Override
	public void writeWordData(int command, int value) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_word_data(fd(), command, value);
	}

	@Override
	public int processCall(int command, int value) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_process_call(fd(), command, value);
	}

	@Override
	public byte[] readBlockData(int command) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_read_block_data(fd(), command);
	}

	@Override
	public void writeBlockData(int command, byte... values) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_block_data(fd(), command, values);
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_read_i2c_block_data(fd(), command, length);
	}

	@Override
	public void writeI2cBlockData(int command, byte... values) throws CException {
		selectDevice();
		I2cDev.i2c_smbus_write_i2c_block_data(fd(), command, values);
	}

	@Override
	public byte[] blockProcessCall(int command, byte... values) throws CException {
		selectDevice();
		return I2cDev.i2c_smbus_block_process_call(fd(), command, values);
	}

	private void selectDevice() throws CException {
		i2c.selectDevice(address);
	}

	private int fd() {
		return i2c.fd().fd();
	}

}
