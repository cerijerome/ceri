package ceri.jna.clib;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import ceri.common.collection.StreamUtil;
import ceri.common.data.TypeTranscoder;
import ceri.common.math.MathUtil;
import ceri.jna.clib.jna.CPoll;

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

	public class Fd {
		private final int index;

		private Fd(int index) {
			this.index = index;
		}

		public Fd fd(FileDescriptor fd) throws IOException {
			return fd.apply(f -> fd(f));
		}

		public Fd fd(int fd) {
			pollfd().fd = fd;
			return this;
		}

		public Fd request(Event... events) {
			for (var event : events)
				pollfd().events |= event.value;
			return this;
		}

		public boolean has(Event event) {
			return (revents() & event.value) != 0;
		}

		public Set<Event> responses() {
			return Event.xcoder.decodeAll(revents());
		}

		public boolean has(Error error) {
			return (revents() & error.value) != 0;
		}

		public Set<Error> errors() {
			return Error.xcoder.decodeAll(revents());
		}

		public Fd validate() throws IOException {
			var errors = errors();
			if (errors.isEmpty()) return this;
			throw new IOException("Poll errors [" + index + "] = " + errors);
		}

		public int revents() {
			return MathUtil.ushort(pollfd().revents);
		}

		private CPoll.pollfd pollfd() {
			return pollfds[index];
		}
	}

	public static Poll of(int count) {
		return new Poll(CPoll.pollfd.array(count));
	}

	private Poll(CPoll.pollfd[] pollfds) {
		this.pollfds = pollfds;
		fds = IntStream.range(0, pollfds.length).mapToObj(Fd::new).toList();
	}

	public int poll() throws IOException {
		return poll(-1);
	}

	public int poll(int timeoutMs) throws IOException {
		return CPoll.poll(pollfds, timeoutMs);
	}

	public List<Fd> fds() {
		return fds;
	}

	public Set<Event> responses() {
		return StreamUtil.toSet(fds().stream().flatMap(fd -> fd.responses().stream()));
	}

	public Set<Error> errors() {
		return StreamUtil.toSet(fds().stream().flatMap(fd -> fd.errors().stream()));
	}

	public void validate() throws IOException {
		for (var fd : this)
			fd.validate();
	}

	@Override
	public Iterator<Fd> iterator() {
		return fds.iterator();
	}

	public Fd fd(int i) {
		return fds.get(i);
	}

	public int size() {
		return fds.size();
	}
}
