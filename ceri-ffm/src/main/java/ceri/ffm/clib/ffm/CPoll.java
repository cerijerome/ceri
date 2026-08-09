package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import ceri.common.math.Maths;
import ceri.common.util.Validate;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Struct;

/**
 * Types and functions from {@code <poll.h>}
 */
@CInclude("poll.h")
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
	 * Data structure for a polling request.
	 */
	@Fields({ "fd", "events", "revents" })
	public static class pollfd extends Struct<pollfd> {
		/** Operational support for the type. */
		public static final Supporter<pollfd> $ = support(pollfd.class);
		/** File descriptor to be polled. */
		public int fd;
		/** Events of interest. */
		public short events;
		/** Events that occurred. */
		public short revents;
	}

	/**
	 * Examines file descriptors for I/O readiness, or occurred events. Timeout is in milliseconds;
	 * a timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(int timeoutMs, pollfd... pollFds) throws CException {
		if (Validate.nonNull(pollFds).length == 0) return 0;
		try (var arena = Arena.ofConfined()) {
			for (var pollFd : pollFds)
				pollFd.revents = 0;
			var pointer = pollfd.$.pointerOfArray(arena, pollFds, false);
			int n = poll(pointer, pollFds.length, timeoutMs);
			if (n > 0) pointer.readArray(pollFds, false); // TODO: optimize read for revents only
			// if (n > 0) Struct.read(fds, "revents");
			return n;
		}
	}

	// TODO:
	// - mechanism to update a single group field by name/index to/from memory
	// - read/write pollfd.revents

	/**
	 * Examines file descriptors for I/O readiness, or occurred events. Timeout is in milliseconds;
	 * a timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(Pointer<pollfd> fds, int timeoutMs) throws CException {
		return poll(fds, Integer.MAX_VALUE, timeoutMs);
	}

	/**
	 * Examines file descriptors for I/O readiness, or occurred events. Timeout is in milliseconds;
	 * a timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(Pointer<pollfd> fds, int nfds, int timeoutMs) throws CException {
		int count = (int) Maths.limit(nfds, 0, fds.count());
		int n = CLib.caller.verifyInt(lib -> lib.poll(fds, count, timeoutMs), -1, "poll", fds,
			count, timeoutMs);
		return n;
	}
}
