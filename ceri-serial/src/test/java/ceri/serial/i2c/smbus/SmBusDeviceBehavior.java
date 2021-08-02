package ceri.serial.i2c.smbus;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.clib.jna.TestCLibUtil.autoError;
import static ceri.serial.i2c.jna.TestI2cCLibNative.smBusBlock;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cDevice;
import ceri.serial.i2c.jna.TestI2cCLibNative;

public class SmBusDeviceBehavior {
	private static final ByteProvider emptyBlock = smBusBlock();
	private static final I2cAddress address = I2cAddress.of(0x3b);
	private TestI2cCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private CFileDescriptor fd;
	private I2cDevice i2c;
	private SmBus smBus;

	@Before
	public void before() throws IOException {
		lib = TestI2cCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = I2cDevice.file(1);
		i2c = I2cDevice.of(fd);
		smBus = i2c.smBus(address);
	}

	@After
	public void after() throws IOException {
		fd.close();
		enc.close();
	}

	@Test
	public void shouldResetState() throws IOException {
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		lib.ioctlI2cInt.assertValues( // set address once
			List.of(0x704, 0), // 7-bit address
			List.of(0x703, 0x3b)); // address 0x3b
		i2c.reset();
		smBus.writeQuick(true);
		lib.ioctlI2cInt.assertValues( // set address again
			List.of(0x704, 0), // 7-bit address
			List.of(0x703, 0x3b)); // address 0x3b
	}

	@Test
	public void shouldForceAddressOnFailure() throws IOException {
		// Fail every I2C_SLAVE ioctl
		autoError(lib.ioctlI2cInt, 0, list -> (int) list.get(0) != 0x703, "I2C_SLAVE failure");
		smBus.writeQuick(true);
		lib.ioctlI2cInt.assertValues( // set address once
			List.of(0x704, 0), // 7-bit address
			List.of(0x706, 0x3b)); // force address 0x3b
	}

	@Test
	public void shouldProcessNumberWrites() throws IOException {
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlSmBusInt.assertValues( //
			List.of(1, 0, 0), //
			List.of(0, 0, 0), //
			List.of(0, 0xbc, 1), //
			List.of(0, 0x12, 2, 0xa5, 0, emptyBlock), //
			List.of(0, 0x12, 3, 0, 0xa5b6, emptyBlock));
	}

	@Test
	public void shouldProcessByteArrayWrites() throws IOException {
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlSmBusBytes.assertValues( //
			List.of(0, 0x12, 5, 0, 0, smBusBlock(3, 1, 2, 3)), //
			List.of(0, 0x12, 8, 0, 0, smBusBlock(3, 1, 2, 3))); // libusb converts to [1, 2, 3]?
	}

	@Test
	public void shouldProcessNumberReads() throws IOException {
		lib.ioctlSmBusInt.autoResponses(0xabcd);
		assertEquals(smBus.readByte(), 0xcd);
		assertEquals(smBus.readByteData(0x12), 0xcd);
		assertEquals(smBus.readWordData(0x12), 0xabcd);
		assertEquals(smBus.processCall(0x12, 0xa5b6), 0xabcd);
		lib.ioctlSmBusInt.assertValues( //
			List.of(1, 0, 1, 0, 0, emptyBlock), //
			List.of(1, 0x12, 2, 0, 0, emptyBlock), //
			List.of(1, 0x12, 3, 0, 0, emptyBlock), //
			List.of(0, 0x12, 4, 0, 0xa5b6, emptyBlock));
	}

	@Test
	public void shouldProcessByteArrayReads() throws IOException {
		lib.ioctlSmBusBytes.autoResponses(provider(3, 1, 2, 3)); // len, (1, 2, 3)
		assertArray(smBus.readBlockData(0x12), 1, 2, 3);
		assertArray(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		assertArray(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlSmBusBytes.assertValues( //
			List.of(1, 0x12, 5, 0, 0, emptyBlock), //
			List.of(0, 0x12, 7, 0, 0, smBusBlock(3, 4, 5, 6)), //
			List.of(1, 0x12, 8, 0, 0, smBusBlock(3)));
	}

}
