package ceri.serial.i2c.util;

import static ceri.serial.i2c.I2cAddress.DEVICE_ID;
import static ceri.serial.i2c.I2cAddress.GENERAL_CALL;
import java.io.IOException;
import ceri.common.data.ByteProvider;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.test.CallSync;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;

public class I2cSlaveDeviceEmulator implements I2cEmulator.SlaveDevice {
	public final I2cAddress address;
	public final CallSync.Consumer<ByteProvider> write = CallSync.consumer(null, true);
	public final CallSync.Function<Read, ByteProvider> read = CallSync.function(null);
	public final CallSync.Runnable softwareReset = CallSync.runnable(true);
	public final CallSync.Supplier<DeviceId> deviceId = CallSync.supplier(DeviceId.NONE);

	public record Read(ByteProvider command, int len) {
		public ByteProvider defaultResponse() {
			return ByteProvider.of(new byte[Math.max(0, len)]);
		}
	}

	public static I2cSlaveDeviceEmulator of(I2cAddress address) {
		return new I2cSlaveDeviceEmulator(address);
	}

	protected I2cSlaveDeviceEmulator(I2cAddress address) {
		this.address = address;
		read.autoResponse(Read::defaultResponse);
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

	private void write(byte[] command) throws IOException {
		write.accept(ByteProvider.of(command), ExceptionAdapter.io);
	}

	private byte[] read(byte[] command, int readLen) throws IOException {
		var read = new Read(ByteProvider.of(command), readLen);
		var response = this.read.apply(read, ExceptionAdapter.io);
		return response == null ? null : response.copy(0);
	}

	private void softwareReset() throws IOException {
		softwareReset.run(ExceptionAdapter.io);
	}

	private DeviceId deviceId() throws IOException {
		return deviceId.get(ExceptionAdapter.io);
	}

	private static boolean isSoftwareReset(byte[] command) {
		return command.length == 1 && command[0] == I2cUtil.SOFTWARE_RESET;
	}

	private byte[] readDeviceId(byte[] command) throws IOException {
		I2cAddress address = I2cAddress.fromFrames(command);
		if (!this.address.equals(address)) return null;
		return deviceId().encodeBytes();
	}
}
