package ceri.jna.clib.util;

import static ceri.jna.clib.Poll.Event.POLLIN;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.IoUtil;
import ceri.jna.clib.Pipe;
import ceri.jna.clib.Poll;
import ceri.log.util.LogUtil;

/**
 * A pipe used to interrupt a blocking C poll().
 */
public class SyncPipe implements RuntimeCloseable {
	private final AtomicBoolean sync = new AtomicBoolean();
	private final Pipe pipe;
	private final Poll.Fd pollFd;
	private volatile boolean closed;

	@SuppressWarnings("resource")
	public static SyncPipe of(Poll.Fd pollFd) throws IOException {
		return LogUtil.applyOrClose(Pipe.of(), pipe -> {
			pipe.blocking(false);
			pollFd.fd(pipe.read).request(POLLIN);
			return new SyncPipe(pipe, pollFd);
		});
	}

	private SyncPipe(Pipe pipe, Poll.Fd pollFd) {
		this.pipe = pipe;
		this.pollFd = pollFd;
	}

	/**
	 * Throws an exception for any error event on the read fd, returns true on POLLIN.
	 */
	public boolean verifyPoll() throws IOException {
		return pollFd.validate().has(POLLIN);
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
		LogUtil.close(this::write, pipe);
	}

	@SuppressWarnings("resource")
	private boolean write() throws IOException {
		if (pipe.in().available() > 0) return false;
		pipe.out().write(0);
		return true;
	}
}
