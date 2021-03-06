package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.LIBUSB_HOTPLUG_MATCH_ANY;
import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_deregister_callback;
import static ceri.serial.libusb.jna.LibUsb.libusb_hotplug_register_callback;
import static ceri.serial.libusb.jna.LibUsb.libusb_capability.LIBUSB_CAP_HAS_HOTPLUG;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_event;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_flag;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Wraps hotplug methods, keeping track of callback references to avoid early removal by GC.
 */
public class UsbHotplug implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Usb usb;
	// Temporarily stores callbacks to make sure they are not removed by GC
	// Assigns a generated id, and tracks id/handle per callback
	private final Map<Integer, CallbackContext> callbackHandles = new ConcurrentHashMap<>();
	private final Map<Integer, CallbackContext> callbackIds = new ConcurrentHashMap<>();
	private final AtomicInteger callbackId = new AtomicInteger();

	/**
	 * Return true if finished processing events.
	 */
	public interface Callback<T> {
		boolean event(Usb context, UsbDevice device, libusb_hotplug_event event, T userData)
			throws IOException;
	}

	/**
	 * Keeps track of callback handles and assigned ids.
	 */
	static class CallbackContext {
		final Integer handle;
		final Integer id;
		final libusb_hotplug_callback_fn callback;

		CallbackContext(int handle, int id, libusb_hotplug_callback_fn callback) {
			this.handle = handle;
			this.id = id;
			this.callback = callback;
		}
	}

	public static class CallbackHandle {
		final libusb_hotplug_callback_handle handle;

		CallbackHandle(libusb_hotplug_callback_handle handle) {
			this.handle = handle;
		}
	}

	/**
	 * Encapsulation of callback registration options.
	 */
	public static class Registration<T> {
		private final UsbHotplug hotPlug;
		final Callback<T> callback;
		T userData = null;
		final Collection<libusb_hotplug_event> events = new LinkedHashSet<>();
		final Collection<libusb_hotplug_flag> flags = new LinkedHashSet<>();
		int vendor = LIBUSB_HOTPLUG_MATCH_ANY;
		int product = LIBUSB_HOTPLUG_MATCH_ANY;
		libusb_class_code deviceClass = null;

		Registration(UsbHotplug hotPlug, Callback<T> callback) {
			this.hotPlug = hotPlug;
			this.callback = callback;
		}

		public Registration<T> userData(T userData) {
			this.userData = userData;
			return this;
		}

		public Registration<T> events(libusb_hotplug_event... events) {
			return events(Arrays.asList(events));
		}

		public Registration<T> events(Collection<libusb_hotplug_event> events) {
			this.events.addAll(events);
			return this;
		}

		public Registration<T> allEvents() {
			return events(libusb_hotplug_event.xcoder.all());
		}

		public Registration<T> flags(libusb_hotplug_flag... flags) {
			return flags(Arrays.asList(flags));
		}

		public Registration<T> flags(Collection<libusb_hotplug_flag> flags) {
			this.flags.addAll(flags);
			return this;
		}

		public Registration<T> vendor(int vendor) {
			this.vendor = vendor;
			return this;
		}

		public Registration<T> product(int product) {
			this.product = product;
			return this;
		}

		public Registration<T> deviceClass(libusb_class_code deviceClass) {
			this.deviceClass = deviceClass;
			return this;
		}
		
		public CallbackHandle register() throws LibUsbException {
			return hotPlug.register(this);
		}
	}

	public static boolean hasCapability() {
		return LibUsb.libusb_has_capability(LIBUSB_CAP_HAS_HOTPLUG);
	}

	UsbHotplug(Usb usb) {
		this.usb = usb;
	}

	public Registration<?> registration(Callback<?> callback) {
		return new Registration<>(this, callback);
	}

	public <T> Registration<T> registration(Callback<T> callback, T userData) {
		return new Registration<>(this, callback).userData(userData);
	}

	public void deregister(CallbackHandle handle) throws LibUsbException {
		libusb_hotplug_deregister_callback(context(), handle.handle);
		untrackCallbackByHandle(handle.handle.value);
	}

	@Override
	public void close() {
		callbackHandles.clear();
		callbackIds.clear();
	}

	private <T> CallbackHandle register(Registration<T> registration)
		throws LibUsbException {
		int callbackId = this.callbackId.incrementAndGet();
		libusb_hotplug_callback_fn jnaCallback = (ctx, dev, evt, user_data) -> adaptCallback(dev,
			evt, callbackId, registration.callback, registration.userData);
		libusb_hotplug_callback_handle handle = libusb_hotplug_register_callback(context(),
			libusb_hotplug_event.xcoder.encode(registration.events),
			libusb_hotplug_flag.xcoder.encode(registration.flags), valueOrAny(registration.vendor),
			valueOrAny(registration.product), valueOrAny(registration.deviceClass), jnaCallback,
			null);
		trackCallback(handle.value, callbackId, jnaCallback);
		return new CallbackHandle(handle);
	}

	@SuppressWarnings("resource")
	private <T> int adaptCallback(libusb_device dev, int evt, int callbackId, Callback<T> callback,
		T userData) {
		try {
			UsbDevice device = new UsbDevice(usb, dev);
			libusb_hotplug_event event = libusb_hotplug_event.xcoder.decode(evt);
			boolean result = callback.event(usb, device, event, userData);
			if (result) untrackCallbackById(callbackId);
			return result ? 1 : 0;
		} catch (IOException | RuntimeException e) {
			logger.catching(e);
			return 0; // don't stop receiving events
		}
	}

	private void trackCallback(int handle, int id, libusb_hotplug_callback_fn callback) {
		CallbackContext context = new CallbackContext(handle, id, callback);
		callbackHandles.put(handle, context);
		callbackIds.put(id, context);
	}

	private void untrackCallbackByHandle(int handle) {
		CallbackContext context = callbackHandles.remove(handle);
		if (context != null) callbackIds.remove(context.id);
	}

	private void untrackCallbackById(int id) {
		CallbackContext context = callbackIds.remove(id);
		if (context != null) callbackHandles.remove(context.handle);
	}

	private int valueOrAny(int value) {
		return value == 0 ? LIBUSB_HOTPLUG_MATCH_ANY : value;
	}

	private int valueOrAny(libusb_class_code dev) {
		return dev == null ? LIBUSB_HOTPLUG_MATCH_ANY : dev.value;
	}

	private libusb_context context() {
		return usb.context();
	}
}
