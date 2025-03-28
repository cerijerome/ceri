package ceri.serial.i2c;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.StreamUtil.toSet;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.provider;
import static ceri.jna.clib.test.TestCLibNative.autoError;
import static ceri.jna.test.JnaTestUtil.LEX;
import static ceri.jna.test.JnaTestUtil.mem;
import static ceri.jna.test.JnaTestUtil.memSize;
import static ceri.serial.i2c.jna.I2cDev.i2c_func.I2C_FUNC_SMBUS_EMUL;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.TestI2cCLibNative;
import ceri.serial.i2c.jna.TestI2cCLibNative.Bytes;
import ceri.serial.i2c.jna.TestI2cCLibNative.Int;

public class I2cDeviceBehavior {
	private JnaLibrary.Ref<TestI2cCLibNative> ref = TestI2cCLibNative.ref();
	private CFileDescriptor fd;
	private I2cDevice i2c;

	@After
	public void after() {
		CloseableUtil.close(fd, ref);
		fd = null;
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		initI2c();
		assertFind(i2c, "%s.*fd=", I2cDevice.class.getSimpleName());
	}

	@Test
	public void shouldOpenDevice() throws IOException {
		var lib = initI2c();
		assertThrown(() -> I2cDevice.open(-1));
		lib.open.assertAuto(new OpenArgs("/dev/i2c-1", 2, 0)); // open fd
		lib.ioctlI2cInt.autoResponses(i2c_func.xcoder.encodeInt(I2C_FUNC_SMBUS_EMUL));
		assertCollection(i2c.functions(), I2C_FUNC_SMBUS_EMUL);
		lib.ioctlI2cInt.assertAuto(new Int(0x0705, 0)); // I2C_FUNCS, dummy 0
	}

	@Test
	public void shouldDetermineIfAddressExists() throws IOException {
		var lib = initI2c();
		assertTrue(i2c.exists(I2cAddress.of(0x3b)));
		assertThrown(() -> i2c.exists(I2cAddress.of(0x1ab))); // 10-bit not supported
		lib.ioctlSmBusInt.error.setFrom(LEX);
		assertFalse(i2c.exists(I2cAddress.of(0x3b)));
	}

	@Test
	public void shouldScanFor7BitAddresses() throws IOException {
		var lib = initI2c();
		Set<Integer> exists = Set.of(0x2a, 0x3b, 0x4c);
		Set<I2cAddress> existAddrs = toSet(exists.stream().map(I2cAddress::of));
		autoError(lib.ioctlI2cInt, 0, i -> i.request() == 0x704 || exists.contains(i.value()),
			"Address does not exist");
		assertCollection(i2c.scan7Bit(), existAddrs);
	}

	@Test
	public void shouldRetrieveDeviceId() throws IOException {
		var lib = initI2c();
		lib.ioctlI2cBytes.autoResponses(provider(0, 0xa2, 0x6d));
		assertEquals(i2c.deviceId(I2cAddress.of10Bit(0x123)), DeviceId.decode(0xa26d));
		lib.ioctlI2cBytes.assertAuto(
			List.of(new Bytes(0x7c, 0, provider(0xf2, 0x23), 2), new Bytes(0x7c, 1, null, 3)));
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
		lib.ioctlI2cBytes.assertAuto(List.of(new Bytes(0, 0, provider(0x06), 1)));
	}

	@Test
	public void shouldReadData() throws IOException {
		var lib = initI2c();
		byte[] receive = new byte[3];
		lib.ioctlI2cBytes.autoResponses(provider(4, 5, 6));
		i2c.readData(I2cAddress.of(0x1ab), bytes(1, 2, 3), receive);
		assertArray(receive, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(
			List.of(new Bytes(0x1ab, 0x10, provider(1, 2, 3), 3), new Bytes(0x1ab, 0x11, null, 3)));
	}

	@Test
	public void shouldWriteAndReadFromMemory() throws IOException {
		var lib = initI2c();
		var out = mem(1, 2, 3);
		var in = memSize(3);
		lib.ioctlI2cBytes.autoResponses(provider(4, 5, 6));
		i2c.writeRead(I2cAddress.of(0x1ab), out.m, in.m);
		JnaTestUtil.assertMemory(in.m, 0, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(
			List.of(new Bytes(0x1ab, 0x10, provider(1, 2, 3), 3), new Bytes(0x1ab, 0x11, null, 3)));
	}

	private TestI2cCLibNative initI2c() throws IOException {
		ref.init();
		fd = I2cDevice.open(1);
		i2c = I2cDevice.of(fd);
		return ref.get();
	}	
}
