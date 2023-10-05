package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNotFound;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.TestUtil.inputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.io.IoUtil;
import ceri.common.io.PipedStream;
import ceri.common.io.SystemIo;
import ceri.common.text.AnsiEscape.Sgr.BasicColor;

public class ManualTesterBehavior {

	@Test
	public void shouldParseMatcherBoolean() {
		assertEquals(ManualTester.Parse.b(matcher("x(.*)", "x")), null);
		assertEquals(ManualTester.Parse.b(matcher("x(.*)", "x1")), true);
		assertEquals(ManualTester.Parse.b(matcher("x(.*)", "xYES")), true);
		assertEquals(ManualTester.Parse.b(matcher("x(.*)", "xtrue")), true);
		assertEquals(ManualTester.Parse.b(matcher("x(.*)", "x0")), false);
		assertEquals(ManualTester.Parse.b(matcher("x(.)(.)", "x01"), 2), true);
	}

	@Test
	public void shouldParseMatcherChar() {
		assertEquals(ManualTester.Parse.c(matcher("x(.*)", "x")), null);
		assertEquals(ManualTester.Parse.c(matcher("x(.*)", "xxyz")), 'x');
		assertEquals(ManualTester.Parse.c(matcher("x(.)(.*)", "xxyz"), 2), 'y');
	}

	@Test
	public void shouldParseMatcherInt() {
		assertEquals(ManualTester.Parse.i(matcher("x(.*)", "x")), null);
		assertThrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x123x")));
		assertEquals(ManualTester.Parse.i(matcher("x(.*)", "x123")), 123);
		assertEquals(ManualTester.Parse.i(matcher("x(.)(.*)", "x123"), 2), 23);
	}

	@Test
	public void shouldParseMatcherDouble() {
		assertEquals(ManualTester.Parse.d(matcher("x(.*)", "x")), null);
		assertThrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x1.23x")));
		assertEquals(ManualTester.Parse.d(matcher("x(.*)", "x1.23")), 1.23);
		assertEquals(ManualTester.Parse.d(matcher("x(.)(.*)", "x1.23"), 2), .23);
	}

	@Test
	public void shouldFailIfNoSubject() {
		assertThrown(() -> ManualTester.builderList(List.of()));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteCommandsFromStdIn() {
		try (SystemIo stdIo = SystemIo.of()) {
			stdIo.in(inputStream("?;*;-;+;@0;:;~0;!\n"));
			stdIo.out(IoUtil.nullPrintStream());
			ManualTester.builderArray("test", 1).promptColor(null).build().run();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBuildAnInstance() {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(inputStream("!\n"));
			sys.out(IoUtil.nullPrintStream());
			ManualTester.builder("test", String::valueOf).in(System.in).out(System.out).delayMs(0)
				.err(System.err).indent("  ").promptColor(BasicColor.cyan)
				.preProcessor(String.class, (s, m) -> m.out(s)).preProcessor(m -> m.out("test"))
				.command(String.class, (s, i, m) -> true, "test").build().run();
		}
	}

	@Test
	public void shouldPrintErrors() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var m = ManualTester.builder("test").build();
			m.err("err-test");
			m.err(new IOException(), true);
			assertFind(sys.err, "err-test");
			assertFind(sys.err, "IOException");
		}
	}

	@Test
	public void shouldPrintBytesReadFromInputStream() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var m = ManualTester.builder("test").build();
			m.readBytes(inputStream());
			assertTrue(sys.out.isEmpty());
			m.readBytes(inputStream(1, -1, 0));
			assertFind(sys.out, "01 ff 00");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPrintBytesWrittenToOutputStream() throws IOException {
		try (var sys = SystemIoCaptor.of(); var pipe = PipedStream.of()) {
			var m = ManualTester.builder("test").build();
			m.writeAscii(pipe.out(), "");
			assertTrue(sys.out.isEmpty());
			assertEquals(pipe.in().available(), 0);
			m.writeAscii(pipe.out(), "ascii");
			assertFind(sys.out, "ascii");
			assertRead(pipe.in(), "ascii".getBytes());
		}
	}

	@Test
	public void shouldShowHelpForMatchingCommandsOnly() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var m = ManualTester.builderArray("test", 1)
				.command(Integer.class, "i", null, "int-cmd").build();
			sys.in.print("!\n"); // shows help for first subject
			m.run();
			assertNotFound(sys.out, "int-cmd");
			sys.in.print("+;?;!\n"); // move to next subject
			m.run();
			assertFind(sys.out, "int-cmd");
		}
	}

	@Test
	public void shouldExecuteCommandOnlyIfMatchingType() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var m = ManualTester.builderArray("test", 1)
				.command(Integer.class, "i", (x, i, t) -> t.out("int-cmd"), "i").build();
			sys.in.print("i\n!\n"); // invalid command
			m.run();
			assertNotFound(sys.out, "int-cmd");
			assertFind(sys.err, "Invalid");
			sys.in.print("+;i\n!\n"); // move to next subject first
			m.run();
			assertFind(sys.out, "int-cmd");
		}
	}

	@Test
	public void shouldPreProcessOnlyIfMatchingType() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var m = ManualTester.builderArray(null, 1)
				.preProcessor(Integer.class, (i, t) -> t.out("int-cmd")).build();
			sys.in.print(":;!\n");
			m.run();
			assertNotFound(sys.out, "int-cmd");
			sys.in.print("+\n!\n"); // move to next subject first
			m.run();
			assertFind(sys.out, "int-cmd");
		}
	}

	@Test
	public void shouldCaptureEvents() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var events = ManualTester.eventCatcher();
			events.add("event ok");
			events.execute(() -> {});
			events.execute(() -> throwIt(new Exception("event error")));
			sys.in.print("!\n");
			ManualTester.builder("test").preProcessor(events).build().run();
			assertFind(sys.out, "event ok");
			assertFind(sys.err, "event error");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldHandleInputIoErrors() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			var in = TestInputStream.of();
			in.to.writeAscii("\n");
			in.read.error.setFrom(IOX);
			var m = ManualTester.builder("test").in(in).build();
			m.run(); // exits on error
			assertFind(sys.err, "IOException");
		}
	}

	@Test
	public void shouldHandleCommandErrors() {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			ErrorGen error = ErrorGen.of();
			var m = ManualTester.builder(1).command(Integer.class, "x", (x, i, t) -> {
				error.callWithInterrupt();
			}, "x").build();
			error.setFrom(IOX, INX, RIX); // IO, Interrupted, then RuntimeInterrupted exceptions
			sys.in.print("x\nx\nx\n");
			assertThrown(m::run); // exits on InterruptedException
			assertFind(sys.err, "IOException");
			assertThrown(m::run); // exits on RuntimeInterruptedException
		}
	}

	private Matcher matcher(String pattern, String input) {
		var m = Pattern.compile(pattern).matcher(input);
		assertTrue(m.matches(), "Pattern \"%s\" does not match \"%s\"", pattern, input);
		return m;
	}
}
