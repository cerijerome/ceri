package ceri.serial.i2c.util;

import static ceri.serial.i2c.I2cAddress.DEVICE_ID;
import static ceri.serial.i2c.I2cAddress.GENERAL_CALL;
import java.io.IOException;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;

public class I2cSlaveDeviceEmulator implements I2cEmulator.SlaveDevice {
	public final I2cAddress address;

	protected I2cSlaveDeviceEmulator(I2cAddress address) {
		this.address = address;
	}

	public I2cSlaveDeviceEmulator addTo(I2cEmulator i2c) {
		i2c.add(address, this);
		return this;
	}

	@Override
	public byte[] read(I2cAddress address, byte[] command, int readLen) throws IOException {
		if (this.address.equals(address)) return read(command, readLen);
		if (DEVICE_ID.equals(address)) return readDeviceId(command);
		return null;
	}

	@Override
	public void write(I2cAddress address, byte[] command) throws IOException {
		if (this.address.equals(address)) write(command);
		else if (GENERAL_CALL.equals(address)) {
			if (isSoftwareReset(command)) softwareReset();
		}
	}

	/**
	 * Override for direct write command.
	 */
	@SuppressWarnings("unused")
	protected void write(byte[] command) throws IOException {}

	/**
	 * Override for direct read command.
	 */
	@SuppressWarnings("unused")
	protected byte[] read(byte[] command, int readLen) throws IOException {
		return new byte[readLen];
	}

	/**
	 * Override to act on a software reset.
	 */
	@SuppressWarnings("unused")
	protected void softwareReset() throws IOException {}

	/**
	 * Override to provide a device id.
	 */
	@SuppressWarnings("unused")
	protected DeviceId deviceId() throws IOException {
		return null;
	}

	private static boolean isSoftwareReset(byte[] command) {
		return command.length == 1 &&  command[0] == I2cUtil.SOFTWARE_RESET;
	}

	private byte[] readDeviceId(byte[] command) throws IOException {
		I2cAddress address = I2cAddress.fromFrames(command);
		if (!this.address.equals(address)) return null;
		DeviceId deviceId = deviceId();
		if (deviceId == null) return null;
		return deviceId.encodeBytes();
	}
}
