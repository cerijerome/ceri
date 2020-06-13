package ceri.serial.i2c.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.jna.Pointer;
import ceri.common.function.FunctionUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.smbus.SmBus;
import ceri.serial.i2c.smbus.SmBusEmulator;
import ceri.serial.jna.JnaUtil;

/**
 * I2C emulator, where slave device emulators can register and respond to messages.
 */
public class I2cEmulator implements I2c {
	public static final int ANY_READ_LEN = -1;
	private final int hz;
	public final Map<I2cAddress, SlaveDevice> slaves = new ConcurrentHashMap<>();

	/**
	 * Interface for slave device emulation.
	 */
	public static interface SlaveDevice {
		/**
		 * Process a write message. Address may be a reserved address or the slave address.
		 */
		void write(I2cAddress address, byte[] command) throws IOException;

		/**
		 * Process a read message. Address may be a reserved address or the slave address. Read
		 * length can be specified as ANY_READ_LEN if the device determines the length.
		 */
		byte[] read(I2cAddress address, byte[] command, int readLen) throws IOException;
	}

	/**
	 * Static constructor with I2C frequency. The frequency is used to simulate I2C data transfer
	 * delays.
	 */
	public static I2cEmulator of(int hz) {
		return new I2cEmulator(hz);
	}

	private I2cEmulator(int hz) {
		this.hz = hz;
	}

	/**
	 * Add a slave device to receive messages.
	 */
	public SlaveDevice add(I2cAddress address, SlaveDevice slave) {
		return slaves.put(address, slave);
	}

	/**
	 * Remove the slave device from receiving messages.
	 */
	public SlaveDevice remove(I2cAddress address) {
		return slaves.remove(address);
	}

	@Override
	public I2c retries(int count) {
		return this;
	}

	@Override
	public I2c timeout(int timeoutMs) {
		return this;
	}

	@Override
	public Collection<i2c_func> functions() {
		return i2c_func.xcoder.all();
	}

	@Override
	public void smBusPec(boolean on) {}

	@Override
	public SmBus smBus(I2cAddress address) {
		return SmBusEmulator.of(this, address);
	}

	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException {
		byte[] command = JnaUtil.byteArray(writeBuf, 0, writeLen);
		slaveWrite(address, command);
	}

	@Override
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws IOException {
		byte[] command = JnaUtil.byteArray(writeBuf, 0, writeLen);
		byte[] response = slaveRead(address, command, readLen); // length checked in method
		JnaUtil.write(readBuf, response);
	}

	/**
	 * Execute a write for registered slave devices.
	 */
	public void slaveWrite(I2cAddress address, byte[] command) throws IOException {
		delay(address, command.length);
		if (address.isSlave()) slaveWrite(slaves.get(address), address, command);
		else FunctionUtil.forEach(slaves.values(), slave -> slaveWrite(slave, address, command));
	}

	/**
	 * Execute a read for registered slave devices.
	 */
	public byte[] slaveRead(I2cAddress address, byte[] command, int readLen) throws IOException {
		delay(address, command.length);
		if (address.isSlave()) {
			byte[] response = slaveRead(slaves.get(address), address, command, readLen);
			if (response == null) throw new IOException("No response from " + address);
			return response(address, response, readLen);
		}
		for (SlaveDevice slave : slaves.values()) {
			byte[] response = slaveRead(slave, address, command, readLen);
			if (response != null) return response(address, response, readLen);
		}
		throw new IOException("No response for " + address);
	}

	private byte[] response(I2cAddress address, byte[] response, int readLen) throws IOException {
		if (readLen != ANY_READ_LEN && readLen != response.length) throw new IOException(
			String.format("Unexpected response length from %s: %d, expected %d", address,
				response.length, readLen));
		delay(address, response.length);
		return response;
	}

	private void slaveWrite(SlaveDevice slave, I2cAddress address, byte[] command)
		throws IOException {
		if (slave == null) throw new IOException("Device not found at " + address);
		try {
			slave.write(address, command);
		} catch (IOException | RuntimeException e) {
			throw new IOException("Error from " + address, e);
		}
	}

	private byte[] slaveRead(SlaveDevice slave, I2cAddress address, byte[] command, int readLen)
		throws IOException {
		if (slave == null) throw new IOException("Device not found at " + address);
		try {
			return slave.read(address, command, readLen);
		} catch (IOException | RuntimeException e) {
			throw new IOException("Error from " + address, e);
		}
	}

	private void delay(I2cAddress address, int bytes) {
		long micros = I2cUtil.micros(hz, address, bytes);
		BasicUtil.delayMicros(micros);
	}

}
