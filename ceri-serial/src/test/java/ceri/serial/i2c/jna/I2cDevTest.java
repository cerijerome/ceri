package ceri.serial.i2c.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.serial.i2c.jna.TestI2cCLibNative.smBusBlock;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.util.JnaLibrary;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;
import ceri.serial.i2c.jna.I2cDev.i2c_rdwr_ioctl_data;
import ceri.serial.i2c.jna.I2cDev.i2c_smbus_data;
import ceri.serial.i2c.jna.TestI2cCLibNative.Int;
import ceri.serial.i2c.jna.TestI2cCLibNative.Rw;

public class I2cDevTest {
	private JnaLibrary.Ref<TestI2cCLibNative> ref = TestI2cCLibNative.i2cRef();

	@After
	public void after() {
		CloseableUtil.close(ref);
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
		smBus.setBlock(ArrayUtil.bytes.of(1, 2, 3, 4, 5));
		assertArray(smBus.block, smBusBlock(5, 1, 2, 3, 4, 5).copy(0));
	}

	@Test
	public void testI2cTenBit() throws IOException {
		var lib = ref.init();
		int fd = I2cDev.i2c_open(1, 0);
		I2cDev.i2c_tenbit(fd, false);
		I2cDev.i2c_tenbit(fd, true);
		lib.ioctlI2cInt.assertValues(new Int(0x704, 0), new Int(0x704, 1));
	}

	@Test
	public void testWriteBlockData() throws IOException {
		var lib = ref.init();
		int fd = I2cDev.i2c_open(1, 0);
		I2cDev.i2c_smbus_write_block_data(fd, 0x12, ArrayUtil.bytes.of(1, 2, 3));
		I2cDev.i2c_smbus_write_i2c_block_data(fd, 0x12, ArrayUtil.bytes.of(1, 2, 3));
		lib.ioctlSmBusBytes.assertValues(new Rw(0, 0x12, 5, 0, 0, smBusBlock(3, 1, 2, 3)),
			new Rw(0, 0x12, 8, 0, 0, smBusBlock(3, 1, 2, 3)));
	}
}
