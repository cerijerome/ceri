package ceri.serial.i2c.smbus;

import static ceri.jna.clib.test.TestCLibNative.autoError;
import static ceri.serial.i2c.jna.TestI2cCLibNative.smBusBlock;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.util.JnaLibrary;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cDevice;
import ceri.serial.i2c.jna.TestI2cCLibNative;
import ceri.serial.i2c.jna.TestI2cCLibNative.Int;
import ceri.serial.i2c.jna.TestI2cCLibNative.Rw;

public class SmBusDeviceBehavior {
	private static final ByteProvider emptyBlock = smBusBlock();
	private static final I2cAddress address = I2cAddress.of(0x3b);
	private final JnaLibrary.Ref<TestI2cCLibNative> ref = TestI2cCLibNative.i2cRef();
	private CFileDescriptor fd;
	private I2cDevice i2c;
	private SmBus smBus;

	@After
	public void after() {
		Closeables.close(fd, ref);
		fd = null;
	}

	@Test
	public void shouldResetState() throws IOException {
		var lib = initI2c();
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		lib.ioctlI2cInt.assertValues( // set address once
			new Int(0x704, 0), // 7-bit address
			new Int(0x703, 0x3b)); // address 0x3b
		i2c.reset();
		smBus.writeQuick(true);
		lib.ioctlI2cInt.assertValues( // set address again
			new Int(0x704, 0), // 7-bit address
			new Int(0x703, 0x3b)); // address 0x3b
	}

	@Test
	public void shouldForceAddressOnFailure() throws IOException {
		var lib = initI2c();
		// Fail every I2C_SLAVE ioctl
		autoError(lib.ioctlI2cInt, 0, i -> i.request() != 0x703, "I2C_SLAVE failure");
		smBus.writeQuick(true);
		lib.ioctlI2cInt.assertValues( // set address once
			new Int(0x704, 0), // 7-bit address
			new Int(0x706, 0x3b)); // force address 0x3b
	}

	@Test
	public void shouldProcessNumberWrites() throws IOException {
		var lib = initI2c();
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlSmBusInt.assertValues( //
			new Rw(1, 0, 0, 0, 0, null), //
			new Rw(0, 0, 0, 0, 0, null), //
			new Rw(0, 0xbc, 1, 0, 0, null), //
			new Rw(0, 0x12, 2, 0xa5, 0, emptyBlock), //
			new Rw(0, 0x12, 3, 0, 0xa5b6, emptyBlock));
	}

	@Test
	public void shouldProcessByteArrayWrites() throws IOException {
		var lib = initI2c();
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlSmBusBytes.assertValues( //
			new Rw(0, 0x12, 5, 0, 0, smBusBlock(3, 1, 2, 3)), //
			new Rw(0, 0x12, 8, 0, 0, smBusBlock(3, 1, 2, 3))); // libusb converts to [1, 2, 3]?
	}

	@Test
	public void shouldProcessNumberReads() throws IOException {
		var lib = initI2c();
		lib.ioctlSmBusInt.autoResponses(0xabcd);
		Assert.equal(smBus.readByte(), 0xcd);
		Assert.equal(smBus.readByteData(0x12), 0xcd);
		Assert.equal(smBus.readWordData(0x12), 0xabcd);
		Assert.equal(smBus.processCall(0x12, 0xa5b6), 0xabcd);
		lib.ioctlSmBusInt.assertValues( //
			new Rw(1, 0, 1, 0, 0, emptyBlock), //
			new Rw(1, 0x12, 2, 0, 0, emptyBlock), //
			new Rw(1, 0x12, 3, 0, 0, emptyBlock), //
			new Rw(0, 0x12, 4, 0, 0xa5b6, emptyBlock));
	}

	@Test
	public void shouldProcessByteArrayReads() throws IOException {
		var lib = initI2c();
		lib.ioctlSmBusBytes.autoResponses(ByteProvider.of(3, 1, 2, 3)); // len, (1, 2, 3)
		Assert.array(smBus.readBlockData(0x12), 1, 2, 3);
		Assert.array(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		Assert.array(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlSmBusBytes.assertValues( //
			new Rw(1, 0x12, 5, 0, 0, emptyBlock), //
			new Rw(0, 0x12, 7, 0, 0, smBusBlock(3, 4, 5, 6)), //
			new Rw(1, 0x12, 8, 0, 0, smBusBlock(3)));
	}

	private TestI2cCLibNative initI2c() throws IOException {
		ref.init();
		fd = I2cDevice.open(1);
		i2c = I2cDevice.of(fd);
		smBus = i2c.smBus(address);
		return ref.get();
	}
}
