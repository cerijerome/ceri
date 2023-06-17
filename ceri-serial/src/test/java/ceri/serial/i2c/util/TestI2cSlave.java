package ceri.serial.i2c.util;

import static ceri.common.collection.ImmutableUtil.asList;
import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.util.List;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;

public class TestI2cSlave extends I2cSlaveDeviceEmulator {
	public final CallSync.Consumer<ByteProvider> write = CallSync.consumer(null, true);
	public final CallSync.Function<List<?>, ByteProvider> read =
		CallSync.function(null, ByteProvider.empty());
	public final CallSync.Runnable softwareReset = CallSync.runnable(true);
	public final CallSync.Supplier<DeviceId> deviceId = CallSync.supplier(DeviceId.NONE);

	public static TestI2cSlave of(I2cAddress address) {
		return new TestI2cSlave(address);
	}

	private TestI2cSlave(I2cAddress address) {
		super(address);
	}

	@Override
	protected void write(byte[] command) throws IOException {
		ByteProvider bp = command == null ? null : ByteProvider.of(command);
		write.accept(bp, IO_ADAPTER);
	}

	@Override
	protected byte[] read(byte[] command, int readLen) throws IOException {
		ByteProvider bp = command == null ? null : ByteProvider.of(command);
		ByteProvider response = read.apply(asList(bp, readLen), IO_ADAPTER);
		return response == null ? null : response.copy(0);
	}

	@Override
	protected void softwareReset() throws IOException {
		softwareReset.run(IO_ADAPTER);
	}

	@Override
	protected DeviceId deviceId() throws IOException {
		return deviceId.get(IO_ADAPTER);
	}
}
