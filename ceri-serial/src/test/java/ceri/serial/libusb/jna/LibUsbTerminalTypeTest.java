package ceri.serial.libusb.jna;

import org.junit.jupiter.api.Test;
import ceri.common.test.Testing;

public class LibUsbTerminalTypeTest {

	@Test
	public void test() {
		Testing.exerciseEnum(LibUsbTerminalType.class);
	}
}
