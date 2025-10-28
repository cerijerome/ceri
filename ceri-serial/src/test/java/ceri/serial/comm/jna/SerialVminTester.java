package ceri.serial.comm.jna;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.Concurrent;
import ceri.common.function.Excepts;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CUnistd;
import ceri.log.test.LogModifier;
import ceri.serial.comm.util.SerialTestUtil;

/**
 * Testing VMIN and VTIME on serial ports for poll and fionread.
 */
public class SerialVminTester {
	private static final Logger logger = LogManager.getFormatterLogger();

	static {
		LogModifier.set(Level.INFO, SerialVminTester.class, SerialTestUtil.class);
	}

	public static void main(String[] args) throws Exception {
		test(250000, fd -> runAvailable(fd, 10, 0));
		test(250000, fd -> runPoll(fd, 10, 0));
	}

	public static void test(int baud, Excepts.IntConsumer<IOException> consumer) throws Exception {
		var ports = SerialTestUtil.usbPorts(2);
		try (var p = SerialTestUtil.execFd(ports[0], baud, consumer);
			var w = SerialTestUtil.execFd(ports[1], baud, fd -> runWrite(fd))) {
			w.get();
			p.get();
		}
		logger.info("done");
	}

	private static void runPoll(int fd, int vmin, int vtime) throws IOException {
		SerialTestUtil.clear(fd);
		var pfds = CPoll.pollfd.array(1);
		pfds[0].fd = fd;
		pfds[0].events = CPoll.POLLIN;
		CSerial.setReadParams(fd, vmin, vtime);
		Concurrent.delay(100);
		logger.info("poll: start");
		var b = CPoll.poll(pfds, 20000);
		logger.info("poll: %s revents=0x%x", b, pfds[0].revents);
	}

	private static void runAvailable(int fd, int vmin, int vtime) throws IOException {
		SerialTestUtil.clear(fd);
		CSerial.setReadParams(fd, vmin, vtime);
		Concurrent.delay(100);
		int n = 20;
		for (int i = 1; i <= n; i++) {
			Concurrent.delay(500);
			logger.info("available: %d", CIoctl.fionread(fd));
		}
		logger.info("available: done");
	}

	private static void runWrite(int fd) throws IOException {
		int n = 20;
		for (int i = 1; i <= n; i++) {
			Concurrent.delay(500);
			logger.info("write: %d/%d", i, n);
			CUnistd.write(fd, 0x77);
		}
		logger.info("write: done");
	}
}
