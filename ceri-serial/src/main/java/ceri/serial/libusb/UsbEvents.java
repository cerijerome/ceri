package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_event_handler_active;
import static ceri.serial.libusb.jna.LibUsb.libusb_event_handling_ok;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_next_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_pollfds;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_locked;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout_completed;
import static ceri.serial.libusb.jna.LibUsb.libusb_interrupt_event_handler;
import static ceri.serial.libusb.jna.LibUsb.libusb_pollfds_handle_timeouts;
import static ceri.serial.libusb.jna.LibUsb.libusb_set_pollfd_notifiers;
import static ceri.serial.libusb.jna.LibUsb.libusb_wait_for_event;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionCloseable;
import ceri.common.util.Enclosed;
import ceri.log.util.LogUtil;
import ceri.serial.clib.jna.Time;
import ceri.serial.jna.PointerRef;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_poll_event;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Encapsulation of context-based event handling.
 */
public class UsbEvents implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Usb usb;
	private final Set<Object> pollfdCallbackRefs = ConcurrentHashMap.newKeySet();

	public interface PollAddListener<T> {
		void invoke(int fd, Set<libusb_poll_event> events, T userData) throws IOException;
	}

	public interface PollRemoveListener<T> {
		void invoke(int fd, T userData) throws IOException;
	}

	public static class PollFd {
		private libusb_pollfd pollFd;

		PollFd(libusb_pollfd pollFd) {
			this.pollFd = pollFd;
		}

		public int fd() {
			return pollFd.fd;
		}

		public Set<libusb_poll_event> events() {
			return pollFd.events();
		}
	}

	UsbEvents(Usb usb) {
		this.usb = usb;
	}

	/**
	 * Attempts to lock event-handling. Returns an Enclosed type, with boolean to indicate if
	 * locking was successful. Events are unlocked on close only if true.
	 */
	public Enclosed<Boolean> tryLock() throws LibUsbException {
		if (!LibUsb.libusb_try_lock_events(context())) return Enclosed.noOp(false);
		return Enclosed.of(true, t -> unlockEvents());
	}

	/**
	 * Locks event-handling. Returns a closable type, that unlocks events on close.
	 */
	public ExceptionCloseable<RuntimeException> lock() throws LibUsbException {
		LibUsb.libusb_lock_events(context());
		return () -> unlockEvents();
	}

	/**
	 * Acquires the event waiters lock. Returns a closable type that unlocks on close.
	 */
	public ExceptionCloseable<RuntimeException> lockWaiters() throws LibUsbException {
		LibUsb.libusb_lock_events(context());
		return () -> unlockEventWaiters();
	}

	public boolean handlerActive() throws LibUsbException {
		return libusb_event_handler_active(context());
	}

	public boolean handlingOk() throws LibUsbException {
		return libusb_event_handling_ok(context());
	}

	public void interruptHandler() throws LibUsbException {
		libusb_interrupt_event_handler(context());
	}

	public Duration nextTimeout() throws LibUsbException {
		return Time.Util.duration(libusb_get_next_timeout(context()));
	}

	public void handle() throws LibUsbException {
		libusb_handle_events(context());
	}

	public int handleCompleted() throws LibUsbException {
		return libusb_handle_events_completed(context());
	}

	public void handleLocked(Duration d) throws LibUsbException {
		libusb_handle_events_locked(context(), Time.Util.timeval(d));
	}

	public void handleTimeout(Duration d) throws LibUsbException {
		libusb_handle_events_timeout(context(), Time.Util.timeval(d));
	}

	public int handleTimeoutCompleted(Duration d) throws LibUsbException {
		return libusb_handle_events_timeout_completed(context(), Time.Util.timeval(d));
	}

	public void await(Duration d) throws LibUsbException {
		libusb_wait_for_event(context(), Time.Util.timeval(d));
	}

	public Enclosed<List<PollFd>> pollFds() throws LibUsbException {
		var ref = libusb_get_pollfds(context());
		var list = pollFds(ref);
		return Enclosed.of(list, t -> freePollFds(ref));
	}

	public boolean pollsHandleTimeouts() throws LibUsbException {
		return libusb_pollfds_handle_timeouts(context());
	}

	public <T> void pollNotifiers(PollAddListener<T> addedCallback,
		PollRemoveListener<T> removedCallback, T userData) throws LibUsbException {
		libusb_pollfd_added_cb jnaAddedCallback = addedCallback == null ? null :
			(fd, events, user_data) -> pollfdAdded(fd, events, addedCallback, userData);
		libusb_pollfd_removed_cb jnaRemovedCallback = removedCallback == null ? null :
			(fd, user_data) -> pollfdRemoved(fd, removedCallback, userData);
		libusb_set_pollfd_notifiers(context(), jnaAddedCallback, jnaRemovedCallback, null);
		updatePollCallbacks(jnaAddedCallback, jnaRemovedCallback);
	}

	@Override
	public void close() throws IOException {
		LogUtil.close(logger, pollfdCallbackRefs::clear);
	}
	
	private void updatePollCallbacks(libusb_pollfd_added_cb addedCallback,
		libusb_pollfd_removed_cb removedCallback) {
		pollfdCallbackRefs.clear();
		if (addedCallback != null) pollfdCallbackRefs.add(addedCallback);
		if (removedCallback != null) pollfdCallbackRefs.add(removedCallback);
	}

	private <T> void pollfdAdded(int fd, short events, PollAddListener<T> callback, T userData) {
		LogUtil.execute(logger,
			() -> callback.invoke(fd, libusb_poll_event.xcoder.decodeAll(events), userData));
	}

	private <T> void pollfdRemoved(int fd, PollRemoveListener<T> callback, T userData) {
		LogUtil.execute(logger, () -> callback.invoke(fd, userData));
	}

	private void unlockEvents() {
		LogUtil.execute(logger, () -> LibUsb.libusb_unlock_events(context()));
	}

	private void unlockEventWaiters() {
		LogUtil.execute(logger, () -> LibUsb.libusb_unlock_event_waiters(context()));
	}

	private static void freePollFds(PointerRef<libusb_pollfd> ref) {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_pollfds(ref));
	}

	private static List<PollFd> pollFds(PointerRef<libusb_pollfd> ref) {
		if (ref == null) return List.of();
		libusb_pollfd[] array = Struct.read(ref.array());
		return ImmutableUtil.collectAsList(Stream.of(array).map(PollFd::new));
	}

	private libusb_context context() {
		return usb.context();
	}

}
