package ceri.serial.i2c.util;

import java.io.IOException;
import java.util.Collection;
import com.sun.jna.Pointer;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.smbus.NullSmBus;
import ceri.serial.i2c.smbus.SmBus;

/**
 * A no-op implementation of I2c interface.
 */
public class NullI2c implements I2c {

	@Override
	public I2c retries(int count) throws IOException {
		return this;
	}

	@Override
	public I2c timeout(int timeoutMs) throws IOException {
		return this;
	}

	@Override
	public Collection<i2c_func> functions() throws IOException {
		return i2c_func.xcoder.all();
	}

	@Override
	public void smBusPec(boolean on) throws IOException {}

	@Override
	public SmBus smBus(I2cAddress address) {
		return NullSmBus.instance();
	}

	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException {}

	@Override
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws IOException {}

}
