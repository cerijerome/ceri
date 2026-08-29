package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.SegmentAllocator;
import ceri.common.array.Array;
import ceri.common.math.Maths;
import ceri.common.time.TimeSpec;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.Refine.Out;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Route;
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
		/** Access to the field with open array index. */
		public static final Route<Pointer.OfShort> REVENTS = $.route("+.revents");
		/** File descriptor to be polled. */
		public @Out int fd;
		/** Events of interest. */
		public @Out short events;
		/** Events that occurred. */
		public short revents;
	}

	/**
	 * Examines file descriptors for I/O readiness or occurred events. Timeout is in milliseconds; a
	 * timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(int timeoutMs, pollfd... pollFds) throws CException {
		if (Array.isEmpty(pollFds)) return 0;
		try (var arena = Arena.ofConfined()) {
			for (var pollFd : pollFds)
				pollFd.revents = 0;
			var pointer = pollfd.$.pointerOfArray(arena, pollFds, false);
			int n = poll(pointer, timeoutMs);
			if (n > 0) pointer.readAll(pollFds);
			return n;
		}
	}

	/**
	 * Examines file descriptors for I/O readiness or occurred events. Timeout is in milliseconds; a
	 * timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(Pointer<pollfd> fds, int timeoutMs) throws CException {
		return poll(fds, Integer.MAX_VALUE, timeoutMs);
	}

	/**
	 * Examines file descriptors for I/O readiness or occurred events. Timeout is in milliseconds; a
	 * timeout of -1 blocks until an event occurs. Returns the number of descriptors with returned
	 * events.
	 */
	public static int poll(Pointer<pollfd> fds, int nfds, int timeoutMs) throws CException {
		if (fds == null) return 0;
		int count = (int) Maths.limit(nfds, 0, fds.count());
		return CLib.caller.verifyInt(lib -> lib.poll(fds, count, timeoutMs), -1, "poll", fds, count,
			timeoutMs);
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static final class Linux {
		private Linux() {}

		/**
		 * Examines file descriptors for I/O readiness or occurred events, stopping if a signal is
		 * caught. A null timeout blocks until an event or signal occurs. A null signal set ignores
		 * signals. Returns the number of descriptors with returned events.
		 */
		public static int ppoll(int timeoutMs, pollfd[] pollFds, CSignal... signals)
			throws CException {
			if (Array.isEmpty(pollFds)) return 0;
			try (var arena = Arena.ofConfined()) {
				for (var pollFd : pollFds)
					pollFd.revents = 0;
				var pointer = pollfd.$.pointerOfArray(arena, pollFds, false);
				var tmo = timeout(arena, timeoutMs);
				var sigset = Array.isEmpty(signals) ? null : CSignal.sigset(arena, signals);
				int n = ppoll(pointer, tmo, sigset);
				if (n > 0) pointer.readAll(pollFds);
				return n;
			}
		}

		/**
		 * Examines file descriptors for I/O readiness or occurred events, stopping if a signal is
		 * caught. A null timeout blocks until an event or signal occurs. Returns the number of
		 * descriptors with returned events.
		 */
		public static int ppoll(Pointer<pollfd> fds, Pointer<CTime.timespec> tmo,
			Pointer<CSignal.sigset_t> sigmask) throws CException {
			return ppoll(fds, Integer.MAX_VALUE, tmo, sigmask);
		}

		/**
		 * Examines file descriptors for I/O readiness or occurred events, stopping if a signal is
		 * caught. A null timeout blocks until an event or signal occurs. Returns the number of
		 * descriptors with returned events.
		 */
		public static int ppoll(Pointer<pollfd> fds, int nfds, Pointer<CTime.timespec> tmo,
			Pointer<CSignal.sigset_t> sigmask) throws CException {
			if (fds == null) return 0;
			int count = (int) Maths.limit(nfds, 0, fds.count());
			return CLib.caller.verifyInt(lib -> lib.ppoll(fds, count, tmo, sigmask), -1, "ppoll",
				fds, count, tmo, sigmask);
		}
	}

	// support

	private static Pointer<CTime.timespec> timeout(SegmentAllocator allocator, long timeoutMs) {
		if (timeoutMs < 0) return null;
		var tmo = new CTime.timespec().set(TimeSpec.fromMillis(timeoutMs));
		return CTime.timespec.$.pointerOf(allocator, tmo);
	}
}
