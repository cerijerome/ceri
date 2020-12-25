package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.convertAsList;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_init_default;
import static ceri.serial.libusb.jna.LibUsb.libusb_setlocale;
import java.io.Closeable;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbDevice.Devices;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Entry point for access to libusb functionality. Wraps libusb_context.
 */
public class Usb implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final UsbEvents events;
	private final UsbHotplug hotplug;
	private libusb_context context;

	public static void setLocale(Locale locale) throws LibUsbException {
		libusb_setlocale(locale.toString());
	}

	/**
	 * Creates an instance with a new context.
	 */
	public static Usb of() throws LibUsbException {
		return new Usb(libusb_init());
	}

	/**
	 * Creates an instance with the default context.
	 */
	public static Usb ofDefault() throws LibUsbException {
		return new Usb(libusb_init_default());
	}

	private Usb(libusb_context context) {
		this.context = context;
		events = new UsbEvents(this);
		hotplug = new UsbHotplug(this);
	}

	public UsbDeviceHandle open(LibUsbFinder finder) throws LibUsbException {
		var handle = finder.findAndOpen(context());
		return new UsbDeviceHandle(this, handle);
	}

	public Devices deviceList() throws LibUsbException {
		var list = libusb_get_device_list(context());
		var devices = convertAsList(d -> new UsbDevice(this, d), list.array());
		return new Devices(list, devices);
	}

	public UsbEvents events() {
		return events;
	}

	public UsbHotplug hotplug() {
		return hotplug;
	}

	@Override
	public void close() {
		LogUtil.close(logger, hotplug, events, () -> libusb_exit(context));
		context = null;
	}

	libusb_context context() {
		if (context != null) return context;
		throw new IllegalStateException("Context has been closed");
	}

}
