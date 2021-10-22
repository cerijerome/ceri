package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.convertAsList;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.UsbDevice.Devices;
import ceri.serial.libusb.UsbHotPlug.Callback;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsb.libusb_option;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Entry point for access to libusb functionality. Wraps libusb_context.
 */
public class Usb implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	private static final Map<Level, libusb_log_level> levelMap = levelMap();
	private final UsbEvents events;
	private libusb_context context;

	public static UsbLibVersion version() throws LibUsbException {
		return new UsbLibVersion(LibUsb.libusb_get_version());
	}

	public static void setLocale(Locale locale) throws LibUsbException {
		LibUsb.libusb_setlocale(locale.toString());
	}

	/**
	 * Creates an instance with a new context.
	 */
	public static Usb of() throws LibUsbException {
		return new Usb(LibUsb.libusb_init());
	}

	private Usb(libusb_context context) {
		this.context = context;
		events = new UsbEvents(this);
	}

	public UsbDeviceHandle open(LibUsbFinder finder) throws LibUsbException {
		var handle = finder.findAndOpen(context());
		return new UsbDeviceHandle(this, handle);
	}

	public Devices deviceList() throws LibUsbException {
		var list = LibUsb.libusb_get_device_list(context());
		var devices = convertAsList(d -> new UsbDevice(this, d), list.get());
		return new Devices(list, devices);
	}

	public void debug(Level level) throws LibUsbException {
		LibUsb.libusb_set_option(context(), libusb_option.LIBUSB_OPTION_LOG_LEVEL,
			levelMap.getOrDefault(level, libusb_log_level.LIBUSB_LOG_LEVEL_WARNING).value);
	}

	public void useUsbDk(boolean enabled) throws LibUsbException {
		LibUsb.libusb_set_option(context(), libusb_option.LIBUSB_OPTION_USE_USBDK, enabled ? 1 : 0);
	}

	public void weakAuthority(boolean enabled) throws LibUsbException {
		LibUsb.libusb_set_option(context(), libusb_option.LIBUSB_OPTION_WEAK_AUTHORITY,
			enabled ? 1 : 0);
	}

	public UsbEvents events() {
		return events;
	}

	/**
	 * Start configuring a hot plug event callback.
	 */
	public UsbHotPlug.Builder hotPlug(Callback callback) {
		return new UsbHotPlug.Builder(this, callback);
	}

	@Override
	public void close() {
		LogUtil.close(logger, () -> LibUsb.libusb_exit(context));
		context = null;
	}

	libusb_context context() {
		if (context != null) return context;
		throw new IllegalStateException("Context has been closed");
	}

	private static Map<Level, libusb_log_level> levelMap() {
		return Map.of( //
			Level.OFF, libusb_log_level.LIBUSB_LOG_LEVEL_NONE, //
			Level.ALL, libusb_log_level.LIBUSB_LOG_LEVEL_DEBUG, //
			Level.TRACE, libusb_log_level.LIBUSB_LOG_LEVEL_DEBUG, //
			Level.DEBUG, libusb_log_level.LIBUSB_LOG_LEVEL_DEBUG, //
			Level.INFO, libusb_log_level.LIBUSB_LOG_LEVEL_INFO, //
			Level.WARN, libusb_log_level.LIBUSB_LOG_LEVEL_WARNING, //
			Level.ERROR, libusb_log_level.LIBUSB_LOG_LEVEL_ERROR, //
			Level.FATAL, libusb_log_level.LIBUSB_LOG_LEVEL_ERROR);
	}
}
