package ceri.serial.libusb.jna;

import static ceri.common.test.Testing.exerciseEnum;
import org.junit.Test;

public class LibUsbTerminalTypeTest {

	@Test
	public void test() {
		exerciseEnum(LibUsbTerminalType.class);
	}

}
