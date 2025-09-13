package ceri.serial.libusb.jna;

import java.nio.ByteBuffer;
import ceri.common.math.Maths;

public class LibUsbUtil {
	private static final String SUCCESS_MESSAGE = "success";
	private static final int ERROR_MSG_INDEX = "LIBUSB_ERROR_".length();

	private LibUsbUtil() {}

	public static String errorMessage(LibUsb.libusb_error error) {
		if (error == null) return null;
		if (error == LibUsb.libusb_error.LIBUSB_SUCCESS) return SUCCESS_MESSAGE;
		return error.name().substring(ERROR_MSG_INDEX).toLowerCase().replace('_', ' ');
	}

	/**
	 * Get error code from transfer status.
	 */
	public static LibUsb.libusb_error statusError(LibUsb.libusb_transfer_status status) {
		if (status == null) return LibUsb.libusb_error.LIBUSB_ERROR_IO;
		return switch (status) {
			case LIBUSB_TRANSFER_COMPLETED -> LibUsb.libusb_error.LIBUSB_SUCCESS;
			case LIBUSB_TRANSFER_TIMED_OUT -> LibUsb.libusb_error.LIBUSB_ERROR_TIMEOUT;
			case LIBUSB_TRANSFER_CANCELLED -> LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
			case LIBUSB_TRANSFER_STALL -> LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
			case LIBUSB_TRANSFER_NO_DEVICE -> LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
			case LIBUSB_TRANSFER_OVERFLOW -> LibUsb.libusb_error.LIBUSB_ERROR_OVERFLOW;
			default -> LibUsb.libusb_error.LIBUSB_ERROR_IO;
		};
	}

	public static int requestTypeValue(LibUsb.libusb_request_recipient recipient,
		LibUsb.libusb_request_type type, LibUsb.libusb_endpoint_direction endpoint_direction) {
		return Maths.ubyte(recipient.value | type.value | endpoint_direction.value);
	}

	public static int endpointAddress(int value, LibUsb.libusb_endpoint_direction direction) {
		return Maths.ubyte(value | direction.value);
	}

	public static void require(ByteBuffer buffer, int remaining) throws LibUsbException {
		if (remaining == 0) return;
		require(buffer, "Buffer");
		if (buffer.remaining() < remaining)
			throw LibUsbException.of(LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM,
				"Buffer too small: %d / %d", buffer.remaining(), remaining);
	}

	public static void require(LibUsb.libusb_device dev) throws LibUsbException {
		require(dev, "Device");
	}

	public static void require(LibUsb.libusb_device_handle dev) throws LibUsbException {
		require(dev, "Device handle");
	}

	public static void require(LibUsb.libusb_transfer transfer) throws LibUsbException {
		require(transfer, "Transfer");
	}

	public static void require(Object obj, String name) throws LibUsbException {
		if (obj != null) return;
		throw LibUsbException.of(LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM,
			name + " unavailable");
	}
}
