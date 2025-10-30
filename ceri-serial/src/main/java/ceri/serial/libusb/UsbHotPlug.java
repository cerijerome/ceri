package ceri.serial.libusb;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collect.Sets;
import ceri.common.function.Functions;
import ceri.log.util.Logs;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Holds a registered callback, and deregisters on close. Keeps track of JNA callback reference to
 * avoid early removal by GC.
 * <p/>
 * WARNING: If a device is plugged in then removed, calling <code>device.open()</code> on the arrive
 * event can generate SIGABRT cause the application to abort.
 */
public class UsbHotPlug implements Functions.Closeable {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Usb usb;
	private final LibUsb.libusb_hotplug_callback_handle handle;
	private final LibUsb.libusb_hotplug_callback_fn jnaCallback;
	public final Collection<LibUsb.libusb_hotplug_event> events;
	public final Collection<LibUsb.libusb_hotplug_flag> flags;
	public final int vendor;
	public final int product;
	public final LibUsb.libusb_class_code deviceClass;

	/**
	 * Callback for hot plug events.
	 */
	public interface Callback {
		/**
		 * Invoked on matching event, return true if finished processing events.
		 */
		boolean event(UsbDevice device, LibUsb.libusb_hotplug_event event) throws IOException;
	}

	/**
	 * Builder for hot plug callback options.
	 */
	public static class Builder {
		private final Usb usb;
		private final Callback callback;
		private final Collection<LibUsb.libusb_hotplug_event> events = Sets.link();
		private final Collection<LibUsb.libusb_hotplug_flag> flags = Sets.link();
		int vendor = 0;
		int product = 0;
		LibUsb.libusb_class_code deviceClass = null;

		Builder(Usb usb, Callback callback) {
			this.usb = usb;
			this.callback = callback;
		}

		/**
		 * Register for arrive events.
		 */
		public Builder arrived() {
			events.add(LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_ARRIVED);
			return this;
		}

		/**
		 * Register for leave events.
		 */
		public Builder left() {
			events.add(LibUsb.libusb_hotplug_event.LIBUSB_HOTPLUG_EVENT_DEVICE_LEFT);
			return this;
		}

		/**
		 * Enable enumerate option.
		 */
		public Builder enumerate() {
			flags.add(LibUsb.libusb_hotplug_flag.LIBUSB_HOTPLUG_ENUMERATE);
			return this;
		}

		/**
		 * Set vendor id to match, 0 for any match.
		 */
		public Builder vendor(int vendor) {
			this.vendor = vendor;
			return this;
		}

		/**
		 * Set product id to match, 0 for any match.
		 */
		public Builder product(int product) {
			this.product = product;
			return this;
		}

		/**
		 * Set device class match, null for any match.
		 */
		public Builder deviceClass(LibUsb.libusb_class_code deviceClass) {
			this.deviceClass = deviceClass;
			return this;
		}

		/**
		 * Register the callback and return a closeable holder.
		 */
		public UsbHotPlug register() throws LibUsbException {
			var jnaCallback = jnaCallback(usb, callback);
			var handle = LibUsb.libusb_hotplug_register_callback(usb.context(), events(), flags(),
				valueOrAny(vendor), valueOrAny(product), valueOrAny(deviceClass), jnaCallback,
				null);
			return new UsbHotPlug(this, handle, jnaCallback);
		}

		private int events() {
			return LibUsb.libusb_hotplug_event.xcoder.encodeInt(events);
		}

		private int flags() {
			return LibUsb.libusb_hotplug_flag.xcoder.encodeInt(flags);
		}
	}

	public static boolean hasCapability() throws LibUsbException {
		return LibUsb.libusb_has_capability(LibUsb.libusb_capability.LIBUSB_CAP_HAS_HOTPLUG);
	}

	UsbHotPlug(Builder builder, LibUsb.libusb_hotplug_callback_handle handle,
		LibUsb.libusb_hotplug_callback_fn jnaCallback) {
		this.usb = builder.usb;
		this.handle = handle;
		this.jnaCallback = jnaCallback;
		this.events = List.copyOf(builder.events);
		this.flags = List.copyOf(builder.flags);
		this.vendor = builder.vendor;
		this.product = builder.product;
		this.deviceClass = builder.deviceClass;
	}

	public Usb usb() {
		return usb;
	}

	@Override
	public void close() {
		Logs.close(() -> LibUsb.libusb_hotplug_deregister_callback(usb.context(), handle));
		Reference.reachabilityFence(jnaCallback);
	}

	@SuppressWarnings("resource")
	private static LibUsb.libusb_hotplug_callback_fn jnaCallback(Usb usb, Callback callback) {
		return (_, dev, evt, _) -> {
			try {
				var device = new UsbDevice(usb, dev);
				var event = LibUsb.libusb_hotplug_event.xcoder.decode(evt);
				return callback.event(device, event) ? 1 : 0;
			} catch (IOException | RuntimeException e) {
				logger.catching(e);
				return 0; // keep receiving events
			}
		};
	}

	private static int valueOrAny(int value) {
		return value == 0 ? LibUsb.LIBUSB_HOTPLUG_MATCH_ANY : value;
	}

	private static int valueOrAny(LibUsb.libusb_class_code dev) {
		return dev == null ? LibUsb.LIBUSB_HOTPLUG_MATCH_ANY : dev.value;
	}
}
