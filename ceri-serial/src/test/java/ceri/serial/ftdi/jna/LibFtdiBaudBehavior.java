package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.AfterClass;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiBaudBehavior {

	@Test
	public void shouldCalculateBaud() throws LibUsbException {
		assertThrown(() -> LibFtdiBaud.of(TYPE_2232C, 0, 0));
		assertBaud(LibFtdiBaud.of(TYPE_2232C, 1, 19200), 19200, 32924, 0);
		assertBaud(LibFtdiBaud.of(TYPE_4232H, 1, 19200), 19200, 625, 0x201);
		assertBaud(LibFtdiBaud.of(TYPE_2232H, 1, 450), 450, 23050, 0x101);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 0, 1000000), 1000000, 3, 0);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 2, 250000), 250000, 12, 0);
	}

	private static void assertBaud(LibFtdiBaud baud, int actual, int value, int index) {
		assertEquals(baud.actualRate(), actual, "actualRate");
		assertEquals(baud.value(), value, "value");
		assertEquals(baud.index(), index, "index");
	}
}
