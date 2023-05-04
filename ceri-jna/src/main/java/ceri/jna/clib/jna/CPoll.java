package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <poll.h>}
 */
public class CPoll {
	public static final int POLLIN = 0x0001;
	public static final int POLLPRI = 0x0002;
	public static final int POLLOUT = 0x0004;
	public static final int POLLERR = 0x0008; // revents only
	public static final int POLLHUP = 0x0010; // revents only
	public static final int POLLNVAL = 0x0020; // revents only
	public static final int POLLRDNORM = 0x0040;
	public static final int POLLRDBAND = 0x0080;
	public static final int POLLWRNORM;
	public static final int POLLWRBAND;

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
	public static int poll(pollfd[] fds, int timeout) throws CException {
		if (fds.length > 0 && !Struct.isByVal(fds))
			throw CException.full("Array is not contiguous", CError.EINVAL);
		var p = Struct.pointer(fds);
		int nfds = fds.length;
		int n = caller.verifyInt(() -> lib().poll(p, nfds, timeout), "poll", p, nfds, timeout);
		if (n > 0) Struct.read(fds, "revents");
		return n;
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			POLLWRNORM = 0x4;
			POLLWRBAND = 0x100;
		} else {
			POLLWRNORM = 0x100;
			POLLWRBAND = 0x200;
		}
	}
}
