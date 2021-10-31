package ceri.serial.libusb.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.serial.jna.test.JnaTestUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;

public class LibUsbUtilTest {

	@Test
	public void testStatusError() {
		assertEquals(LibUsbUtil.statusError(null), libusb_error.LIBUSB_ERROR_IO);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED),
			libusb_error.LIBUSB_SUCCESS);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_ERROR),
			libusb_error.LIBUSB_ERROR_IO);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_TIMED_OUT),
			libusb_error.LIBUSB_ERROR_TIMEOUT);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED),
			libusb_error.LIBUSB_ERROR_INTERRUPTED);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_STALL),
			libusb_error.LIBUSB_ERROR_BUSY);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_NO_DEVICE),
			libusb_error.LIBUSB_ERROR_NO_DEVICE);
		assertEquals(LibUsbUtil.statusError(libusb_transfer_status.LIBUSB_TRANSFER_OVERFLOW),
			libusb_error.LIBUSB_ERROR_OVERFLOW);
	}

	@Test
	public void testRequireByteBuffer() throws LibUsbException {
		LibUsbUtil.require(null, 0);
		assertThrown(() -> LibUsbUtil.require(null, 4));
		assertThrown(() -> LibUsbUtil.require(JnaTestUtil.buffer(1, 2, 3), 4));
	}

}
