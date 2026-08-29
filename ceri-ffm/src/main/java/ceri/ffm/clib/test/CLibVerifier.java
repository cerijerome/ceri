package ceri.ffm.clib.test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import ceri.common.collect.Lists;
import ceri.common.io.PathList;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.common.text.Strings;
import ceri.common.util.Os;
import ceri.common.util.StartupValues;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CIoctl;
import ceri.ffm.clib.ffm.CPoll;
import ceri.ffm.clib.ffm.CSignal;
import ceri.ffm.clib.ffm.CStdlib;
import ceri.ffm.clib.ffm.CTermios;
import ceri.ffm.clib.ffm.CUnistd;
import ceri.ffm.type.Pointer;

/**
 * CLib verification logic to run on a target system.
 */
public class CLibVerifier {
	private static final Path DEV_DIR = Path.of("/dev");
	private static final String USB_PATTERN = "regex:tty.*(usb|USB).*";

	private CLibVerifier() {}

	public static void main(String[] args) throws IOException {
		var serial = StartupValues.of(args).next("serial", p -> p.get());
		// LogModifier.run(() -> verifyAll(System.out, serial), Level.OFF, JnaLibrary.class);
		verifyAll(System.out, serial);
	}

	public static void verifyAll(PrintStream out, String serial) throws IOException {
		out.println("CLib FFM system check");
		out.println(Os.info().full());
		verifyFile();
		verifySignal();
		verifySigset();
		verifyPoll();
		if (!verifyTermios(serial)) out.println("INFO: serial not found, skipping CTermios");
		verifyEnv();
		out.println("Success");
	}

	public static void verifyFile() throws IOException {
		int fd = -1;
		try (var files = FileTestHelper.builder().build()) {
			fd = CFcntl.open(files.path("test"), 0777, CFcntl.Open.O_RDWR, CFcntl.Open.O_CREAT);
			Assert.equal(CUnistd.isatty(fd), false);
			Assert.equal(CUnistd.write(fd, 1, 2, 3), 3);
			Assert.equal(CUnistd.lseek(fd, 0, CUnistd.Seek.SEEK_SET), 0L);
			Assert.equal(CIoctl.fionread(fd), 3);
			var bytes = new byte[3];
			Assert.equal(CUnistd.readAll(fd, bytes), 3);
			Assert.array(bytes, 1, 2, 3);
			verifyFileFlags(fd);
		} finally {
			CUnistd.close(fd);
		}
	}

	public static void verifySignal() throws IOException {
		var sync = CallSync.consumer(0, true);
		CSignal.SIGUSR1.set(sync::accept);
		CSignal.SIGUSR1.raise();
		sync.assertAuto(CSignal.SIGUSR1.value);
		CSignal.SIGUSR1.setIgnore();
		Assert.equal(CSignal.SIGUSR1.setDefault().macro(), CSignal.Macro.SIG_IGN);
		verifySigset();
	}

	public static void verifyPoll() throws IOException {
		int[] fds = CUnistd.pipe();
		try {
			var pollfds = CPoll.pollfd.$.initArray(2);
			pollfds[0].fd = fds[0];
			pollfds[0].events = CPoll.POLLIN;
			pollfds[1].fd = fds[1];
			pollfds[1].events = CPoll.POLLOUT;
			CUnistd.write(fds[1], 0);
			Assert.equal(CPoll.poll(1000, pollfds), 2);
			Assert.equal(CPoll.poll(-1, pollfds), 2);
			CUnistd.readAll(fds[0], new byte[1]);
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
		Assert.equal(CStdlib.getenv("CLIBVERIFIER"), "VALUE");
	}

	private static void verifyFileFlags(int fd) throws IOException {
		Assert.equal(CFcntl.getFd(fd), 0);
		CFcntl.setFl(fd, CFcntl.Open.mask(CFcntl.Open.O_NONBLOCK).mask());
		var flags = CFcntl.Open.mask(CFcntl.getFl(fd)).flags().types();
		Assert.containsAll(flags, CFcntl.Open.O_RDWR, CFcntl.Open.O_NONBLOCK);
	}

	private static void verifySigset() throws IOException {
		var sigset = CSignal.sigset();
		for (var signal : CSignal.values()) {
			signal.add(sigset);
			Assert.equal(signal.isMember(sigset), true);
			signal.delete(sigset);
			Assert.equal(signal.isMember(sigset), false);
		}
	}

	private static void verifySerial(int fd) throws IOException {
		CFcntl.setFl(fd, CFcntl.getFl(fd) & ~CFcntl.Open.O_NONBLOCK.value);
		var tty = CTermios.tcgetattr(fd);
		initSerialTermios(tty);
		initSerialSpeed(fd, tty);
		CTermios.tcflow(fd, CTermios.TCOON);
		CTermios.tcdrain(fd);
		CTermios.tcflush(fd, CTermios.TCIOFLUSH);
		CTermios.tcsendbreak(fd, 0);
	}

	private static void initSerialTermios(Pointer<? extends CTermios.termios<?>> tty)
		throws IOException {
		CTermios.cfmakeraw(tty);
		var t = tty.get();
		t.c_iflag = new CTermios.tcflag_t(
			t.c_iflag.value() & ~(CTermios.IXANY | CTermios.IXOFF | CTermios.IXON));
		t.c_cflag = new CTermios.tcflag_t(t.c_cflag.value()
			& ~(CTermios.CSIZE | CTermios.CSTOPB | CTermios.PARENB | CTermios.CMSPAR
				| CTermios.PARODD | CTermios.CRTSCTS)
			| CTermios.CLOCAL | CTermios.CREAD | CTermios.CS8);
		t.c_cc[CTermios.VSTART] = 0x11; // DC1
		t.c_cc[CTermios.VSTOP] = 0x13; // DC3
		t.c_cc[CTermios.VMIN] = 0; // no min bytes for read
		t.c_cc[CTermios.VTIME] = 0; // no timeout for read
		tty.write(Reflect.unchecked(t));
	}

	private static void initSerialSpeed(int fd, Pointer<? extends CTermios.termios<?>> tty)
		throws IOException {
		CTermios.cfsetispeed(tty, CTermios.B9600);
		CTermios.cfsetospeed(tty, CTermios.B9600);
		CTermios.tcsetattr(fd, CTermios.TCSANOW, tty);
		Assert.equal(CTermios.cfgetispeed(tty), CTermios.B9600);
		Assert.equal(CTermios.cfgetospeed(tty), CTermios.B9600);
	}

	private static Integer openSerial(String serial) throws IOException {
		var path = serialPath(serial);
		if (path == null) return null;
		try {
			return CFcntl.open(path, CFcntl.Open.O_RDWR, CFcntl.Open.O_NOCTTY,
				CFcntl.Open.O_NONBLOCK);
		} catch (IOException e) {
			return null;
		}
	}

	private static Path serialPath(String path) throws IOException {
		if (!Strings.isBlank(path)) return Path.of(path);
		return Lists.at(PathList.of(DEV_DIR).nameFilter(USB_PATTERN).sort().list(), 0);
	}
}
