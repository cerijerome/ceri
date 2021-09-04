package ceri.serial.i2c;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.StreamUtil.toSet;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.clib.jna.TestCLibUtil.autoError;
import static ceri.serial.i2c.jna.I2cDev.i2c_func.I2C_FUNC_SMBUS_EMUL;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import ceri.common.util.Enclosed;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.TestI2cCLibNative;
import ceri.serial.jna.test.JnaTestUtil;

public class I2cDeviceBehavior {
	private TestI2cCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private CFileDescriptor fd;
	private I2cDevice i2c;

	@Before
	public void before() throws IOException {
		lib = TestI2cCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = I2cDevice.open(1);
		i2c = I2cDevice.of(fd);
	}

	@After
	public void after() throws IOException {
		fd.close();
		enc.close();
	}

	@Test
	public void shouldOpenDevice() throws IOException {
		assertThrown(() -> I2cDevice.open(-1));
		lib.open.assertAuto(List.of("/dev/i2c-1", 2, 0)); // open fd
		lib.ioctlI2cInt.autoResponses(i2c_func.xcoder.encode(I2C_FUNC_SMBUS_EMUL));
		assertCollection(i2c.functions(), I2C_FUNC_SMBUS_EMUL);
		lib.ioctlI2cInt.assertAuto(List.of(0x0705, 0)); // I2C_FUNCS, dummy 0
	}

	@Test
	public void shouldDetermineIfAddressExists() {
		assertTrue(i2c.exists(I2cAddress.of(0x3b)));
		assertThrown(() -> i2c.exists(I2cAddress.of(0x1ab))); // 10-bit not supported
		lib.ioctlSmBusInt.error.setFrom(s -> new LastErrorException(s));
		assertFalse(i2c.exists(I2cAddress.of(0x3b)));
	}

	@Test
	public void shouldScanFor7BitAddresses() {
		Set<Integer> exists = Set.of(0x2a, 0x3b, 0x4c);
		Set<I2cAddress> existAddrs = toSet(exists.stream().map(I2cAddress::of));
		autoError(lib.ioctlI2cInt, 0,
			list -> (int) list.get(0) == 0x704 || exists.contains(list.get(1)),
			"Address does not exist");
		assertCollection(i2c.scan7Bit(), existAddrs);
	}

	@Test
	public void shouldRetrieveDeviceId() throws IOException {
		lib.ioctlI2cBytes.autoResponses(provider(0, 0xa2, 0x6d));
		assertEquals(i2c.deviceId(I2cAddress.of10Bit(0x123)), DeviceId.decode(0xa26d));
		lib.ioctlI2cBytes.assertAuto(List.of(0x7c, 0, provider(0xf2, 0x23), 0x7c, 1, 3));
	}

	@Test
	public void shouldConfigureDevice() throws IOException {
		i2c.retries(5);
		i2c.timeout(777);
		i2c.smBusPec(true);
		lib.ioctlI2cInt.assertValues(List.of(0x0701, 5), // I2C_RETRIES, 5
			List.of(0x0702, 77), // I2C_TIMEOUT, 777/10
			List.of(0x0708, 1)); // I2C_PEC, true
	}

	@Test
	public void shouldSendSoftwareReset() throws IOException {
		i2c.softwareReset();
		lib.ioctlI2cBytes.assertAuto(List.of(0, 0, provider(0x06)));
	}

	@Test
	public void shouldReadData() throws IOException {
		byte[] receive = new byte[3];
		lib.ioctlI2cBytes.autoResponses(provider(4, 5, 6));
		i2c.readData(I2cAddress.of(0x1ab), bytes(1, 2, 3), receive);
		assertArray(receive, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(List.of(0x1ab, 0x10, provider(1, 2, 3), 0x1ab, 0x11, 3));
	}

	@Test
	public void shouldWriteAndReadFromMemory() throws IOException {
		Memory out = CUtil.mallocBytes(1, 2, 3);
		Memory in = new Memory(3);
		lib.ioctlI2cBytes.autoResponses(provider(4, 5, 6));
		i2c.writeRead(I2cAddress.of(0x1ab), out, in);
		JnaTestUtil.assertMemory(in, 0, 4, 5, 6);
		lib.ioctlI2cBytes.assertAuto(List.of(0x1ab, 0x10, provider(1, 2, 3), 0x1ab, 0x11, 3));
	}

}
