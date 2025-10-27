package ceri.serial.ftdi.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiUtilTest {

	@Test
	public void testGuessChipType() {
		Assert.equal(LibFtdiUtil.guessChipType(0x200, 1), ftdi_chip_type.TYPE_AM);
		Assert.equal(LibFtdiUtil.guessChipType(0x200, 0), ftdi_chip_type.TYPE_BM);
		Assert.equal(LibFtdiUtil.guessChipType(0x300, 0), ftdi_chip_type.TYPE_BM);
		Assert.equal(LibFtdiUtil.guessChipType(0x400, 0), ftdi_chip_type.TYPE_BM);
		Assert.equal(LibFtdiUtil.guessChipType(0x500, 0), ftdi_chip_type.TYPE_2232C);
		Assert.equal(LibFtdiUtil.guessChipType(0x600, 0), ftdi_chip_type.TYPE_R);
		Assert.equal(LibFtdiUtil.guessChipType(0x700, 0), ftdi_chip_type.TYPE_2232H);
		Assert.equal(LibFtdiUtil.guessChipType(0x800, 0), ftdi_chip_type.TYPE_4232H);
		Assert.equal(LibFtdiUtil.guessChipType(0x900, 0), ftdi_chip_type.TYPE_232H);
		Assert.equal(LibFtdiUtil.guessChipType(0x1000, 0), ftdi_chip_type.TYPE_230X);
	}

	@Test
	public void testIsError() {
		Assert.equal(LibFtdiUtil.isError(null, LIBUSB_ERROR_IO), false);
		Assert.equal(LibFtdiUtil.isError(new RuntimeException(), LIBUSB_ERROR_IO), false);
		Assert.equal(
			LibFtdiUtil.isError(LibUsbException.full(LIBUSB_ERROR_IO, "test"), LIBUSB_ERROR_IO),
			true);
		Assert.equal(
			LibFtdiUtil.isError(LibUsbException.full(LIBUSB_ERROR_BUSY, "test"), LIBUSB_ERROR_IO),
			false);
	}

}
