package ceri.jna.clib.util;

import static ceri.jna.clib.Poll.Event.POLLIN;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import ceri.common.function.FunctionUtil;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.IoUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Pipe;
import ceri.jna.clib.Poll;
import ceri.log.util.LogUtil;

/**
 * A pipe used to interrupt a blocking C poll(). Can be applied to multiple poll fds.
 */
public class SyncPipe implements RuntimeCloseable {
	private final AtomicBoolean sync = new AtomicBoolean();
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final Pipe pipe;

	/**
	 * Creates a sync pipe that can be used for multiple poll fds.
	 */
	@SuppressWarnings("resource")
	public static SyncPipe of() throws IOException {
		return LogUtil.applyOrClose(Pipe.of(), pipe -> {
			pipe.blocking(false);
			return new SyncPipe(pipe);
		});
	}

	/**
	 * Creates a sync pipe fixed to the poll fd.
	 */
	@SuppressWarnings("resource")
	public static SyncPipe.Fixed of(Poll.Fd pollFd) throws IOException {
		return LogUtil.applyOrClose(of(), sync -> {
			sync.init(pollFd);
			return new Fixed(sync, pollFd);
		});
	}

	public static SyncPipe.Fd fd(FileDescriptor fd, Poll.Event... events) throws IOException {
		return new Fd(fd, events);
	}

	/**
	 * A pipe used for a single poll fd.
	 */
	public static class Fixed implements RuntimeCloseable {
		private final SyncPipe pipe;
		private final Poll.Fd pollFd;

		private Fixed(SyncPipe pipe, Poll.Fd pollFd) {
			this.pipe = pipe;
			this.pollFd = pollFd;
		}

		/**
		 * Write a token to the pipe to interrupt any waiting poll. Returns false if the pipe has
		 * already been interrupted.
		 */
		public boolean signal() throws IOException {
			return pipe.signal();
		}

		/**
		 * Throws an exception for any error event on the read fd, returns true on POLLIN.
		 */
		public boolean verifyPoll() throws IOException {
			return pollFd.validate().has(POLLIN);
		}

		/**
		 * Clear any tokens written to the pipe.
		 */
		public void clear() {
			pipe.clear();
		}

		@Override
		public void close() {
			pipe.close();
		}

		private boolean closed() {
			return pipe.closed();
		}
	}

	/**
	 * A pipe for monitoring a single file descriptor.
	 */
	public static class Fd implements RuntimeCloseable {
		public final FileDescriptor fd;
		private final SyncPipe.Fixed sync;
		private final Poll poll;

		private Fd(FileDescriptor fd, Poll.Event... events) throws IOException {
			this.fd = fd;
			poll = Poll.of(2);
			poll.fd(0).fd(fd).request(events);
			sync = SyncPipe.of(poll.fd(1));
		}

		/**
		 * Interrupt polling. Returns false if the polling has already been interrupted.
		 */
		public boolean signal() throws IOException {
			return sync.signal();
		}

		/**
		 * Wait for fd events or sync signal. Returns true if the fd has events.
		 */
		public boolean poll(Integer timeoutMs) throws IOException {
			if (sync.closed()) return false;
			poll.poll(timeoutMs);
			poll.fd(0).validate();
			sync.clear();
			return poll.fd(0).revents() != 0;
		}

		/**
		 * Wait for fd events or sync signal. Returns true if the fd has events.
		 */
		public boolean poll() throws IOException {
			return poll(null);
		}

		@Override
		public void close() {
			sync.close();
		}
	}

	private SyncPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	/**
	 * Write a token to the pipe to interrupt any waiting poll. Returns false if a token is already
	 * present.
	 */
	public boolean signal() throws IOException {
		if (sync.getAndSet(true) || closed.get()) return false; // signal already active
		try {
			return write();
		} catch (RuntimeException | IOException e) {
			sync.set(false);
			throw e;
		}
	}

	/**
	 * Initialize a poll fd with the pip's read fd and POLLIN.
	 */
	public void init(Poll.Fd pollFd) throws IOException {
		pollFd.fd(pipe.read).request(POLLIN);
	}

	/**
	 * Clear any tokens written to the pipe.
	 */
	public void clear() {
		if (!closed.get()) FunctionUtil.runSilently(() -> IoUtil.clear(pipe.in()));
		sync.set(false);
	}

	@Override
	public void close() {
		if (closed.getAndSet(true)) return;
		CloseableUtil.close(this::write); // ignore failure
		LogUtil.close(pipe); // log failure
	}

	private boolean closed() {
		return closed.get();
	}

	@SuppressWarnings("resource")
	private boolean write() throws IOException {
		if (pipe.in().available() > 0) return false;
		pipe.out().write(0);
		return true;
	}
}
