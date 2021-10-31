package ceri.serial.javax;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;

public class SerialPortBehavior {

	@Test
	public void testIsBroken() {
		assertEquals(SerialPort.isBroken(null), false);
		assertEquals(SerialPort.isBroken(new NoSuchPortException("fail", null)), true);
		assertEquals(SerialPort.isBroken(new IOException("ioctl")), true);
		assertEquals(SerialPort.isBroken(new IOException("ok")), false);
	}

}
