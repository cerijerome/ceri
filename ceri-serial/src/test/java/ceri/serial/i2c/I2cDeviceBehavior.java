package ceri.serial.i2c;

import static ceri.jna.clib.test.TestCLibNative.autoError;
import static ceri.serial.i2c.jna.I2cDev.i2c_func.I2C_FUNC_SMBUS_EMUL;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteProvider;
import ceri.common.function.Closeables;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.jna.test.JnaAssert;
import ceri.jna.test.JnaTesting;
import ceri.jna.util.JnaLibrary;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.TestI2cCLibNative;
import ceri.serial.i2c.jna.TestI2cCLibNative.Bytes;
import ceri.serial.i2c.jna.TestI2cCLibNative.Int;

public class I2cDeviceBehavior {
	private JnaLibrary.Ref<TestI2cCLibNative> ref = TestI2cCLibNative.i2cRef();
	private CFileDescriptor fd;
	private I2cDevice i2c;

	@After
	public void after() {
		Closeables.close(fd, ref);
		fd = null;
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		initI2c();
		Assert.find(i2c, "%s.*fd=", I2cDevice.class.getSimpleName());
	}

	@Test
	public void shouldOpenDevice() throws IOException {
		var lib = initI2c();
		Assert.thrown(() -> I2cDevice.open(-1));
		lib.open.assertAuto(new OpenArgs("/dev/i2c-1", 2, 0)); // open fd
		lib.ioctlI2cInt.autoResponses(i2c_func.xcoder.encodeInt(I2C_FUNC_SMBUS_EMUL));
		Assert.unordered(i2c.functions(), I2C_FUNC_SMBUS_EMUL);
		lib.ioctlI2cInt.assertAuto(new Int(0x0705, 0)); // I2C_FUNCS, dummy 0
	}

	@Test
	public void shouldDetermineIfAddressExists() throws IOException {
		var lib = initI2c();
		Assert.yes(i2c.exists(I2cAddress.of(0x3b)));
		Assert.thrown(() -> i2c.exists(I2cAddress.of(0x1ab))); // 10-bit not supported
		lib.ioctlSmBusInt.error.setFrom(JnaTesting.LEX);
		Assert.no(i2c.exists(I2cAddress.of(0x3b)));
	}

	@Test
	public void shouldScanFor7BitAddresses() throws IOException {
		var lib = initI2c();
		var exists = Set.of(0x2a, 0x3b, 0x4c);
		var existAddrs = Streams.from(exists).map(I2cAddress::of).toSet();
		autoError(lib.ioctlI2cInt, 0, i -> i.request() == 0x704 || exists.contains(i.value()),
			"Address does not exist");
		Assert.unordered(i2c.scan7Bit(), existAddrs);
	}

	@Test
	public void shouldRetrieveDeviceId() throws IOException {
		var lib = initI2c();
		lib.ioctlI2cBytes.autoResponses(ByteProvider.of(0, 0xa2, 0x6d));
		Assert.equal(i2c.deviceId(I2cAddress.of10Bit(0x123)), DeviceId.decode(0xa26d));
		lib.ioctlI2cBytes.assertAuto(List.of(new Bytes(0x7c, 0, ByteProvider.of(0xf2, 0x23), 2),
			new Bytes(0x7c, 1, null, 3)));
	}

	@Test
	public void shouldConfigureDevice() throws IOException {
		var lib = initI2c();
		i2c.retries(5);
		i2c.timeout(777);
		i2c.smBusPec(true);
		lib.ioctlI2cInt.assertValues(new Int(0x0701, 5), // I2C_RETRIES, 5
			new Int(0x0702, 77), // I2C_TIMEOUT, 777/10
			new Int(0x0708, 1)); // I2C_PEC, true
	}

	@Test
	public void shouldSendSoftwareReset() throws IOException {
		var lib = initI2c();
		i2c.softwareReset();
		lib.ioctlI2cBytes.assertAuto(List.of(new Bytes(0, 0, ByteProvider.of(0x06), 1)));
	}

	@Test
	public void shouldReadData() throws IOException {
		var lib = initI2c();
		byte[] receive = new byte[3];
		lib.ioctlI2cBytes.autoResponses(ByteProvider.of(4, 5, 6));
		i2c.readData(I2cAddress.of(0x1ab), Array.BYTE.of(1, 2, 3), receive);
		Assert.array(receive, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(List.of(new Bytes(0x1ab, 0x10, ByteProvider.of(1, 2, 3), 3),
			new Bytes(0x1ab, 0x11, null, 3)));
	}

	@Test
	public void shouldWriteAndReadFromMemory() throws IOException {
		var lib = initI2c();
		var out = JnaTesting.mem(1, 2, 3);
		var in = JnaTesting.memSize(3);
		lib.ioctlI2cBytes.autoResponses(ByteProvider.of(4, 5, 6));
		i2c.writeRead(I2cAddress.of(0x1ab), out.m, in.m);
		JnaAssert.memory(in.m, 0, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(List.of(new Bytes(0x1ab, 0x10, ByteProvider.of(1, 2, 3), 3),
			new Bytes(0x1ab, 0x11, null, 3)));
	}

	private TestI2cCLibNative initI2c() throws IOException {
		ref.init();
		fd = I2cDevice.open(1);
		i2c = I2cDevice.of(fd);
		return ref.get();
	}
}
