package ceri.jna.clib.util;

import static ceri.jna.clib.Poll.Event.POLLIN;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.IoUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.Pipe;
import ceri.jna.clib.Poll;
import ceri.log.util.LogUtil;

/**
 * A pipe used to interrupt a blocking C poll(). Can be applied to multiple poll fds.
 */
public class SyncPipe implements RuntimeCloseable {
	private final AtomicBoolean sync = new AtomicBoolean();
	private final Pipe pipe;
	private volatile boolean closed;

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
		 * Write a token to the pipe to interrupt any waiting poll.
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
		public void clear() throws IOException {
			pipe.clear();
		}

		@Override
		public void close() {
			pipe.close();
		}
	}

	private SyncPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	/**
	 * Write a token to the pipe to interrupt any waiting poll.
	 */
	public boolean signal() throws IOException {
		if (sync.getAndSet(true)) return false; // signal already active
		if (closed) return false;
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
	@SuppressWarnings("resource")
	public void clear() throws IOException {
		if (!closed) IoUtil.clear(pipe.in());
		sync.set(false);
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		CloseableUtil.close(this::write); // ignore failure
		LogUtil.close(pipe); // log failure
	}

	@SuppressWarnings("resource")
	private boolean write() throws IOException {
		if (pipe.in().available() > 0) return false;
		pipe.out().write(0);
		return true;
	}
}
