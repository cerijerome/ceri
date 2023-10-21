package ceri.serial.libusb.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_BUSY;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_OVERFLOW;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_TIMEOUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import java.nio.ByteBuffer;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_request_recipient;
import ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;

public class LibUsbUtil {
	private static final String SUCCESS_MESSAGE = "success";
	private static final int ERROR_MSG_INDEX = "LIBUSB_ERROR_".length();
	
	private LibUsbUtil() {}

	public static String errorMessage(libusb_error error) {
		if (error == null) return null;
		if (error == LIBUSB_SUCCESS) return SUCCESS_MESSAGE;
		return error.name().substring(ERROR_MSG_INDEX).toLowerCase().replace('_', ' ');
	}
	
	/**
	 * Get error code from transfer status.
	 */
	public static libusb_error statusError(libusb_transfer_status status) {
		if (status == null) return LIBUSB_ERROR_IO;
		return switch (status) {
			case LIBUSB_TRANSFER_COMPLETED -> LIBUSB_SUCCESS;
			case LIBUSB_TRANSFER_TIMED_OUT -> LIBUSB_ERROR_TIMEOUT;
			case LIBUSB_TRANSFER_CANCELLED -> LIBUSB_ERROR_INTERRUPTED;
			case LIBUSB_TRANSFER_STALL -> LIBUSB_ERROR_BUSY;
			case LIBUSB_TRANSFER_NO_DEVICE -> LIBUSB_ERROR_NO_DEVICE;
			case LIBUSB_TRANSFER_OVERFLOW -> LIBUSB_ERROR_OVERFLOW;
			default -> LIBUSB_ERROR_IO;
		};
	}

	public static int requestTypeValue(libusb_request_recipient recipient, libusb_request_type type,
		libusb_endpoint_direction endpoint_direction) {
		return ubyte(recipient.value | type.value | endpoint_direction.value);
	}

	public static int endpointAddress(int value, LibUsb.libusb_endpoint_direction direction) {
		return ubyte(value | direction.value);
	}

	public static void require(ByteBuffer buffer, int remaining) throws LibUsbException {
		if (remaining == 0) return;
		require(buffer, "Buffer");
		if (buffer.remaining() < remaining) throw LibUsbException.of(LIBUSB_ERROR_INVALID_PARAM,
			"Buffer too small: %d / %d", buffer.remaining(), remaining);
	}

	public static void require(libusb_device dev) throws LibUsbException {
		require(dev, "Device");
	}

	public static void require(libusb_device_handle dev) throws LibUsbException {
		require(dev, "Device handle");
	}

	public static void require(libusb_transfer transfer) throws LibUsbException {
		require(transfer, "Transfer");
	}

	public static void require(Object obj, String name) throws LibUsbException {
		if (obj != null) return;
		throw LibUsbException.of(LIBUSB_ERROR_INVALID_PARAM, name + " unavailable");
	}

}
