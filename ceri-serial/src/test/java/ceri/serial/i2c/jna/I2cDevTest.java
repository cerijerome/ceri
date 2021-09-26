package ceri.serial.i2c.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.serial.i2c.jna.TestI2cCLibNative.smBusBlock;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.serial.clib.test.TestCLibNative;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_rdwr_ioctl_data;
import ceri.serial.i2c.jna.I2cDev.i2c_smbus_data;

public class I2cDevTest {
	private TestI2cCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;

	@Before
	public void before() {
		lib = TestI2cCLibNative.of();
		enc = TestCLibNative.register(lib);
	}

	@After
	public void after() {
		enc.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(I2cDev.class);
	}

	@Test
	public void testI2cMsg() {
		assertArray(i2c_msg.array(0));
		var msg = new i2c_msg(null);
		msg.populate(0x3a, 0, 0, null);
		assertByte(msg.addrByte(), 0x74);
		msg.populate(0x3a, 1, 0, null);
		assertByte(msg.addrByte(), 0x75);
	}

	@Test
	public void testRdwrIoctlData() {
		var data = new i2c_rdwr_ioctl_data();
		assertArray(data.msgs()); // 0 count
	}

	@Test
	public void testI2cSmBusData() {
		var smBus = new i2c_smbus_data();
		smBus.setBlock(bytes(1, 2, 3, 4, 5));
		assertArray(smBus.block, smBusBlock(5, 1, 2, 3, 4, 5).copy(0));
	}

	@Test
	public void testI2cTenBit() throws IOException {
		int fd = I2cDev.i2c_open(1, 0);
		I2cDev.i2c_tenbit(fd, false);
		I2cDev.i2c_tenbit(fd, true);
		lib.ioctlI2cInt.assertValues(List.of(0x704, 0), List.of(0x704, 1));
	}

	@Test
	public void testWriteBlockData() throws IOException {
		int fd = I2cDev.i2c_open(1, 0);
		I2cDev.i2c_smbus_write_block_data(fd, 0x12, bytes(1, 2, 3));
		I2cDev.i2c_smbus_write_i2c_block_data(fd, 0x12, bytes(1, 2, 3));
		lib.ioctlSmBusBytes.assertValues(List.of(0, 0x12, 5, 0, 0, smBusBlock(3, 1, 2, 3)),
			List.of(0, 0x12, 8, 0, 0, smBusBlock(3, 1, 2, 3)));
	}

}
