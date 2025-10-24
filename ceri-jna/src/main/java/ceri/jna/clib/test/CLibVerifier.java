package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import org.apache.logging.log4j.Level;
import ceri.common.collect.Lists;
import ceri.common.io.PathList;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.common.text.Strings;
import ceri.common.time.TimeSpec;
import ceri.common.util.OsUtil;
import ceri.common.util.StartupValues;
import ceri.jna.clib.Signal;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.jna.CStdlib;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTime;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaLibrary;
import ceri.log.test.LogModifier;

/**
 * CLib verification logic to run on a target system.
 */
public class CLibVerifier {
	private static final Path DEV_DIR = Path.of("/dev");
	private static final String USB_PATTERN = "regex:tty.*(usb|USB).*";

	private CLibVerifier() {}

	public static void main(String[] args) throws IOException {
		var serial = StartupValues.of(args).next("serial", p -> p.get());
		LogModifier.run(() -> verifyAll(System.out, serial), Level.OFF, JnaLibrary.class);
	}

	public static void verifyAll(PrintStream out, String serial) throws IOException {
		out.println("CLib JNA system check");
		out.println(OsUtil.os().full());
		verifyFile();
		verifySignal();
		verifySigset();
		verifyPoll();
		if (!verifyTermios(serial)) out.println("WARNING: serial not found, skipping CTermios");
		verifyEnv();
		out.println("Success");
	}

	public static void verifyFile() throws IOException {
		int fd = -1;
		try (var files = FileTestHelper.builder().build()) {
			fd = CFcntl.open(files.path("test").toString(), CFcntl.O_RDWR | CFcntl.O_CREAT, 0777);
			assertEquals(CUnistd.isatty(fd), false);
			assertEquals(CUnistd.write(fd, 1, 2, 3), 3);
			assertEquals(CUnistd.lseek(fd, 0, CUnistd.SEEK_SET), 0);
			assertEquals(CIoctl.fionread(fd), 3);
			var bytes = new byte[3];
			assertEquals(CUnistd.read(fd, bytes), 3);
			assertArray(bytes, 1, 2, 3);
			verifyFileFlags(fd);
		} finally {
			CUnistd.close(fd);
		}
	}

	public static void verifySignal() throws IOException {
		var sync = CallSync.consumer(0, true);
		assertEquals(CSignal.signal(CSignal.SIGUSR1, sync::accept), true);
		CSignal.raise(CSignal.SIGUSR1);
		sync.assertAuto(CSignal.SIGUSR1);
		assertEquals(CSignal.signal(CSignal.SIGUSR1, CSignal.SIG_DFL), true);
		verifySigset();
	}

	public static void verifyPoll() throws IOException {
		int[] fds = CUnistd.pipe();
		try {
			var pollfds = CPoll.pollfd.array(2);
			pollfds[0].fd = fds[0];
			pollfds[0].events = CPoll.POLLIN;
			pollfds[1].fd = fds[1];
			pollfds[1].events = CPoll.POLLOUT;
			CUnistd.write(fds[1], 0);
			assertEquals(CPoll.poll(pollfds, 1000), 2);
			assertEquals(CPoll.poll(pollfds, -1), 2);
			if (OsUtil.os().linux) verifyPpoll(pollfds);
			CUnistd.read(fds[0], new byte[1]);
		} finally {
			CUnistd.close(fds[0]);
			CUnistd.close(fds[1]);
		}
	}

	public static boolean verifyTermios(String serial) throws IOException {
		var fd = openSerial(serial);
		if (fd == null) return false;
		try {
			verifySerial(fd);
			return true;
		} finally {
			CUnistd.close(fd);
		}
	}

	public static void verifyEnv() throws IOException {
		CStdlib.setenv("CLIBVERIFIER", "VALUE", true);
		assertEquals(CStdlib.getenv("CLIBVERIFIER"), "VALUE");
	}

	private static void verifyFileFlags(int fd) throws IOException {
		assertEquals(CFcntl.getFd(fd), 0);
		CFcntl.setFl(fd, CFcntl.O_NONBLOCK);
		int fl = CFcntl.getFl(fd);
		assertEquals(fl & CFcntl.O_RDWR, CFcntl.O_RDWR);
		assertEquals(fl & CFcntl.O_NONBLOCK, CFcntl.O_NONBLOCK);
	}

	private static void verifyPpoll(CPoll.pollfd[] pollfds) throws IOException {
		var tmo = new CTime.timespec().time(TimeSpec.ofMillis(1, 0));
		var sigset = CSignal.sigemptyset(new CSignal.sigset_t());
		CSignal.sigaddset(sigset, CSignal.SIGINT);
		assertEquals(CPoll.Linux.ppoll(pollfds, null, null), 2);
		assertEquals(CPoll.Linux.ppoll(pollfds, tmo, null), 2);
		assertEquals(CPoll.Linux.ppoll(pollfds, null, sigset), 2);
		assertEquals(CPoll.Linux.ppoll(pollfds, tmo, sigset), 2);
	}

	private static void verifySigset() throws IOException {
		var sigset = CSignal.sigemptyset(new CSignal.sigset_t());
		for (var signal : Signal.values()) {
			int signum = signal.signal;
			CSignal.sigaddset(sigset, signum);
			assertEquals(CSignal.sigismember(sigset, signum), true);
			CSignal.sigdelset(sigset, signum);
			assertEquals(CSignal.sigismember(sigset, signum), false);
		}
	}

	private static void verifySerial(int fd) throws IOException {
		CFcntl.setFl(fd, flags -> flags & ~CFcntl.O_NONBLOCK);
		var tty = CTermios.tcgetattr(fd);
		initSerialTermios(tty);
		initSerialSpeed(fd, tty);
		CTermios.tcflow(fd, CTermios.TCOON);
		CTermios.tcdrain(fd);
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		CTermios.tcsendbreak(fd, 0);
	}

	private static void initSerialTermios(CTermios.termios tty) throws IOException {
		CTermios.cfmakeraw(tty);
		tty.c_iflag
			.setValue(tty.c_iflag.longValue() & ~(CTermios.IXANY | CTermios.IXOFF | CTermios.IXON));
		tty.c_cflag.setValue((tty.c_cflag.longValue() & ~(CTermios.CSIZE | CTermios.CSTOPB
			| CTermios.PARENB | CTermios.CMSPAR | CTermios.PARODD | CTermios.CRTSCTS))
			| CTermios.CLOCAL | CTermios.CREAD | CTermios.CS8);
		tty.c_cc[CTermios.VSTART] = 0x11; // DC1
		tty.c_cc[CTermios.VSTOP] = 0x13; // DC3
		tty.c_cc[CTermios.VMIN] = 0; // no min bytes for read
		tty.c_cc[CTermios.VTIME] = 0; // no timeout for read
		Struct.write(tty);
	}

	private static void initSerialSpeed(int fd, CTermios.termios tty) throws IOException {
		CTermios.cfsetispeed(tty, CTermios.B9600);
		CTermios.cfsetospeed(tty, CTermios.B9600);
		CTermios.tcsetattr(fd, CTermios.TCSANOW, tty);
		assertEquals(CTermios.cfgetispeed(tty), CTermios.B9600);
		assertEquals(CTermios.cfgetospeed(tty), CTermios.B9600);
	}

	private static Integer openSerial(String serial) throws IOException {
		var path = serialPath(serial);
		if (path == null) return null;
		try {
			return CFcntl.open(path.toString(),
				CFcntl.O_RDWR | CFcntl.O_NOCTTY | CFcntl.O_NONBLOCK);
		} catch (IOException e) {
			return null;
		}
	}

	private static Path serialPath(String path) throws IOException {
		if (!Strings.isBlank(path)) return Path.of(path);
		return Lists.at(PathList.of(DEV_DIR).nameFilter(USB_PATTERN).sort().list(), 0);
	}
}
