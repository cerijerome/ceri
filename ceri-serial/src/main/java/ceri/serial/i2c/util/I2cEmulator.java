package ceri.serial.i2c.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.jna.Pointer;
import ceri.common.function.FunctionUtil;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.SmBus;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.jna.JnaUtil;

/**
 * I2C emulator, where listeners can register and respond to messages.
 */
public class I2cEmulator implements I2c {
	public final Map<I2cAddress, Slave> slaves = new ConcurrentHashMap<>();

	public static void main(String[] args) throws IOException {
		I2cAddress a0 = I2cAddress.of7Bit(0x33);
		I2cAddress a1 = I2cAddress.of7Bit(0x30);
		I2cAddress a2 = I2cAddress.of10Bit(0x33);
		try (I2cEmulator em = I2cEmulator.of()) {
			SlaveEmulation s0 = SlaveEmulation.from(a0, //
				null, null, null, () -> DeviceId.decode(0x55)).addTo(em);
			SlaveEmulation s1 = SlaveEmulation.from(a1, //
				null, null, null, () -> { throw new IOException(); }).addTo(em);
			SlaveEmulation s2 = SlaveEmulation.from(a2, //
				null, null, null, () -> DeviceId.decode(0xff)).addTo(em);
			System.out.println(em.deviceId(a1));
		}
	}

	/**
	 * Interface for slave device emulation.
	 */
	public static interface Slave {
		/**
		 * Receive a write message. Address may be a reserved address or the slave address.
		 */
		void write(I2cAddress address, byte[] command) throws IOException;

		/**
		 * Receive a read message. Address may be a reserved address or the slave address.
		 */
		byte[] read(I2cAddress address, byte[] command) throws IOException;
	}

	public static I2cEmulator of() {
		return new I2cEmulator();
	}

	private I2cEmulator() {}

	/**
	 * Add a slave device to receive messages.
	 */
	public Slave add(I2cAddress address, Slave slave) {
		return slaves.put(address, slave);
	}

	/**
	 * Remove the slave device from receiving messages.
	 */
	public Slave remove(I2cAddress address) {
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
	public SmBus smBus(I2cAddress address, boolean useI2c) {
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
		byte[] response = slaveRead(address, command);
		if (response.length > readLen) throw new IOException(
			String.format("Data response too big %s: %d > %d", address, response.length, readLen));
		JnaUtil.write(readBuf, response);
	}

	@Override
	public void close() {}

	public void slaveWrite(I2cAddress address, byte[] command) throws IOException {
		if (address.isSlave()) slaveWrite(slaves.get(address), address, command);
		else FunctionUtil.forEach(slaves.values(), slave -> slaveWrite(slave, address, command));
	}

	public byte[] slaveRead(I2cAddress address, byte[] command) throws IOException {
		if (address.isSlave()) {
			byte[] response = slaveRead(slaves.get(address), address, command);
			if (response == null) throw new IOException("No response from " + address);
			return response;
		}
		for (Slave slave : slaves.values()) {
			byte[] response = slaveRead(slave, address, command);
			if (response != null) return response;
		}
		throw new IOException("No response: " + address);
	}

	private void slaveWrite(Slave slave, I2cAddress address, byte[] command) throws IOException {
		if (slave == null) throw new IOException("Device not found: " + address);
		try {
			slave.write(address, command);
		} catch (IOException | RuntimeException e) {
			throw new IOException("Unexpected error from " + address, e);
		}
	}

	private byte[] slaveRead(Slave slave, I2cAddress address, byte[] command) throws IOException {
		if (slave == null) throw new IOException("Device not found: " + address);
		try {
			return slave.read(address, command);
		} catch (IOException | RuntimeException e) {
			throw new IOException("Unexpected error from " + address, e);
		}
	}

}
