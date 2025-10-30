package ceri.serial.libusb.jna;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.test.JnaTesting;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;

public class LibUsbUtilTest {

	@Test
	public void testErrorMessage() {
		Assert.isNull(LibUsbUtil.errorMessage(null));
		Assert.equal(LibUsbUtil.errorMessage(libusb_error.LIBUSB_SUCCESS), "success");
		Assert.equal(LibUsbUtil.errorMessage(libusb_error.LIBUSB_ERROR_INVALID_PARAM),
			"invalid param");
	}

	@Test
	public void testStatusError() {
		Assert.equal(LibUsbUtil.statusError(null), libusb_error.LIBUSB_ERROR_IO);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED),
			libusb_error.LIBUSB_SUCCESS);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_ERROR),
			libusb_error.LIBUSB_ERROR_IO);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_TIMED_OUT),
			libusb_error.LIBUSB_ERROR_TIMEOUT);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED),
			libusb_error.LIBUSB_ERROR_INTERRUPTED);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_STALL),
			libusb_error.LIBUSB_ERROR_BUSY);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_NO_DEVICE),
			libusb_error.LIBUSB_ERROR_NO_DEVICE);
		Assert.equal(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_OVERFLOW),
			libusb_error.LIBUSB_ERROR_OVERFLOW);
	}

	@Test
	public void testRequireByteBuffer() throws LibUsbException {
		LibUsbUtil.require(null, 0);
		Assert.thrown(() -> LibUsbUtil.require(null, 4));
		Assert.thrown(() -> LibUsbUtil.require(JnaTesting.buffer(1, 2, 3), 4));
	}
}
