package ceri.jna.clib.util;

import static ceri.jna.clib.jna.CTermios.ECHO;
import static ceri.jna.clib.jna.CTermios.ECHONL;
import static ceri.jna.clib.jna.CTermios.ICANON;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.function.Enclosure;
import ceri.common.function.Excepts.Consumer;
import ceri.common.io.LineReader;
import ceri.common.test.Assert;
import ceri.common.test.SystemIoCaptor;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaLibrary;

public class TtyInputBehavior {
	private static final int LFLAGS = ICANON | ECHO | ECHONL;
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private CTermios.termios termios = null;
	private SystemIoCaptor sys = null;
	private Enclosure<? extends LineReader> ttyRef = null;
	private LineReader tty = null;

	@After
	public void after() {
		Closeables.close(ttyRef, sys, ref);
		termios = null;
		sys = null;
		ttyRef = null;
		tty = null;
	}

	@Test
	public void shouldProvideStandardInputIfNotTty() throws IOException {
		ref.init();
		sys = SystemIoCaptor.of();
		ttyRef = TtyInput.in();
		sys.in.print("abc\n");
		Assert.equal(ttyRef.ref.readLine(), "abc");
	}

	@Test
	public void shouldModifyTermios() throws IOException {
		var lib = initTty();
		termios(lib.tc.value(), t -> Assert.equal(t.c_lflag.intValue() & ~LFLAGS, 0));
		sys.in.print("abc\n");
		Assert.equal(tty.ready(), true);
		Assert.equal(tty.readLine(), "abc");
		ttyRef.close();
		termios(lib.tc.value(), t -> Assert.equal(t.c_lflag.intValue() & LFLAGS, LFLAGS));
	}

	private void termios(TcArgs tc, Consumer<IOException, CTermios.termios> consumer)
		throws IOException {
		CTermios.termios termios = CTermios.tcgetattr(0);
		Struct.copyTo(termios, tc.arg(1));
		consumer.accept(termios);
	}

	private static void handleTc(TcArgs args, CTermios.termios termios) {
		if (args.name().equals("tcgetattr")) Struct.copyTo(termios, args.arg(0));
		if (args.name().equals("tcsetattr")) Struct.copyFrom(args.arg(1), termios);
	}

	private TestCLibNative initTty() throws IOException {
		var lib = ref.init();
		lib.isatty.autoResponses(1);
		termios = CTermios.tcgetattr(0);
		termios.c_lflag.setValue(LFLAGS);
		lib.tc.autoResponse(args -> handleTc(args, termios), 0);
		sys = SystemIoCaptor.of();
		ttyRef = TtyInput.in();
		tty = ttyRef.ref;
		return lib;
	}
}
