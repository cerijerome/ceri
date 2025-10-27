package ceri.serial.spi.pulse;

import java.nio.ByteBuffer;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.util.JnaUtil;

public class PulseBufferBehavior {

	@Test
	public void shouldProvidePulseBitCount() {
		var buffer = PulseCycle.Std._7_27.cycle.buffer(8);
		Assert.equal(buffer.pulseBits(), 7);
		buffer.writePulseTo(null);
	}

	@Test
	public void shouldWriteToBuffer() {
		var buffer = PulseCycle.Std._7_27.cycle.buffer(3);
		buffer.copyFrom(0, new byte[0]);
		buffer.setBytes(0, 1, 2, 3);
		ByteBuffer bb = ByteBuffer.allocate(buffer.storageSize());
		buffer.writePulseTo(null);
		buffer.writePulseTo(bb);
		Assert.array(JnaUtil.bytes(bb), //
			0x83, 0x06, 0x0c, 0x83, 0x06, 0x0f, 0x83, 0x06, 0x0c, 0x83, 0x07, 0x8c, 0x83, 0x06,
			0x0c, 0x83, 0x07, 0x8f);
		Assert.array(buffer.buffer(), //
			0x83, 0x06, 0x0c, 0x83, 0x06, 0x0f, 0x83, 0x06, 0x0c, 0x83, 0x07, 0x8c, 0x83, 0x06,
			0x0c, 0x83, 0x07, 0x8f);
	}

}
