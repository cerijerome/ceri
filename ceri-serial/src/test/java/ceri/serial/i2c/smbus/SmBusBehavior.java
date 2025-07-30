package ceri.serial.i2c.smbus;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;

public class SmBusBehavior {

	@Test
	public void shouldProvideNoOpWrites() throws IOException {
		SmBus.NULL.writeQuick(true);
		SmBus.NULL.writeByte(0xab);
		SmBus.NULL.writeByteData(0x12, 0xab);
		SmBus.NULL.writeWordData(0x12, 0xabcd);
		SmBus.NULL.writeBlockData(0x12, ArrayUtil.bytes.of(0xab, 0xcd));
		SmBus.NULL.writeI2cBlockData(0x12, ArrayUtil.bytes.of(0xab, 0xcd));
	}

	@Test
	public void shouldProvideNoOpReads() throws IOException {
		assertEquals(SmBus.NULL.readByte(), 0);
		assertEquals(SmBus.NULL.readByteData(0x12), 0);
		assertEquals(SmBus.NULL.readWordData(0x12), 0);
		assertEquals(SmBus.NULL.processCall(0x12, 0xabcd), 0);
		assertArray(SmBus.NULL.readBlockData(0x12));
		assertArray(SmBus.NULL.blockProcessCall(0x12, 0xab, 0xcd));
		assertArray(SmBus.NULL.readI2cBlockData(0x12, 3), 0, 0, 0);
	}

}
