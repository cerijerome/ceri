package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.convertAsList;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_version;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_init_default;
import static ceri.serial.libusb.jna.LibUsb.libusb_setlocale;
import static ceri.serial.libusb.jna.LibUsb.libusb_log_level.LIBUSB_LOG_LEVEL_DEBUG;
import static ceri.serial.libusb.jna.LibUsb.libusb_log_level.LIBUSB_LOG_LEVEL_ERROR;
import static ceri.serial.libusb.jna.LibUsb.libusb_log_level.LIBUSB_LOG_LEVEL_INFO;
import static ceri.serial.libusb.jna.LibUsb.libusb_log_level.LIBUSB_LOG_LEVEL_NONE;
import static ceri.serial.libusb.jna.LibUsb.libusb_log_level.LIBUSB_LOG_LEVEL_WARNING;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_LOG_LEVEL;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_USE_USBDK;
import static ceri.serial.libusb.jna.LibUsb.libusb_option.LIBUSB_OPTION_WEAK_AUTHORITY;
import java.io.Closeable;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbDevice.Devices;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Entry point for access to libusb functionality. Wraps libusb_context.
 */
public class Usb implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final Map<Level, libusb_log_level> levelMap = levelMap();
	private final UsbEvents events;
	private final UsbHotplug hotplug;
	private libusb_context context;

	public static UsbLibVersion version() {
		return new UsbLibVersion(libusb_get_version());
	}

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

	public void debug(Level level) throws LibUsbException {
		LibUsb.libusb_set_option(context(), LIBUSB_OPTION_LOG_LEVEL,
			levelMap.getOrDefault(level, LIBUSB_LOG_LEVEL_WARNING));
	}

	public void useUsbDk(boolean enabled) throws LibUsbException {
		LibUsb.libusb_set_option(context(), LIBUSB_OPTION_USE_USBDK, enabled ? 1 : 0);
	}

	public void weakAuthority(boolean enabled) throws LibUsbException {
		LibUsb.libusb_set_option(context(), LIBUSB_OPTION_WEAK_AUTHORITY, enabled ? 1 : 0);
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

	private static Map<Level, libusb_log_level> levelMap() {
		return Map.of( //
			Level.OFF, LIBUSB_LOG_LEVEL_NONE, //
			Level.ALL, LIBUSB_LOG_LEVEL_DEBUG, //
			Level.TRACE, LIBUSB_LOG_LEVEL_DEBUG, //
			Level.DEBUG, LIBUSB_LOG_LEVEL_DEBUG, //
			Level.INFO, LIBUSB_LOG_LEVEL_INFO, //
			Level.WARN, LIBUSB_LOG_LEVEL_WARNING, //
			Level.ERROR, LIBUSB_LOG_LEVEL_ERROR, //
			Level.FATAL, LIBUSB_LOG_LEVEL_ERROR);
	}
}
