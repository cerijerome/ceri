package ceri.serial.libusb.jna;

import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;

public class LibUsbTerminalTypeTest {

	@Test
	public void test() {
		exerciseEnum(LibUsbTerminalType.class);
	}

}
