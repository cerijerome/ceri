package ceri.serial.i2c.smbus;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cEmulator;
import ceri.serial.i2c.util.I2cSlaveDeviceEmulator;
import ceri.serial.i2c.util.I2cSlaveDeviceEmulator.Read;

public class SmBusEmulatorBehavior {
	private static final I2cAddress address = I2cAddress.of(0x5b);
	private I2cEmulator i2c;
	private I2cSlaveDeviceEmulator dev;
	private SmBusEmulator smBus;

	@After
	public void after() {
		i2c = null;
		dev = null;
		smBus = null;
	}

	@Test
	public void shouldWriteNumber() throws IOException {
		init(address);
		smBus.writeQuick(false);
		smBus.writeByte(0xa5);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		dev.write.assertValues( //
			provider(), //
			provider(0xa5), //
			provider(0x12, 0xa5), //
			provider(0x12, 0xb6, 0xa5));
	}

	@Test
	public void shouldWriteBytes() throws IOException {
		init(address);
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		dev.write.assertValues( //
			provider(0x12, 1, 2, 3), //
			provider(0x12, 1, 2, 3));
	}

	@Test
	public void shouldReadNumber() throws IOException {
		init(address);
		dev.read.autoResponses(provider(), provider(0xab), provider(0xab), provider(0xab, 0xcd));
		smBus.writeQuick(true); // actually a read
		assertEquals(smBus.readByte(), 0xab);
		assertEquals(smBus.readByteData(0x12), 0xab);
		assertEquals(smBus.readWordData(0x12), 0xcdab);
		assertEquals(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		dev.read.assertValues( //
			new Read(provider(), 0), //
			new Read(provider(), 1), //
			new Read(provider(0x12), 1), //
			new Read(provider(0x12), 2), //
			new Read(provider(0x12, 0xb6, 0xa5), 2));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		init(address);
		dev.read.autoResponses(provider(1, 2, 3));
		assertArray(smBus.readBlockData(0x12), 1, 2, 3);
		assertArray(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		assertArray(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		dev.read.assertValues( //
			new Read(provider(0x12), -1), //
			new Read(provider(0x12, 4, 5, 6), -1), //
			new Read(provider(0x12), 3));
	}

	private void init(I2cAddress address) {
		i2c = I2cEmulator.of(0);
		dev = I2cSlaveDeviceEmulator.of(address);
		dev.addTo(i2c);
		smBus = (SmBusEmulator) i2c.smBus(address);
	}
}
