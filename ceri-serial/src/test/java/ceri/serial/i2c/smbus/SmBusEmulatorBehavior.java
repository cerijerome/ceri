package ceri.serial.i2c.smbus;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.test.Assert;
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
			ByteProvider.of(), //
			ByteProvider.of(0xa5), //
			ByteProvider.of(0x12, 0xa5), //
			ByteProvider.of(0x12, 0xb6, 0xa5));
	}

	@Test
	public void shouldWriteBytes() throws IOException {
		init(address);
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		dev.write.assertValues( //
			ByteProvider.of(0x12, 1, 2, 3), //
			ByteProvider.of(0x12, 1, 2, 3));
	}

	@Test
	public void shouldReadNumber() throws IOException {
		init(address);
		dev.read.autoResponses(ByteProvider.of(), ByteProvider.of(0xab), ByteProvider.of(0xab),
			ByteProvider.of(0xab, 0xcd));
		smBus.writeQuick(true); // actually a read
		Assert.equal(smBus.readByte(), 0xab);
		Assert.equal(smBus.readByteData(0x12), 0xab);
		Assert.equal(smBus.readWordData(0x12), 0xcdab);
		Assert.equal(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		dev.read.assertValues( //
			new Read(ByteProvider.of(), 0), //
			new Read(ByteProvider.of(), 1), //
			new Read(ByteProvider.of(0x12), 1), //
			new Read(ByteProvider.of(0x12), 2), //
			new Read(ByteProvider.of(0x12, 0xb6, 0xa5), 2));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		init(address);
		dev.read.autoResponses(ByteProvider.of(1, 2, 3));
		Assert.array(smBus.readBlockData(0x12), 1, 2, 3);
		Assert.array(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		Assert.array(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		dev.read.assertValues( //
			new Read(ByteProvider.of(0x12), -1), //
			new Read(ByteProvider.of(0x12, 4, 5, 6), -1), //
			new Read(ByteProvider.of(0x12), 3));
	}

	private void init(I2cAddress address) {
		i2c = I2cEmulator.of(0);
		dev = I2cSlaveDeviceEmulator.of(address);
		dev.addTo(i2c);
		smBus = (SmBusEmulator) i2c.smBus(address);
	}
}
