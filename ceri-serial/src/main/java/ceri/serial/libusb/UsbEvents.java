package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ushort;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import com.sun.jna.ptr.IntByReference;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.RuntimeCloseable;
import ceri.common.time.TimeSpec;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.type.Struct;
import ceri.log.util.LogUtil;
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
public class UsbEvents {
	private final Usb usb;
	private libusb_pollfd_added_cb addedCallback; // ref to prevent gc
	private libusb_pollfd_removed_cb removedCallback; // ref to prevent gc

	public static class Completed {
		private final IntByReference completed = new IntByReference();

		public static Completed of() {
			return new Completed();
		}

		private Completed() {}

		public void complete(int value) {
			completed.setValue(value);
		}

		public int value() {
			return completed.getValue();
		}

		public boolean completed() {
			return value() != 0;
		}
	}

	public static record PollFd(int fd, int events) {
		public Set<libusb_poll_event> pollEvents() {
			return libusb_poll_event.xcoder.decodeAll(events);
		}
	}

	UsbEvents(Usb usb) {
		this.usb = usb;
	}

	/**
	 * Attempts to lock event-handling. Returns an Enclosed type, with boolean to indicate if
	 * locking was successful. Events are unlocked on close only if true.
	 */
	public Enclosed<RuntimeException, Boolean> tryLock() throws LibUsbException {
		if (!LibUsb.libusb_try_lock_events(context())) return Enclosed.noOp(false);
		return Enclosed.of(true, _ -> unlockEvents());
	}

	/**
	 * Locks event-handling. Returns a closable type, that unlocks events on close.
	 */
	public RuntimeCloseable lock() throws LibUsbException {
		LibUsb.libusb_lock_events(context());
		return () -> unlockEvents();
	}

	/**
	 * Acquires the event waiters lock. Returns a closable type that unlocks on close.
	 */
	public RuntimeCloseable lockWaiters() throws LibUsbException {
		LibUsb.libusb_lock_event_waiters(context());
		return () -> unlockEventWaiters();
	}

	public boolean handlerActive() throws LibUsbException {
		return LibUsb.libusb_event_handler_active(context());
	}

	public boolean handlingOk() throws LibUsbException {
		return LibUsb.libusb_event_handling_ok(context());
	}

	public void interruptHandler() throws LibUsbException {
		LibUsb.libusb_interrupt_event_handler(context());
	}

	public Duration nextTimeout() throws LibUsbException {
		return LibUsb.libusb_get_next_timeout(context()).time().toDuration();
	}

	public void handle() throws LibUsbException {
		LibUsb.libusb_handle_events(context());
	}

	public int handleCompleted(Completed completed) throws LibUsbException {
		return LibUsb.libusb_handle_events_completed(context(),
			completed == null ? null : completed.completed);
	}

	public void handleLocked(Duration d) throws LibUsbException {
		LibUsb.libusb_handle_events_locked(context(), time(d));
	}

	public void handleTimeout(Duration d) throws LibUsbException {
		LibUsb.libusb_handle_events_timeout(context(), time(d));
	}

	public int handleTimeoutCompleted(Duration d, Completed completed) throws LibUsbException {
		return LibUsb.libusb_handle_events_timeout_completed(context(), time(d),
			completed == null ? null : completed.completed);
	}

	public void await(Duration d) throws LibUsbException {
		LibUsb.libusb_wait_for_event(context(), time(d));
	}

	public List<PollFd> pollFds() throws LibUsbException {
		var ref = LibUsb.libusb_get_pollfds(context());
		try {
			libusb_pollfd[] array = Struct.read(ref.get());
			return ImmutableUtil.collectAsList(
				Stream.of(array).map(pollFd -> new PollFd(pollFd.fd, ushort(pollFd.events))));
		} finally {
			LogUtil.close(() -> LibUsb.libusb_free_pollfds(ref));
		}
	}

	public boolean pollHandleTimeouts() throws LibUsbException {
		return LibUsb.libusb_pollfds_handle_timeouts(context());
	}

	public void pollNotifiers(ExceptionConsumer<IOException, PollFd> addedCallback,
		ExceptionIntConsumer<IOException> removedCallback) throws LibUsbException {
		this.addedCallback = addedCallback(addedCallback);
		this.removedCallback = removedCallback(removedCallback);
		LibUsb.libusb_set_pollfd_notifiers(context(), this.addedCallback, this.removedCallback,
			null);
	}

	private libusb_pollfd_added_cb addedCallback(ExceptionConsumer<IOException, PollFd> callback) {
		if (callback == null) return null;
		return (fd, events, _) -> LogUtil
			.runSilently(() -> callback.accept(new PollFd(fd, ushort(events))));
	}

	private libusb_pollfd_removed_cb removedCallback(ExceptionIntConsumer<IOException> callback) {
		if (callback == null) return null;
		return (fd, _) -> LogUtil.runSilently(() -> callback.accept(fd));
	}

	private void unlockEvents() {
		LogUtil.runSilently(() -> LibUsb.libusb_unlock_events(context()));
	}

	private void unlockEventWaiters() {
		LogUtil.runSilently(() -> LibUsb.libusb_unlock_event_waiters(context()));
	}

	private libusb_context context() {
		return usb.context();
	}

	private static timeval time(Duration d) {
		return Struct.write(new timeval().time(TimeSpec.from(d)));
	}
}
