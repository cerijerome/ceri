package ceri.jna.clib.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.jna.clib.jna.CTermios.ECHO;
import static ceri.jna.clib.jna.CTermios.ECHONL;
import static ceri.jna.clib.jna.CTermios.ICANON;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.ExceptionConsumer;
import ceri.common.io.LineReader;
import ceri.common.test.SystemIoCaptor;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.util.Struct;

public class TtyInputBehavior {
	private static final int LFLAGS = ICANON | ECHO | ECHONL;
	private TestCLibNative lib = null;
	private Enclosed<RuntimeException, ?> enc = null;
	private CTermios.termios termios = null;
	private SystemIoCaptor sys = null;
	private Enclosed<RuntimeException, ? extends LineReader> ttyRef = null;
	private LineReader tty = null;

	@After
	public void after() {
		CloseableUtil.close(ttyRef, sys, enc);
		enc = null;
		lib = null;
		termios = null;
		sys = null;
		ttyRef = null;
		tty = null;
	}

	@Test
	public void shouldProvideStandardInputIfNotTty() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		sys = SystemIoCaptor.of();
		ttyRef = TtyInput.in();
		sys.in.print("abc\n");
		assertEquals(ttyRef.ref.readLine(), "abc");
	}

	@Test
	public void shouldModifyTermios() throws IOException {
		init();
		termios(lib.tc.value(), t -> assertEquals(t.c_lflag.intValue() & ~LFLAGS, 0));
		sys.in.print("abc\n");
		assertEquals(tty.ready(), true);
		assertEquals(tty.readLine(), "abc");
		ttyRef.close();
		termios(lib.tc.value(), t -> assertEquals(t.c_lflag.intValue() & LFLAGS, LFLAGS));
	}

	private void termios(TcArgs tc, ExceptionConsumer<IOException, CTermios.termios> consumer)
		throws IOException {
		CTermios.termios termios = CTermios.tcgetattr(0);
		Struct.copyTo(termios, tc.arg(1));
		consumer.accept(termios);
	}

	private static void handleTc(TcArgs args, CTermios.termios termios) {
		if (args.name().equals("tcgetattr")) Struct.copyTo(termios, args.arg(0));
		if (args.name().equals("tcsetattr")) Struct.copyFrom(args.arg(1), termios);
	}

	private void init() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		lib.isatty.autoResponses(1);
		termios = CTermios.tcgetattr(0);
		termios.c_lflag.setValue(LFLAGS);
		lib.tc.autoResponse(args -> handleTc(args, termios), 0);
		sys = SystemIoCaptor.of();
		ttyRef = TtyInput.in();
		tty = ttyRef.ref;
	}
}
