package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.Direction;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.util.Struct;

public class TermiosBehavior {
	private TestCLibNative lib = null;
	private Enclosed<RuntimeException, ?> enc = null;
	private CFileDescriptor fd = null;
	private CTermios.termios termios = null;
	private Termios tm = null;

	@After
	public void after() {
		CloseableUtil.close(fd, enc);
		enc = null;
		lib = null;
		fd = null;
		termios = null;
		tm = null;
	}

	@Test
	public void shouldAccessFields() throws IOException {
		init();
		termios.c_cflag.setValue(0xffff);
		tm = Termios.get(fd);
		tm.inFlags().setValue(0x111);
		tm.outFlags().or(0x222);
		tm.controlFlags().and(0x333);
		tm.localFlags().andOr(-1, 0x444);
		tm.controlChar(3, 0x55);
		assertEquals(tm.inFlags().intValue(), 0x111);
		assertEquals(tm.outFlags().intValue(), 0x222);
		assertEquals(tm.controlFlags().intValue(), 0x333);
		assertEquals(tm.localFlags().intValue(), 0x444);
		assertEquals(tm.controlChar(3), 0x55);
	}

	@Test
	public void shouldAccessSpeeds() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.inSpeed(11111);
		tm.outSpeed(22222);
		assertEquals(tm.inSpeed(), 11111);
		assertEquals(tm.outSpeed(), 22222);
	}

	@Test
	public void shouldMakeRaw() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.makeRaw();
		assertEquals(lib.cf.awaitAuto().name(), "cfmakeraw");
	}

	@Test
	public void shouldSendBreak() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.sendBreak(111);
		lib.tc.assertAuto(TcArgs.of("tcsendbreak", fd.fd(), 111));
	}

	@Test
	public void shouldDrain() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.drain();
		lib.tc.assertAuto(TcArgs.of("tcdrain", fd.fd()));
	}

	@Test
	public void shouldFlush() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.flush(Direction.none); // does nothing
		tm.flush(Direction.in);
		lib.tc.assertAuto(TcArgs.of("tcflush", fd.fd(), CTermios.TCIFLUSH));
		tm.flush(Direction.out);
		lib.tc.assertAuto(TcArgs.of("tcflush", fd.fd(), CTermios.TCOFLUSH));
		tm.flush(Direction.duplex);
		lib.tc.assertAuto(TcArgs.of("tcflush", fd.fd(), CTermios.TCIOFLUSH));
	}

	@Test
	public void shouldFlow() throws IOException {
		init();
		tm = Termios.get(fd);
		tm.flow(Direction.none, true); // does nothing
		tm.flow(Direction.in, false);
		lib.tc.assertAuto(TcArgs.of("tcflow", fd.fd(), CTermios.TCIOFF));
		tm.flow(Direction.out, false);
		lib.tc.assertAuto(TcArgs.of("tcflow", fd.fd(), CTermios.TCOOFF));
		lib.tc.clearCalls();
		tm.flow(Direction.duplex, true);
		lib.tc.assertValues(TcArgs.of("tcflow", fd.fd(), CTermios.TCION),
			TcArgs.of("tcflow", fd.fd(), CTermios.TCOON));
	}

	private void init() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = CFileDescriptor.open("test", Open.RDWR);
		termios = CTermios.tcgetattr(fd.fd());
		lib.cf.autoResponse(args -> handleCfSpeed(args, termios));
		lib.tc.autoResponse(args -> handleTc(args, termios), 0);
	}

	private static <T extends CTermios.termios> void handleTc(TcArgs args, T termios) {
		if (args.name().equals("tcgetattr")) Struct.copyTo(termios, args.arg(0));
		if (args.name().equals("tcsetattr")) Struct.copyFrom(args.arg(1), termios);
	}

	private static <T extends CTermios.termios> int handleCfSpeed(CfArgs args, T termios) {
		Struct.copyFrom(args.termios(), termios);
		if (args.name().equals("cfgetispeed")) return termios.c_ispeed.intValue();
		if (args.name().equals("cfgetospeed")) return termios.c_ospeed.intValue();
		if (args.name().equals("cfsetispeed")) termios.c_ispeed.setValue(args.<Integer>arg(0));
		if (args.name().equals("cfsetospeed")) termios.c_ospeed.setValue(args.<Integer>arg(0));
		Struct.copyTo(termios, args.termios());
		return 0;
	}
}
