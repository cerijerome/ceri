package ceri.serial.comm.jna;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.TestUtil;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.clib.util.SyncPipe;
import ceri.log.test.LogModifier;
import ceri.serial.comm.util.SerialTestUtil;

/**
 * Polls a serial port fd, and randomly signals the pipe or writes to the fd.
 */
public class SerialPollTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int CYCLES = 20;
	private static final int CYCLE_MS = 500;

	static {
		LogModifier.set(Level.INFO, SerialPollTester.class);
	}

	public static void main(String[] args) throws Exception {
		var port = SerialTestUtil.usbPorts(2);
		int baud = 250000;
		try (var poll = SyncPipe.poll(1);
			var p = SerialTestUtil.execFd(port[0], baud, fd -> runPoll(poll, fd));
			var w = SerialTestUtil.execFd(port[1], baud, fd -> runWrite(poll, fd))) {
			w.get();
			p.get();
		}
		logger.info("done");
	}

	private static void runPoll(SyncPipe.Poll poll, int fd) throws IOException {
		SerialTestUtil.clear(fd);
		poll.get(0).init(fd, CPoll.POLLIN);
		logger.info("poll: start");
		for (int i = 0; i < CYCLES; i++) {
			var n = poll.poll(10000);
			logger.info("poll: %s revents[0]=0x%x", n, poll.get(0).revents);
			SerialTestUtil.clear(fd);
		}
	}

	private static void runWrite(SyncPipe.Poll poll, int fd) throws IOException {
		for (int i = 0; i < CYCLES; i++) {
			ConcurrentUtil.delay(CYCLE_MS);
			if (TestUtil.randomBool()) {
				logger.info(">>> signal");
				poll.signal();
			} else {
				logger.info(">>> write");
				CUnistd.write(fd, 0);
			}
		}
	}
}
