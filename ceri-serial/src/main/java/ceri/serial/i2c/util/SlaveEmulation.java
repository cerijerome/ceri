package ceri.serial.i2c.util;

import static ceri.serial.i2c.I2cAddress.DEVICE_ID;
import static ceri.serial.i2c.I2cAddress.GENERAL_CALL;
import java.io.IOException;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cUtil;

public class SlaveEmulation implements I2cEmulator.Slave {
	public final I2cAddress address;

	/**
	 * Creates a slave device emulation from functions.
	 */
	public static SlaveEmulation from(I2cAddress address,
		ExceptionConsumer<IOException, byte[]> writeFn,
		ExceptionFunction<IOException, byte[], byte[]> readFn,
		ExceptionRunnable<IOException> softwareResetFn,
		ExceptionSupplier<IOException, DeviceId> deviceIdFn) {
		return new SlaveEmulation(address) {
			@Override
			protected void write(byte[] command) throws IOException {
				if (writeFn != null) writeFn.accept(command);
			}

			@Override
			protected byte[] read(byte[] command) throws IOException {
				return readFn != null ? readFn.apply(command) : null;
			}

			@Override
			protected void softwareReset() throws IOException {
				if (softwareResetFn != null) softwareResetFn.run();
			}

			@Override
			protected DeviceId deviceId() throws IOException {
				return deviceIdFn != null ? deviceIdFn.get() : null;
			}
		};
	}

	private SlaveEmulation(I2cAddress address) {
		this.address = address;
	}

	public SlaveEmulation addTo(I2cEmulator i2c) {
		i2c.add(address, this);
		return this;
	}
	
	@Override
	public byte[] read(I2cAddress address, byte[] command) throws IOException {
		if (this.address.equals(address)) return read(command);
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
	protected byte[] read(byte[] command) throws IOException {
		return null;
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
		if (command == null || command.length != 1) return false;
		return command[0] == I2cUtil.SOFTWARE_RESET;
	}

	private byte[] readDeviceId(byte[] command) throws IOException {
		I2cAddress address = I2cAddress.fromFrames(command);
		if (!this.address.equals(address)) return null;
		DeviceId deviceId = deviceId();
		if (deviceId == null) return null;
		return deviceId.encodeBytes();
	}
}
