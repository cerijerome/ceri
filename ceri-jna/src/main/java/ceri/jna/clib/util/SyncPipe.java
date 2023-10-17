package ceri.jna.clib.util;

import static ceri.common.collection.ArrayUtil.validateIndex;
import static ceri.common.validation.ValidationUtil.validateEqual;
import static ceri.jna.clib.jna.CFcntl.O_NONBLOCK;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sun.jna.Memory;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.RuntimeCloseable;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.log.util.LogUtil;

/**
 * A pipe used to interrupt a blocking C poll().
 */
public class SyncPipe implements RuntimeCloseable {
	private static final int BUFFER_SIZE = 3;
	private final AtomicBoolean sync = new AtomicBoolean();
	private final Memory buffer = new Memory(BUFFER_SIZE);
	private final int readFd;
	private final int writeFd;
	private volatile boolean closed;

	/**
	 * Encapsulation for polling with a sync pipe and 0+ file descriptors.
	 */
	public static class Poll implements RuntimeCloseable {
		private final SyncPipe pipe;
		private final CPoll.pollfd[] pollFds; // [0] used for pipe
		public final List<CPoll.pollfd> list;

		private Poll(int count) throws IOException {
			pollFds = CPoll.pollfd.array(count + 1);
			pipe = SyncPipe.of();
			pipe.init(pollFds[0]);
			list = ImmutableUtil.wrapAsList(pollFds, 1, pollFds.length);
		}

		/**
		 * Returns the number of fds.
		 */
		public int count() {
			return pollFds.length - 1;
		}

		/**
		 * Returns the poll data for fd at given index.
		 */
		public CPoll.pollfd get(int index) {
			validateIndex(count(), index);
			return pollFds[index + 1];
		}

		/**
		 * Poll fds for events, including sync pipe. Returns the number of fds with events,
		 * excluding the sync pipe.
		 */
		public int poll(int timeoutMs) throws IOException {
			return poll(timeoutMs, true);
		}

		/**
		 * Poll fds for events, including sync pipe. Returns the number of fds with events,
		 * excluding the sync pipe. Does not clear the sync pipe.
		 */
		public int pollPeek(int timeoutMs) throws IOException {
			return poll(timeoutMs, false);
		}

		/**
		 * Signal the sync pipe by writing a token.
		 */
		public void signal() throws IOException {
			pipe.signal();
		}

		/**
		 * Clear any sync pipe tokens.
		 */
		public void clear() throws IOException {
			pipe.clear(pollFds[0]);
		}

		@Override
		public void close() {
			pipe.close();
		}
		
		private int poll(int timeoutMs, boolean clear) throws IOException {
			if (pipe.closed) return 0; // do not poll if pipe is closed
			int n = CPoll.poll(pollFds, timeoutMs);
			if (n <= 0 || !pollFds[0].hasEvent()) return n;
			if (clear) clear();
			return n - 1;
		}
	}

	/**
	 * Create a polling array, with hidden entry for sync pipe.
	 */
	public static Poll poll(int count) throws IOException {
		return new Poll(count);
	}

	public static SyncPipe of() throws IOException {
		int[] fds = CUnistd.pipe();
		try {
			CFcntl.setFl(fds[0], flags -> flags | O_NONBLOCK);
			CFcntl.setFl(fds[1], flags -> flags | O_NONBLOCK);
			return new SyncPipe(fds[0], fds[1]);
		} catch (RuntimeException | IOException e) {
			CUnistd.closeSilently(fds);
			throw e;
		}
	}

	private SyncPipe(int readFd, int writeFd) {
		this.readFd = readFd;
		this.writeFd = writeFd;
	}

	/**
	 * Initialize the poll structure for the pipe read fd.
	 */
	public void init(CPoll.pollfd pollfd) {
		pollfd.init(readFd, CPoll.POLLIN);
	}

	/**
	 * Throws an exception for any error event on the read fd, returns true on POLLIN.
	 */
	public boolean verify(CPoll.pollfd pollfd) throws CException {
		validateEqual(pollfd.fd, readFd, "fd");
		return pollfd.verify().hasEvent(CPoll.POLLIN);
	}

	/**
	 * Write a token to the pipe to interrupt any waiting poll.
	 */
	public boolean signal() throws IOException {
		if (sync.getAndSet(true)) return true;
		if (closed) return false;
		try {
			return write();
		} catch (RuntimeException | IOException e) {
			sync.set(false);
			throw e;
		}
	}

	/**
	 * Clear any tokens written to the pipe.
	 */
	public void clear() throws IOException {
		if (!closed) CUnistd.read(readFd, buffer, JnaUtil.intSize(buffer));
		sync.set(false);
	}

	/**
	 * Clear any tokens written to the pipe only if a POLLIN event occurred.
	 */
	public void clear(CPoll.pollfd pollfd) throws IOException {
		validateEqual(pollfd.fd, readFd, "fd");
		if (pollfd.hasEvent(CPoll.POLLIN)) clear();
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		LogUtil.close(this::write, () -> CUnistd.closeSilently(readFd, writeFd));
	}

	private boolean write() throws IOException {
		return CUnistd.write(writeFd, buffer, 1) > 0;
	}
}
