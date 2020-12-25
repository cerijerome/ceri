package ceri.serial.i2c.smbus;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cDevice;
import ceri.serial.i2c.jna.TestI2cCLibNative;

public class SmBusI2cBehavior {
	private static final I2cAddress address = I2cAddress.of(0x3b);
	private TestI2cCLibNative lib;
	private Enclosed<?> enc;
	private CFileDescriptor fd;
	private I2cDevice i2c;
	private SmBus smBus;

	@Before
	public void before() throws IOException {
		lib = TestI2cCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = I2cDevice.file(1);
		i2c = I2cDevice.of(fd);
		smBus = i2c.smBusI2c(address);
	}

	@After
	public void after() throws IOException {
		fd.close();
		enc.close();
	}

	@Test
	public void shouldSupport10BitAddress() throws IOException {
		smBus = i2c.smBusI2c(I2cAddress.of(0x3bc));
		smBus.writeByte(0xbc);
		lib.ioctlI2cBytes.assertValues(List.of(0x3bc, 0x10, provider(0xbc)));
	}

	@Test
	public void shouldProcessNumberWrites() throws IOException {
		i2c.smBusPec(false);
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 1, 0), //
			List.of(0x3b, 0, provider()), //
			List.of(0x3b, 0, provider(0xbc)), //
			List.of(0x3b, 0, provider(0x12, 0xa5)), //
			List.of(0x3b, 0, provider(0x12, 0xb6, 0xa5)));
	}

	@Test
	public void shouldProcessNumberWritesWithPec() throws IOException {
		i2c.smBusPec(true);
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 1, 0), // no PEC
			List.of(0x3b, 0, provider()), // no PEC
			List.of(0x3b, 0, provider(0xbc, 0xe1)), // generated PEC
			List.of(0x3b, 0, provider(0x12, 0xa5, 0x15)), // generated PEC
			List.of(0x3b, 0, provider(0x12, 0xb6, 0xa5, 0x71))); // generated PEC
	}

	@Test
	public void shouldProcessByteArrayWrites() throws IOException {
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 0, provider(0x12, 3, 1, 2, 3)), //
			List.of(0x3b, 0, provider(0x12, 1, 2, 3)));
	}

	@Test
	public void shouldProcessByteArrayWritesWithPec() throws IOException {
		i2c.smBusPec(true);
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 0, provider(0x12, 3, 1, 2, 3, 0xa1)), // generated PEC
			List.of(0x3b, 0, provider(0x12, 1, 2, 3, 0xd6))); // generated PEC
	}

	@Test
	public void shouldProcessNumberReads() throws IOException {
		lib.ioctlI2cBytes.autoResponses(provider(0xab), provider(0xab, 0xcd));
		assertEquals(smBus.readByte(), 0xab);
		assertEquals(smBus.readByteData(0x12), 0xab);
		assertEquals(smBus.readWordData(0x12), 0xcdab);
		assertEquals(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 1, 1), //
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 1), //
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 2), //
			List.of(0x3b, 0, provider(0x12, 0xb6, 0xa5), 0x3b, 1, 2));
	}

	@Test
	public void shouldProcessNumberReadsWithPec() throws IOException {
		i2c.smBusPec(true);
		lib.ioctlI2cBytes.autoResponses( // different PEC per request, verified
			provider(0xab, 0x91), //
			provider(0xab, 0xa3), //
			provider(0xab, 0xcd, 0x0d), //
			provider(0xab, 0xcd, 0x9f)); //
		assertEquals(smBus.readByte(), 0xab);
		assertEquals(smBus.readByteData(0x12), 0xab);
		assertEquals(smBus.readWordData(0x12), 0xcdab);
		assertEquals(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		lib.ioctlI2cBytes.assertValues( // no generated PECs
			List.of(0x3b, 1, 2), //
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 2), //
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 3), //
			List.of(0x3b, 0, provider(0x12, 0xb6, 0xa5), 0x3b, 1, 3));
	}

	@Test
	public void shouldProcessByteArrayReads() throws IOException {
		lib.ioctlI2cBytes.autoResponses( //
			provider(3, 1, 2, 3), //
			provider(3, 1, 2, 3), //
			provider(1, 2, 3));
		assertArray(smBus.readBlockData(0x12), 1, 2, 3);
		assertArray(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		assertArray(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( //
			List.of(0x3b, 0, provider(0x12), 0x3b, 0x401, 1),
			List.of(0x3b, 0, provider(0x12, 3, 4, 5, 6), 0x3b, 0x401, 1),
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 3));
	}

	@Test
	public void shouldProcessByteArrayReadsWithPec() throws IOException {
		i2c.smBusPec(true);
		lib.ioctlI2cBytes.autoResponses( // different PEC per request, verified
			provider(3, 1, 2, 3, 0xf2), //
			provider(3, 1, 2, 3, 0x95), //
			provider(1, 2, 3, 0xcb));
		assertArray(smBus.readBlockData(0x12), 1, 2, 3);
		assertArray(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		assertArray(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( // no generated PECs
			List.of(0x3b, 0, provider(0x12), 0x3b, 0x401, 1),
			List.of(0x3b, 0, provider(0x12, 3, 4, 5, 6), 0x3b, 0x401, 1),
			List.of(0x3b, 0, provider(0x12), 0x3b, 1, 4));
	}

}
