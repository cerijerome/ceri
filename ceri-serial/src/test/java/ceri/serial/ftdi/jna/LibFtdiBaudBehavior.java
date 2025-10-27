package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232C;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_2232H;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_230X;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_AM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_BM;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type.TYPE_R;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiBaudBehavior {

	@Test
	public void shouldProvideStringRepresentation() throws LibUsbException {
		Assert.find(LibFtdiBaud.of(TYPE_2232H, 1, 720), "TYPE_2232H.*0x15046.*720");
	}

	@Test
	public void shouldCalculateBaud() throws LibUsbException {
		Assert.thrown(() -> LibFtdiBaud.of(TYPE_2232C, 0, 0));
		assertBaud(LibFtdiBaud.of(TYPE_2232H, 1, 720), 720, 20550, 0x101);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 0, 9600), 9600, 16696, 0);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 0, 11200), 11194, 268, 0);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 0, 1501000), 1500000, 2, 0);
		assertBaud(LibFtdiBaud.of(TYPE_AM, 0, 3000000), 3000000, 0, 0);
		assertBaud(LibFtdiBaud.of(TYPE_BM, 0, 3000000), 3000000, 0, 0);
		assertBaud(LibFtdiBaud.of(TYPE_BM, 0, 2000000), 2000000, 1, 0);
		assertBaud(LibFtdiBaud.of(TYPE_BM, 0, 1500000), 1500000, 2, 0);
		assertBaud(LibFtdiBaud.of(TYPE_230X, 0, 1000000), 1000000, 3, 0);
		assertBaud(LibFtdiBaud.of(TYPE_2232C, 0, 180), 183, 0xffff, 1);
		assertBaud(LibFtdiBaud.of(TYPE_R, 0, 70000), 69971, 49194, 1);
		Assert.thrown(() -> LibFtdiBaud.of(TYPE_230X, 0, 6400000));
		Assert.thrown(() -> LibFtdiBaud.of(TYPE_AM, 0, 631000));
		Assert.thrown(() -> LibFtdiBaud.of(TYPE_AM, 0, 634000));
	}

	private static void assertBaud(LibFtdiBaud baud, int actual, int value, int index) {
		Assert.equal(baud.actualRate(), actual, "actualRate");
		Assert.equal(baud.value(), value, "value");
		Assert.equal(baud.index(), index, "index");
	}
}
