package ceri.serial.i2c.smbus;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cEmulator;
import ceri.serial.i2c.util.TestI2cSlave;

public class SmBusEmulatorBehavior {
	private static final I2cAddress address = I2cAddress.of(0x5b);
	private I2cEmulator i2c;
	private TestI2cSlave dev;
	private SmBusEmulator smBus;

	@Before
	public void before() {
		i2c = I2cEmulator.of(1000000);
		dev = TestI2cSlave.of(address);
		dev.addTo(i2c);
		smBus = (SmBusEmulator) i2c.smBus(address);
	}

	@Test
	public void shouldWriteNumber() throws IOException {
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
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		dev.write.assertValues( //
			provider(0x12, 1, 2, 3), //
			provider(0x12, 1, 2, 3));
	}

	@Test
	public void shouldReadNumber() throws IOException {
		dev.read.autoResponses(provider(), provider(0xab), provider(0xab), provider(0xab, 0xcd));
		smBus.writeQuick(true); // actually a read
		assertEquals(smBus.readByte(), 0xab);
		assertEquals(smBus.readByteData(0x12), 0xab);
		assertEquals(smBus.readWordData(0x12), 0xcdab);
		assertEquals(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		dev.read.assertValues( //
			List.of(provider(), 0), //
			List.of(provider(), 1), //
			List.of(provider(0x12), 1), //
			List.of(provider(0x12), 2), //
			List.of(provider(0x12, 0xb6, 0xa5), 2));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		dev.read.autoResponses(provider(1, 2, 3));
		assertArray(smBus.readBlockData(0x12), 1, 2, 3);
		assertArray(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		assertArray(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		dev.read.assertValues( //
			List.of(provider(0x12), -1), //
			List.of(provider(0x12, 4, 5, 6), -1), //
			List.of(provider(0x12), 3));
	}

}
