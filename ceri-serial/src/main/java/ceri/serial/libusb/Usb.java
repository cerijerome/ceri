package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_error_name;
import static ceri.serial.libusb.jna.LibUsb.libusb_event_handler_active;
import static ceri.serial.libusb.jna.LibUsb.libusb_event_handling_ok;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_container_id_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_device_list;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_next_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_pollfds;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_ss_endpoint_companion_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_ss_usb_device_capability_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_usb_2_0_extension_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_version;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_locked;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_has_capability;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_init_default;
import static ceri.serial.libusb.jna.LibUsb.libusb_lock_event_waiters;
import static ceri.serial.libusb.jna.LibUsb.libusb_lock_events;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_open_device_with_vid_pid;
import static ceri.serial.libusb.jna.LibUsb.libusb_pollfds_handle_timeouts;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_debug;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_pollfd_notifiers;
import static ceri.serial.libusb.jna.LibUsb.libusb_setlocale;
import static ceri.serial.libusb.jna.LibUsb.libusb_strerror;
import static ceri.serial.libusb.jna.LibUsb.libusb_try_lock_events;
import static ceri.serial.libusb.jna.LibUsb.libusb_unlock_event_waiters;
import static ceri.serial.libusb.jna.LibUsb.libusb_unlock_events;
import static ceri.serial.libusb.jna.LibUsb.libusb_wait_for_event;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_device_callback;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.ExceptionPredicate;
import ceri.log.util.LogUtil;
import ceri.serial.jna.clib.Time;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_log_level;
import ceri.serial.libusb.jna.LibUsb.libusb_poll_event;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_version;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;
import ceri.serial.libusb.jna.LibUsbNotFoundException;

public class Usb implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final UsbHotplug hotplug;
	private libusb_context context;
	private final Set<Object> pollfdCallbackRefs = ConcurrentHashMap.newKeySet();

	public interface PollfdAddedCallback<T> {
		void invoke(int fd, Set<libusb_poll_event> events, T userData) throws IOException;
	}

	public interface PollfdRemovedCallback<T> {
		void invoke(int fd, T userData) throws IOException;
	}

	public static libusb_version version() throws LibUsbException {
		return libusb_get_version();
	}

	public static boolean hasCapability(libusb_capability capability) {
		return libusb_has_capability(capability);
	}

	public static void setLocale(Locale locale) throws LibUsbException {
		libusb_setlocale(locale.toString());
	}

	public static String errorName(libusb_error error) {
		return libusb_error_name(error);
	}

	public static String errorString(libusb_error error) {
		return libusb_strerror(error);
	}

	public static libusb_device_criteria criteria(String criteria) {
		return LibUsbFinder.libusb_find_criteria_string(criteria);
	}

	public static libusb_device_criteria criteria() {
		return LibUsbFinder.libusb_find_criteria();
	}

	/**
	 * Take ownership of the given context. This instance takes responsibility for freeing the
	 * context on close.
	 */
	public static Usb from(libusb_context context) {
		return new Usb(context);
	}

	public static Usb init() throws LibUsbException {
		return new Usb(libusb_init());
	}

	public static Usb initDefault() throws LibUsbException {
		libusb_init_default();
		return new Usb(null);
	}

	private Usb(libusb_context context) {
		this.context = context;
		hotplug = new UsbHotplug(this);
	}

	public void debug(libusb_log_level level) {
		libusb_set_debug(context(), level);
	}

	public UsbDeviceHandle openDevice(libusb_device_criteria criteria) throws LibUsbException {
		libusb_device_handle[] handles = new libusb_device_handle[1];
		libusb_find_device_callback(context(), criteria, dev -> {
			handles[0] = libusb_open(dev);
			return true;
		});
		if (handles[0] != null) return new UsbDeviceHandle(this::context, handles[0]);
		throw new LibUsbNotFoundException("Device not found, " + criteria);
	}

	public UsbDeviceHandle openDevice(int vendorId, int productId) throws LibUsbException {
		libusb_device_handle handle =
			libusb_open_device_with_vid_pid(context(), vendorId, productId);
		return new UsbDeviceHandle(this::context, handle);
	}

	/**
	 * Find device based on criteria. The callback takes responsibility for closing the given device
	 * if it returns true, otherwise the device is automatically closed.
	 */
	public boolean findDevice(libusb_device_criteria criteria,
		ExceptionPredicate<LibUsbException, UsbDevice> callback) throws LibUsbException {
		if (callback == null) return false;
		return LibUsbFinder.libusb_find_device_callback(context(), criteria,
			dev -> findDeviceCallback(dev, callback));
	}

	private boolean findDeviceCallback(libusb_device dev,
		ExceptionPredicate<LibUsbException, UsbDevice> callback) throws LibUsbException {
		UsbDevice device = new UsbDevice(this::context, dev);
		try {
			boolean result = callback.test(device);
			if (!result) device.close();
			return result;
		} catch (LibUsbException | RuntimeException e) {
			device.close();
			throw e;
		}
	}

	public UsbDevice findDeviceRef(libusb_device_criteria criteria) throws LibUsbException {
		libusb_device device = LibUsbFinder.libusb_find_device_ref(context(), criteria);
		if (device == null) return null;
		return new UsbDevice(this::context, device, 1);
	}

	public UsbDeviceList deviceList() throws LibUsbException {
		libusb_device.ByReference list = libusb_get_device_list(context());
		return new UsbDeviceList(this::context, list);
	}

	public boolean eventHandlerActive() throws LibUsbException {
		return libusb_event_handler_active(context());
	}

	public boolean eventHandlingOk() throws LibUsbException {
		return libusb_event_handling_ok(context());
	}

	public Duration nextTimeout() throws LibUsbException {
		return Time.Util.duration(libusb_get_next_timeout(context()));
	}

	public void handleEvents() throws LibUsbException {
		libusb_handle_events(context());
	}

	public int handleEventsCompleted() throws LibUsbException {
		return libusb_handle_events_completed(context());
	}

	public void handleEventsLocked(Duration d) throws LibUsbException {
		libusb_handle_events_locked(context(), Time.Util.timeval(d));
	}

	public void handleEventsTimeout(Duration d) throws LibUsbException {
		libusb_handle_events_timeout(context(), Time.Util.timeval(d));
	}

	public int handleEventsTimeoutCompleted(Duration d) throws LibUsbException {
		return libusb_handle_events_timeout_completed(context(), Time.Util.timeval(d));
	}

	public void lockEventWaiters() throws LibUsbException {
		libusb_lock_event_waiters(context());
	}

	public void lockEvents() throws LibUsbException {
		libusb_lock_events(context());
	}

	public void tryLockEvents() throws LibUsbException {
		libusb_try_lock_events(context());
	}

	public void unlockEventWaiters() throws LibUsbException {
		libusb_unlock_event_waiters(context());
	}

	public void unlockEvents() throws LibUsbException {
		libusb_unlock_events(context());
	}

	public void waitForEvent(Duration d) throws LibUsbException {
		libusb_wait_for_event(context(), Time.Util.timeval(d));
	}

	public UsbPollFds pollfds() throws LibUsbException {
		return new UsbPollFds(libusb_get_pollfds(context()));
	}

	public boolean pollfdsHandleTimeouts() throws LibUsbException {
		return libusb_pollfds_handle_timeouts(context());
	}

	public <T> void pollfdNotifiers(PollfdAddedCallback<T> addedCallback,
		PollfdRemovedCallback<T> removedCallback, T userData) throws LibUsbException {
		libusb_pollfd_added_cb jnaAddedCallback = addedCallback == null ? null :
			(fd, events, user_data) -> pollfdAdded(fd, events, addedCallback, userData);
		libusb_pollfd_removed_cb jnaRemovedCallback = removedCallback == null ? null :
			(fd, user_data) -> pollfdRemoved(fd, removedCallback, userData);
		libusb_set_pollfd_notifiers(context(), jnaAddedCallback, jnaRemovedCallback, null);
		updatePollfdCallbackRefs(jnaAddedCallback, jnaRemovedCallback);
	}

	private void updatePollfdCallbackRefs(libusb_pollfd_added_cb addedCallback,
		libusb_pollfd_removed_cb removedCallback) {
		pollfdCallbackRefs.clear();
		if (addedCallback != null) pollfdCallbackRefs.add(addedCallback);
		if (removedCallback != null) pollfdCallbackRefs.add(removedCallback);
	}

	private <T> void pollfdAdded(int fd, short events, PollfdAddedCallback<T> callback,
		T userData) {
		try {
			callback.invoke(fd, libusb_poll_event.xcoder.decode(events), userData);
		} catch (IOException | RuntimeException e) {
			logger.catching(e);
		}
	}

	private <T> void pollfdRemoved(int fd, PollfdRemovedCallback<T> callback, T userData) {
		try {
			callback.invoke(fd, userData);
		} catch (IOException | RuntimeException e) {
			logger.catching(e);
		}
	}

	public UsbHotplug hotplug() {
		return hotplug;
	}

	public UsbDescriptor.SsEndpointCompanion
		ssEndpointCompanionDescriptor(libusb_endpoint_descriptor endpoint) throws LibUsbException {
		return new UsbDescriptor.SsEndpointCompanion(
			libusb_get_ss_endpoint_companion_descriptor(context(), endpoint));
	}

	public UsbDescriptor.SsUsbDeviceCapability ssUsbDeviceCapabilityDescriptor(
		libusb_bos_dev_capability_descriptor devCap) throws LibUsbException {
		return new UsbDescriptor.SsUsbDeviceCapability(
			libusb_get_ss_usb_device_capability_descriptor(context(), devCap));
	}

	public UsbDescriptor.Usb20Extension usb20ExtensionDescriptor(
		libusb_bos_dev_capability_descriptor devCap) throws LibUsbException {
		return new UsbDescriptor.Usb20Extension(
			libusb_get_usb_2_0_extension_descriptor(context(), devCap));
	}

	public UsbDescriptor.ContainerId containerIdDescriptor(
		libusb_bos_dev_capability_descriptor descriptor) throws LibUsbException {
		return new UsbDescriptor.ContainerId(
			libusb_get_container_id_descriptor(context(), descriptor));
	}

	@Override
	public void close() {
		LogUtil.close(logger, hotplug, () -> libusb_exit(context), pollfdCallbackRefs::clear);
		context = null;
	}

	public UsbDevice wrap(libusb_device device) {
		return wrap(device, 0);
	}

	public UsbDevice wrap(libusb_device device, int refs) {
		return new UsbDevice(this::context, device, refs);
	}

	public libusb_context context() {
		if (context != null) return context;
		throw new IllegalStateException("Context has been closed");
	}

}
