package ceri.jna.clib.util;

import static ceri.common.validation.ValidationUtil.validateEqual;
import static ceri.jna.clib.jna.CFcntl.O_NONBLOCK;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
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
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int BUFFER_SIZE = 3;
	private final AtomicBoolean sync = new AtomicBoolean();
	private final Memory buffer = new Memory(BUFFER_SIZE);
	private final int readFd;
	private final int writeFd;
	private volatile boolean closed;

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
		LogUtil.close(logger, this::write, () -> CUnistd.closeSilently(readFd, writeFd));
	}

	private boolean write() throws IOException {
		return CUnistd.write(writeFd, buffer, 1) > 0;
	}
}
