package ceri.serial.spi.pulse;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Test;
import ceri.common.data.ByteUtil;
import ceri.common.test.CallSync;
import ceri.serial.jna.JnaUtil;

public class PulseBufferBehavior {

	@Test
	public void shouldProvidePulseBitCount() {
		var buffer = PulseCycles.Std._7_27.cycle.buffer(8);
		assertEquals(buffer.pulseBits(), 7);
		buffer.writePulseTo(null);
	}

	@Test
	public void shouldWriteToBuffer() {
		var buffer = PulseCycles.Std._7_27.cycle.buffer(3);
		buffer.copyFrom(0, new byte[0]);
		buffer.setBytes(0, 1, 2, 3);
		ByteBuffer bb = ByteBuffer.allocate(buffer.storageSize());
		buffer.writePulseTo(null);
		buffer.writePulseTo(bb);
		assertArray(JnaUtil.byteArray(bb), 0x83, 0x06, 0x0c, 0x83, 0x06, 0x0f, 0x83, 0x06, 0x0c,
			0x83, 0x07, 0x8c, 0x83, 0x06, 0x0c, 0x83, 0x07, 0x8f);
	}

}