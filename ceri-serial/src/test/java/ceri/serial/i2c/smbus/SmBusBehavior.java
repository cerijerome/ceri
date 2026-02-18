package ceri.serial.i2c.smbus;

import java.io.IOException;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.test.Assert;

public class SmBusBehavior {

	@Test
	public void shouldProvideNoOpWrites() throws IOException {
		SmBus.NULL.writeQuick(true);
		SmBus.NULL.writeByte(0xab);
		SmBus.NULL.writeByteData(0x12, 0xab);
		SmBus.NULL.writeWordData(0x12, 0xabcd);
		SmBus.NULL.writeBlockData(0x12, Array.BYTE.of(0xab, 0xcd));
		SmBus.NULL.writeI2cBlockData(0x12, Array.BYTE.of(0xab, 0xcd));
	}

	@Test
	public void shouldProvideNoOpReads() throws IOException {
		Assert.equal(SmBus.NULL.readByte(), 0);
		Assert.equal(SmBus.NULL.readByteData(0x12), 0);
		Assert.equal(SmBus.NULL.readWordData(0x12), 0);
		Assert.equal(SmBus.NULL.processCall(0x12, 0xabcd), 0);
		Assert.array(SmBus.NULL.readBlockData(0x12));
		Assert.array(SmBus.NULL.blockProcessCall(0x12, 0xab, 0xcd));
		Assert.array(SmBus.NULL.readI2cBlockData(0x12, 3), 0, 0, 0);
	}

}
