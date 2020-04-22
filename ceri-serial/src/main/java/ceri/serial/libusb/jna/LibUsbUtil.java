package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import java.nio.ByteBuffer;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;

public class LibUsbUtil {

	private LibUsbUtil() {}

	public static void require(ByteBuffer buffer, int remaining) throws LibUsbException {
		if (buffer == null || buffer.remaining() >= remaining) return;
		throw LibUsbException.of(LIBUSB_ERROR_INVALID_PARAM, "Buffer too small: %d / %d",
			buffer.remaining(), remaining);
	}

	public static void require(libusb_device dev) throws LibUsbException {
		require(dev, "Device");
	}

	public static void require(libusb_context ctx) throws LibUsbException {
		require(ctx, "Context");
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
