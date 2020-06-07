package ceri.serial.i2c.smbus;

import java.io.IOException;
import java.util.function.IntSupplier;
import ceri.common.function.ExceptionConsumer;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev;

/**
 * Provides SMBus functionality directly. Sets address if needed before SMBus action.
 */
public class SmBusDevice implements SmBus {
	private final IntSupplier fdFn;
	private final ExceptionConsumer<IOException, I2cAddress> selectDeviceFn;
	private final I2cAddress address;

	public static SmBusDevice of(IntSupplier fdFn,
		ExceptionConsumer<IOException, I2cAddress> selectDeviceFn, I2cAddress address) {
		return new SmBusDevice(fdFn, selectDeviceFn, address);
	}

	private SmBusDevice(IntSupplier fdFn, ExceptionConsumer<IOException, I2cAddress> selectDeviceFn,
		I2cAddress address) {
		this.fdFn = fdFn;
		this.selectDeviceFn = selectDeviceFn;
		this.address = address;
	}

	@Override
	public void writeQuick(boolean on) throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_quick(fd(), on);
	}

	@Override
	public int readByte() throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_read_byte(fd());
	}

	@Override
	public void writeByte(int value) throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_byte(fd(), value);
	}

	@Override
	public int readByteData(int command) throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_read_byte_data(fd(), command);
	}

	@Override
	public void writeByteData(int command, int value) throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_byte_data(fd(), command, value);
	}

	@Override
	public int readWordData(int command) throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_read_word_data(fd(), command);
	}

	@Override
	public void writeWordData(int command, int value) throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_word_data(fd(), command, value);
	}

	@Override
	public int processCall(int command, int value) throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_process_call(fd(), command, value);
	}

	@Override
	public byte[] readBlockData(int command) throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_read_block_data(fd(), command);
	}

	@Override
	public void writeBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_block_data(fd(), command, values, offset, length);
	}

	@Override
	public byte[] readI2cBlockData(int command, int length) throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_read_i2c_block_data(fd(), command, length);
	}

	@Override
	public void writeI2cBlockData(int command, byte[] values, int offset, int length)
		throws IOException {
		selectDevice();
		I2cDev.i2c_smbus_write_i2c_block_data(fd(), command, values, offset, length);
	}

	@Override
	public byte[] blockProcessCall(int command, byte[] values, int offset, int length)
		throws IOException {
		selectDevice();
		return I2cDev.i2c_smbus_block_process_call(fd(), command, values);
	}

	private void selectDevice() throws IOException {
		selectDeviceFn.accept(address);
	}

	private int fd() {
		return fdFn.getAsInt();
	}

}
