package ceri.serial.i2c.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.i2c.util.I2cUtil.SOFTWARE_RESET;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;

public class I2cEmulatorBehavior {
	private static final I2cAddress address = I2cAddress.of(0x5b);
	private static final I2cAddress badAddress = I2cAddress.of(0x6c);
	private I2cEmulator i2c;
	private TestI2cSlave dev;

	@Before
	public void before() {
		i2c = I2cEmulator.of(1000000);
		dev = TestI2cSlave.of(address);
		dev.addTo(i2c);
	}

	@Test
	public void shouldWriteToDevice() throws IOException {
		i2c.writeData(address, 1, 2, 3);
		dev.write.assertAuto(provider(1, 2, 3));
	}

	@Test
	public void shouldWriteToDevices() throws IOException {
		var dev2 = TestI2cSlave.of(I2cAddress.of(0x5c));
		dev2.addTo(i2c);
		i2c.writeData(I2cAddress.GENERAL_CALL, 0xff); // ignored
		i2c.writeData(I2cAddress.of(0x07), 1, 2, 3); // ignored
		i2c.writeData(I2cAddress.GENERAL_CALL, SOFTWARE_RESET, 0); // ignored
		i2c.softwareReset();
		dev.softwareReset.awaitAuto();
		dev2.softwareReset.awaitAuto();
		dev.write.assertNoCall();
		dev2.write.assertNoCall();
	}

	@Test
	public void shouldReadFromDevice() throws IOException {
		dev.read.autoResponses(provider(4, 5, 6));
		i2c.readData(address, bytes(1, 2, 3), 3);
		dev.read.assertAuto(List.of(provider(1, 2, 3), 3));
	}

	@Test
	public void shouldReadDeviceId() throws IOException {
		assertThrown(() -> i2c.deviceId(badAddress));
		DeviceId id = DeviceId.of(8, 7, 2);
		dev.deviceId.autoResponses(id);
		assertEquals(i2c.deviceId(address), id);
	}

	@Test
	public void shouldFailWriteForBadDevice() {
		assertThrown(() -> i2c.writeData(badAddress, 1, 2, 3));
		dev.write.error.setFrom(IOX);
		assertThrown(() -> i2c.writeData(address, 1, 2, 3));
	}

	@Test
	public void shouldFailReadForBadDevice() {
		assertThrown(() -> i2c.readData(badAddress, bytes(1, 2, 3), 3));
		dev.read.autoResponses((ByteProvider) null);
		assertThrown(() -> i2c.readData(address, bytes(1, 2, 3), 3));
		dev.read.autoResponses(provider(1, 2, 3));
		assertThrown(() -> i2c.readData(address, bytes(1, 2, 3), 2));
		assertThrown(() -> i2c.readData(I2cAddress.of(0x05), bytes(1, 2, 3), 3));
		dev.read.error.setFrom(IOX);
		assertThrown(() -> i2c.readData(address, bytes(1, 2, 3), 3));
	}

	@Test
	public void shouldProvideDefaultDevice() throws IOException {
		var dev = new I2cSlaveDeviceEmulator(address) {};
		dev.addTo(i2c);
		i2c.writeData(address, 1, 2, 3);
		assertArray(i2c.readData(address, bytes(1, 2, 3), 3), 0, 0, 0);
		assertThrown(() -> i2c.deviceId(address));
		i2c.softwareReset();
		i2c.remove(dev.address);
	}

}
