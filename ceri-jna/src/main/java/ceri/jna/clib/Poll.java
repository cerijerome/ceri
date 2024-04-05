package ceri.jna.clib;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import ceri.common.collection.StreamUtil;
import ceri.common.data.TypeTranscoder;
import ceri.common.math.MathUtil;
import ceri.common.time.TimeSpec;
import ceri.common.util.OsUtil;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CTime;

/**
 * Encapsulates poll fds and events for polling calls.
 */
public class Poll implements Iterable<Poll.Fd> {
	private final CPoll.pollfd[] pollfds;
	private final List<Fd> fds;

	/**
	 * Request and response events.
	 */
	public static enum Event {
		POLLIN(CPoll.POLLIN),
		POLLPRI(CPoll.POLLPRI),
		POLLOUT(CPoll.POLLOUT);

		private static final TypeTranscoder<Event> xcoder =
			TypeTranscoder.of(t -> t.value, Event.class);
		public final int value;

		private Event(int value) {
			this.value = value;
		}
	}

	/**
	 * Response errors.
	 */
	public static enum Error {
		POLLERR(CPoll.POLLERR),
		POLLHUP(CPoll.POLLHUP),
		POLLNVAL(CPoll.POLLNVAL);

		private static final TypeTranscoder<Error> xcoder =
			TypeTranscoder.of(t -> t.value, Error.class);
		public final int value;

		private Error(int value) {
			this.value = value;
		}
	}

	/**
	 * Encapsulates an fd for polling.
	 */
	public class Fd {
		private final int index;

		private Fd(int index) {
			this.index = index;
		}

		/**
		 * Sets the file descriptor.
		 */
		public Fd fd(FileDescriptor fd) throws IOException {
			return fd.apply(f -> fd(f));
		}

		/**
		 * Sets the file descriptor.
		 */
		public Fd fd(int fd) {
			pollfd().fd = fd;
			return this;
		}

		/**
		 * Sets request events.
		 */
		public Fd request(Event... events) {
			for (var event : events)
				pollfd().events |= event.value;
			return this;
		}

		/**
		 * Returns true if response events include the event.
		 */
		public boolean has(Event event) {
			return (revents() & event.value) != 0;
		}

		/**
		 * Provides all typed response events for this fd.
		 */
		public Set<Event> responses() {
			return Event.xcoder.decodeAll(revents());
		}

		/**
		 * Returns true if response errors include the error.
		 */
		public boolean has(Error error) {
			return (revents() & error.value) != 0;
		}

		/**
		 * Provides all typed response errors for this fd.
		 */
		public Set<Error> errors() {
			return Error.xcoder.decodeAll(revents());
		}

		/**
		 * Throws an exception if any response errors are present.
		 */
		public Fd validate() throws IOException {
			var errors = errors();
			if (errors.isEmpty()) return this;
			throw new IOException("Poll errors [" + index + "] = " + errors);
		}

		/**
		 * Returns the raw response value, including events and errors.
		 */
		public int revents() {
			return MathUtil.ushort(pollfd().revents);
		}

		private CPoll.pollfd pollfd() {
			return pollfds[index];
		}
	}

	/**
	 * Creates an array of poll fds.
	 */
	public static Poll of(int count) {
		return new Poll(CPoll.pollfd.array(count));
	}

	/**
	 * Convenience constructor for a single poll fd.
	 */
	public static Poll of(FileDescriptor fd, Event... events) throws IOException {
		var poll = of(1);
		poll.fd(0).fd(fd).request(events);
		return poll;
	}

	private Poll(CPoll.pollfd[] pollfds) {
		this.pollfds = pollfds;
		fds = IntStream.range(0, pollfds.length).mapToObj(Fd::new).toList();
	}

	/**
	 * Poll without timeout.
	 */
	public int poll() throws IOException {
		return poll((Integer) null);
	}

	/**
	 * Poll with timeout.
	 */
	public int poll(Integer timeoutMs) throws IOException {
		return CPoll.poll(pollfds, timeoutMs == null ? -1 : timeoutMs);
	}

	/**
	 * Poll with signal set (Linux only).
	 */
	public int poll(SigSet sigset) throws IOException {
		return poll(null, sigset);
	}

	/**
	 * Poll with timeout and signal set (Linux only).
	 */
	public int poll(TimeSpec timeout, SigSet sigset) throws IOException {
		var os = OsUtil.os();
		if (!os.linux) throw new UnsupportedOperationException("Linux only: " + os);
		return CPoll.Linux.ppoll(pollfds, CTime.timespec.of(timeout), SigSet.struct(sigset));
	}

	/**
	 * Combines responses from all fds.
	 */
	public Set<Event> responses() {
		return StreamUtil.toSet(fds().stream().flatMap(fd -> fd.responses().stream()));
	}

	/**
	 * Combines errors from all fds.
	 */
	public Set<Error> errors() {
		return StreamUtil.toSet(fds().stream().flatMap(fd -> fd.errors().stream()));
	}

	/**
	 * Throws an exception if any fd has an error response.
	 */
	public Poll validate() throws IOException {
		for (var fd : this)
			fd.validate();
		return this;
	}

	/**
	 * Iterates over fds.
	 */
	@Override
	public Iterator<Fd> iterator() {
		return fds().iterator();
	}

	/**
	 * Direct access to the fds.
	 */
	public List<Fd> fds() {
		return fds;
	}

	/**
	 * Provides access to fds by index.
	 */
	public Fd fd(int i) {
		return fds().get(i);
	}

	/**
	 * Returns the number of fds.
	 */
	public int size() {
		return fds().size();
	}
}
