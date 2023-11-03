package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import java.util.stream.IntStream;
import com.sun.jna.Pointer;
import ceri.common.collection.StreamUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <poll.h>}
 */
public class CPoll {
	/** There is data to read. */
	public static final int POLLIN = 0x0001;
	/** There is some exceptional condition on the file descriptor. */
	public static final int POLLPRI = 0x0002;
	/** Writing is now possible. */
	public static final int POLLOUT = 0x0004;
	/** Error condition; revents only. */
	public static final int POLLERR = 0x0008;
	/** Hang up, peer closed channel; revents only. */
	public static final int POLLHUP = 0x0010;
	/** Invalid request, fd not open; revents only. */
	public static final int POLLNVAL = 0x0020;

	private CPoll() {}

	/**
	 * Data structure for a polling request. Use array(n) to create a contiguous array.
	 */
	@Fields({ "fd", "events", "revents" })
	public static class pollfd extends Struct {
		public int fd; // file descriptor to poll
		public short events; // events of interest
		public short revents; // events that occurred

		public static pollfd[] array(int size) {
			return Struct.<pollfd>arrayByVal(() -> new pollfd(), pollfd[]::new, size);
		}

		public pollfd() {}

		public pollfd(Pointer p) {
			super(p);
		}

		/**
		 * Initialize the struct with fd and events.
		 */
		public pollfd init(int fd, int... events) {
			this.fd = fd;
			this.events = (short) StreamUtil.bitwiseOr(IntStream.of(events));
			this.revents = 0;
			return this;
		}

		/**
		 * Returns true if revents has the given event.
		 */
		public boolean hasEvent(int event) {
			return (revents & event) != 0;
		}

		/**
		 * Returns true if revents has any event.
		 */
		public boolean hasEvent() {
			return revents != 0;
		}

		/**
		 * Throws an exception if any error conditions occurred on revents.
		 */
		public pollfd verify() throws CException {
			if (hasEvent(POLLERR)) throw CException.general("POLLERR on fd %d", fd);
			if (hasEvent(POLLHUP)) throw CException.general("POLLHUP on fd %d", fd);
			if (hasEvent(POLLNVAL)) throw CException.general("POLLNVAL on fd %d", fd);
			return this;
		}
	}

	/**
	 * Examines a file descriptor to see if it is ready for I/O, or if certain events have occurred.
	 * Timeout is in milliseconds; a timeout of -1 blocks until an event occurs. Returns the true if
	 * the descriptor has a returned event.
	 */
	public static boolean poll(pollfd fd, int timeoutMs) throws CException {
		return poll(new pollfd[] { fd }, timeoutMs) == 1;
	}

	/**
	 * Examines a set of file descriptors to see if some of them are ready for I/O, or if certain
	 * events have occurred on them. Timeout is in milliseconds; a timeout of -1 blocks until an
	 * event occurs. Returns the number of descriptors with returned events.
	 */
	public static int poll(pollfd[] fds, int timeoutMs) throws CException {
		if (fds.length > 0 && !Struct.isByVal(fds))
			throw CException.full("Array is not contiguous", CError.EINVAL);
		Struct.write(fds);
		var p = Struct.pointer(fds);
		int nfds = fds.length;
		int n = caller.verifyInt(() -> lib().poll(p, nfds, timeoutMs), "poll", p, nfds, timeoutMs);
		if (n > 0) Struct.read(fds, "revents");
		return n;
	}
}
