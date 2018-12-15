package ceri.serial.libusb;

import java.io.Closeable;
import java.util.Locale;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsb.libusb_version;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbContext implements Closeable {
	private final libusb_context ctx;

	public static libusb_version version() throws LibUsbException {
		return LibUsb.libusb_get_version();
	}

	public static boolean hasCapability(libusb_capability capability) {
		return LibUsb.libusb_has_capability(capability);
	}

	public static void setLocale(Locale locale) throws LibUsbException {
		LibUsb.libusb_setlocale(locale.toString());
	}

	public static String errorName(libusb_error error) {
		return LibUsb.libusb_error_name(error);
	}

	public static String errorString(libusb_error error) {
		return LibUsb.libusb_strerror(error);
	}

	public static LibUsbContext init() throws LibUsbException {
		return new LibUsbContext(LibUsb.libusb_init());
	}

	public static LibUsbContext initDefault() throws LibUsbException {
		LibUsb.libusb_init_default();
		return new LibUsbContext(null);
	}

	private LibUsbContext(libusb_context ctx) {
		this.ctx = ctx;
	}

	public void setDebug(libusb_log_level level) {
		LibUsb.libusb_set_debug(ctx, level);
	}

	public LibUsbDeviceHandle openDeviceWithVidPid(int vendorId, int productId)
		throws LibUsbException {
		libusb_device_handle handle =
			LibUsb.libusb_open_device_with_vid_pid(ctx, (short) vendorId, (short) productId);
		if (handle == null) return null;
		return new LibUsbDeviceHandle(this, handle);
	}

	public LibUsbDeviceList deviceList() throws LibUsbException {
		libusb_device.ByReference list = LibUsb.libusb_get_device_list(ctx);
		return new LibUsbDeviceList(this, list);
	}

	@Override
	public void close() {
		LibUsb.libusb_exit(ctx);
	}

}
