package ceri.serial.i2c.smbus;

import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.util.JnaLibrary;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cDevice;
import ceri.serial.i2c.jna.TestI2cCLibNative;
import ceri.serial.i2c.jna.TestI2cCLibNative.Bytes;

public class SmBusI2cBehavior {
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
	public void shouldSupport10BitAddress() throws IOException {
		var lib = initI2c();
		smBus = i2c.smBusI2c(I2cAddress.of(0x3bc));
		smBus.writeByte(0xbc);
		lib.ioctlI2cBytes.assertValues(List.of(new Bytes(0x3bc, 0x10, provider(0xbc), 1)));
	}

	@Test
	public void shouldProcessNumberWrites() throws IOException {
		var lib = initI2c();
		i2c.smBusPec(false);
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlI2cBytes.assertValues( //
			List.of(new Bytes(0x3b, 1, null, 0)), //
			List.of(new Bytes(0x3b, 0, provider(), 0)), //
			List.of(new Bytes(0x3b, 0, provider(0xbc), 1)), //
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xa5), 2)), //
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xb6, 0xa5), 3)));
	}

	@Test
	public void shouldProcessNumberWritesWithPec() throws IOException {
		var lib = initI2c();
		i2c.smBusPec(true);
		smBus.writeQuick(true);
		smBus.writeQuick(false);
		smBus.writeByte(0xbc);
		smBus.writeByteData(0x12, 0xa5);
		smBus.writeWordData(0x12, 0xa5b6);
		lib.ioctlI2cBytes.assertValues( //
			List.of(new Bytes(0x3b, 1, null, 0)), // no PEC
			List.of(new Bytes(0x3b, 0, provider(), 0)), // no PEC
			List.of(new Bytes(0x3b, 0, provider(0xbc, 0xe1), 2)), // generated PEC
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xa5, 0x15), 3)), // generated PEC
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xb6, 0xa5, 0x71), 4))); // generated PEC
	}

	@Test
	public void shouldProcessByteArrayWrites() throws IOException {
		var lib = initI2c();
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( //
			List.of(new Bytes(0x3b, 0, provider(0x12, 3, 1, 2, 3), 5)),
			List.of(new Bytes(0x3b, 0, provider(0x12, 1, 2, 3), 4)));
	}

	@Test
	public void shouldProcessByteArrayWritesWithPec() throws IOException {
		var lib = initI2c();
		i2c.smBusPec(true);
		smBus.writeBlockData(0x12, 1, 2, 3);
		smBus.writeI2cBlockData(0x12, 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( //
			List.of(new Bytes(0x3b, 0, provider(0x12, 3, 1, 2, 3, 0xa1), 6)), // generated PEC
			List.of(new Bytes(0x3b, 0, provider(0x12, 1, 2, 3, 0xd6), 5))); // generated PEC
	}

	@Test
	public void shouldProcessNumberReads() throws IOException {
		var lib = initI2c();
		lib.ioctlI2cBytes.autoResponses(provider(0xab), provider(0xab, 0xcd));
		Assert.equal(smBus.readByte(), 0xab);
		Assert.equal(smBus.readByteData(0x12), 0xab);
		Assert.equal(smBus.readWordData(0x12), 0xcdab);
		Assert.equal(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		lib.ioctlI2cBytes.assertValues( //
			List.of(new Bytes(0x3b, 1, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 2)), //
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xb6, 0xa5), 3),
				new Bytes(0x3b, 1, null, 2)));
	}

	@Test
	public void shouldProcessNumberReadsWithPec() throws IOException {
		var lib = initI2c();
		i2c.smBusPec(true);
		lib.ioctlI2cBytes.autoResponses( // different PEC per request, verified
			provider(0xab, 0x91), //
			provider(0xab, 0xa3), //
			provider(0xab, 0xcd, 0x0d), //
			provider(0xab, 0xcd, 0x9f)); //
		Assert.equal(smBus.readByte(), 0xab);
		Assert.equal(smBus.readByteData(0x12), 0xab);
		Assert.equal(smBus.readWordData(0x12), 0xcdab);
		Assert.equal(smBus.processCall(0x12, 0xa5b6), 0xcdab);
		lib.ioctlI2cBytes.assertValues( // no generated PECs
			List.of(new Bytes(0x3b, 1, null, 2)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 2)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 3)), //
			List.of(new Bytes(0x3b, 0, provider(0x12, 0xb6, 0xa5), 3),
				new Bytes(0x3b, 1, null, 3)));
	}

	@Test
	public void shouldProcessByteArrayReads() throws IOException {
		var lib = initI2c();
		lib.ioctlI2cBytes.autoResponses( //
			provider(3, 1, 2, 3), //
			provider(3, 1, 2, 3), //
			provider(1, 2, 3));
		Assert.array(smBus.readBlockData(0x12), 1, 2, 3);
		Assert.array(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		Assert.array(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlI2cBytes.assertValues(
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 0x401, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12, 3, 4, 5, 6), 5),
				new Bytes(0x3b, 0x401, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 3)));
	}

	@Test
	public void shouldProcessByteArrayReadsWithPec() throws IOException {
		var lib = initI2c();
		i2c.smBusPec(true);
		lib.ioctlI2cBytes.autoResponses( // different PEC per request, verified
			provider(3, 1, 2, 3, 0xf2), //
			provider(3, 1, 2, 3, 0x95), //
			provider(1, 2, 3, 0xcb));
		Assert.array(smBus.readBlockData(0x12), 1, 2, 3);
		Assert.array(smBus.blockProcessCall(0x12, 4, 5, 6), 1, 2, 3);
		Assert.array(smBus.readI2cBlockData(0x12, 3), 1, 2, 3);
		lib.ioctlI2cBytes.assertValues( // no generated PECs
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 0x401, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12, 3, 4, 5, 6), 5),
				new Bytes(0x3b, 0x401, null, 1)),
			List.of(new Bytes(0x3b, 0, provider(0x12), 1), new Bytes(0x3b, 1, null, 4)));
	}

	private TestI2cCLibNative initI2c() throws IOException {
		ref.init();
		fd = I2cDevice.open(1);
		i2c = I2cDevice.of(fd);
		smBus = i2c.smBusI2c(address);
		return ref.get();
	}
}
