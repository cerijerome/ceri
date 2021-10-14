package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import org.junit.Test;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiUtilTest {

	@Test
	public void testGuessChipType() {
		assertEquals(LibFtdiUtil.guessChipType(0x200, 1), ftdi_chip_type.TYPE_AM);
		assertEquals(LibFtdiUtil.guessChipType(0x200, 0), ftdi_chip_type.TYPE_BM);
		assertEquals(LibFtdiUtil.guessChipType(0x300, 0), ftdi_chip_type.TYPE_BM);
		assertEquals(LibFtdiUtil.guessChipType(0x400, 0), ftdi_chip_type.TYPE_BM);
		assertEquals(LibFtdiUtil.guessChipType(0x500, 0), ftdi_chip_type.TYPE_2232C);
		assertEquals(LibFtdiUtil.guessChipType(0x600, 0), ftdi_chip_type.TYPE_R);
		assertEquals(LibFtdiUtil.guessChipType(0x700, 0), ftdi_chip_type.TYPE_2232H);
		assertEquals(LibFtdiUtil.guessChipType(0x800, 0), ftdi_chip_type.TYPE_4232H);
		assertEquals(LibFtdiUtil.guessChipType(0x900, 0), ftdi_chip_type.TYPE_232H);
		assertEquals(LibFtdiUtil.guessChipType(0x1000, 0), ftdi_chip_type.TYPE_230X);
	}

	@Test
	public void testIsError() {
		assertEquals(LibFtdiUtil.isError(null, LIBUSB_ERROR_IO), false);
		assertEquals(LibFtdiUtil.isError(new RuntimeException(), LIBUSB_ERROR_IO), false);
		assertEquals(
			LibFtdiUtil.isError(LibUsbException.full("test", LIBUSB_ERROR_IO), LIBUSB_ERROR_IO),
			true);
		assertEquals(
			LibFtdiUtil.isError(LibUsbException.full("test", LIBUSB_ERROR_BUSY), LIBUSB_ERROR_IO),
			false);
	}

}
