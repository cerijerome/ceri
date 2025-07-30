package ceri.serial.i2c.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.i2c.util.I2cUtil.SOFTWARE_RESET;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cSlaveDeviceEmulator.Read;

public class I2cEmulatorBehavior {
	private static final I2cAddress address = I2cAddress.of(0x5b);
	private static final I2cAddress badAddress = I2cAddress.of(0x6c);
	private I2cEmulator i2c;
	private I2cSlaveDeviceEmulator dev;

	@After
	public void after() {
		i2c = null;
		dev = null;
	}

	@Test
	public void shouldWriteToDevice() throws IOException {
		init(address);
		i2c.writeData(address, 1, 2, 3);
		dev.write.assertAuto(provider(1, 2, 3));
		i2c.write(address, 0, null);
		dev.write.assertAuto(ByteProvider.empty());
	}

	@Test
	public void shouldWriteToDevices() throws IOException {
		init(address);
		var dev2 = I2cSlaveDeviceEmulator.of(I2cAddress.of(0x5c));
		dev2.addTo(i2c);
		i2c.writeData(I2cAddress.GENERAL_CALL, 0xff); // ignored
		i2c.writeData(I2cAddress.of(0x07), 1, 2, 3); // ignored
		i2c.writeData(I2cAddress.GENERAL_CALL, SOFTWARE_RESET, 0); // ignored
		i2c.softwareReset();
		dev.softwareReset.awaitAuto();
		dev2.softwareReset.awaitAuto();
		dev.write.assertCalls(0);
		dev2.write.assertCalls(0);
	}

	@Test
	public void shouldWriteAndRead() {
		// I2cEmulator.();
	}

	@Test
	public void shouldReadFromDevice() throws IOException {
		init(address);
		dev.read.autoResponses(provider(4, 5, 6));
		i2c.readData(address, ArrayUtil.bytes.of(1, 2, 3), 3);
		dev.read.assertAuto(new Read(provider(1, 2, 3), 3));
	}

	@Test
	public void shouldReadDeviceId() throws IOException {
		init(address);
		assertThrown(() -> i2c.deviceId(badAddress));
		DeviceId id = DeviceId.of(8, 7, 2);
		dev.deviceId.autoResponses(id);
		assertEquals(i2c.deviceId(address), id);
	}

	@Test
	public void shouldFailWriteForBadDevice() {
		init(address);
		assertThrown(() -> i2c.writeData(badAddress, 1, 2, 3));
		dev.write.error.setFrom(IOX);
		assertThrown(() -> i2c.writeData(address, 1, 2, 3));
	}

	@Test
	public void shouldFailReadForBadDevice() {
		init(address);
		assertThrown(() -> i2c.readData(badAddress, ArrayUtil.bytes.of(1, 2, 3), 3));
		dev.read.autoResponses((ByteProvider) null);
		assertThrown(() -> i2c.readData(address, ArrayUtil.bytes.of(1, 2, 3), 3));
		dev.read.autoResponses(provider(1, 2, 3));
		assertThrown(() -> i2c.readData(address, ArrayUtil.bytes.of(1, 2, 3), 2));
		assertThrown(() -> i2c.readData(I2cAddress.of(0x05), ArrayUtil.bytes.of(1, 2, 3), 3));
		dev.read.error.setFrom(IOX);
		assertThrown(() -> i2c.readData(address, ArrayUtil.bytes.of(1, 2, 3), 3));
	}

	@Test
	public void shouldProvideDefaultDevice() throws IOException {
		init(address);
		i2c.writeData(address, 1, 2, 3);
		assertArray(i2c.readData(address, ArrayUtil.bytes.of(1, 2, 3), 3), 0, 0, 0);
		assertEquals(i2c.deviceId(address), DeviceId.NONE);
		i2c.softwareReset();
		i2c.remove(dev.address);
	}

	private void init(I2cAddress address) {
		i2c = I2cEmulator.of(0);
		dev = I2cSlaveDeviceEmulator.of(address);
		dev.addTo(i2c);
	}
}
