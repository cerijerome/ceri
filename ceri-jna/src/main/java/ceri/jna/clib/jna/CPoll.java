package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.Pointer;
import ceri.jna.clib.jna.CSignal.sigset_t;
import ceri.jna.clib.jna.CTime.timespec;
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
	}

	/**
	 * Examines a set of file descriptors to see if some of them are ready for I/O, or if certain
	 * events have occurred on them. Timeout is in milliseconds; a timeout of -1 blocks until an
	 * event occurs. Returns the number of descriptors with returned events.
	 */
	public static int poll(pollfd[] fds, int timeoutMs) throws CException {
		var p = initFds(fds);
		var nfds = fds.length;
		int n = caller.verifyInt(() -> lib().poll(p, nfds, timeoutMs), "poll", p, nfds, timeoutMs);
		if (n > 0) Struct.read(fds, "revents");
		return n;
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static final class Linux {
		private Linux() {}

		/**
		 * Examines a set of file descriptors to see if some of them are ready for I/O, if certain
		 * events have occurred on them, or if the signals from the given set are raised. A null
		 * timeout blocks until an event occurs. Returns the number of descriptors with returned
		 * events.
		 */
		public static int ppoll(pollfd[] fds, timespec tmo, sigset_t sigmask) throws CException {
			var p = initFds(fds);
			var nfds = fds.length;
			Struct.write(tmo);
			int n = caller.verifyInt(
				() -> lib().ppoll(p, nfds, Struct.pointer(tmo), Struct.pointer(sigmask)), "poll", p,
				nfds, tmo, sigmask);
			if (n > 0) Struct.read(fds, "revents");
			return n;
		}
	}

	private static Pointer initFds(pollfd[] fds) throws CException {
		CUtil.requireContiguous(fds);
		for (var fd : fds)
			fd.revents = 0;
		return Struct.pointer(Struct.write(fds));
	}
}
