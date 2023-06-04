package ceri.jna.clib.util;

import static ceri.jna.clib.jna.CFcntl.O_NONBLOCK;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.log.util.LogUtil;

/**
 * A pipe used to interrupt a blocking C poll().
 */
public class SyncPipe implements Closeable {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int BUFFER_SIZE = 3;
	private final AtomicBoolean sync = new AtomicBoolean();
	private final Memory buffer = new Memory(BUFFER_SIZE);
	private final int readFd;
	private final int writeFd;

	public static SyncPipe of() throws IOException {
		return new SyncPipe();
	}

	private SyncPipe() throws IOException {
		int[] fds = CUnistd.pipe();
		readFd = fds[0];
		writeFd = fds[1];
		try {
			CFcntl.setFl(readFd, flags -> flags | O_NONBLOCK);
			CFcntl.setFl(writeFd, flags -> flags | O_NONBLOCK);
		} catch (RuntimeException | IOException e) {
			close();
			throw e;
		}
	}

	/**
	 * Initialize the poll structure for the pipe read fd.
	 */
	public void init(CPoll.pollfd pollfd) {
		pollfd.fd = readFd;
		pollfd.events = CPoll.POLLIN;
	}

	/**
	 * Write a token to the pipe to interrupt any waiting poll.
	 */
	public void signal() throws IOException {
		if (!sync.getAndSet(true)) try { // only write once
			CUnistd.write(writeFd, buffer, 1);
		} catch (RuntimeException | IOException e) {
			sync.set(false); // failed to write, clear the flag
			throw e;
		}
	}

	/**
	 * Clear any tokens written to the pipe.
	 */
	public void clear() throws IOException {
		CUnistd.read(readFd, buffer, JnaUtil.intSize(buffer));
		sync.set(false);
	}

	@Override
	public void close() {
		LogUtil.close(logger, () -> CUnistd.close(readFd), () -> CUnistd.close(writeFd), buffer);
	}

}
