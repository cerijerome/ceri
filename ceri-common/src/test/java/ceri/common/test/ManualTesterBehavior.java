package ceri.common.test;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertMatch;
import static ceri.common.test.Assert.assertNotFound;
import static ceri.common.test.Assert.assertRead;
import static ceri.common.test.Assert.assertString;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.fail;
import static ceri.common.test.Assert.throwIt;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ManualTester.rt;
import static ceri.common.test.ManualTester.Parse.i;
import static ceri.common.test.TestUtil.inputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;
import ceri.common.io.IoStream;
import ceri.common.io.PipedStream;
import ceri.common.io.SystemIo;
import ceri.common.test.ManualTester.Action;
import ceri.common.test.ManualTester.Parse;
import ceri.common.text.AnsiEscape;
import ceri.common.text.AnsiEscape.Sgr.BasicColor;
import ceri.common.util.Counter;

public class ManualTesterBehavior {
	private Functions.Closeable fastMode;

	@Before
	public void before() {
		fastMode = ManualTester.fastMode();
	}

	@After
	public void after() {
		Closeables.close(fastMode);
	}

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
		Assert.thrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x123x")));
		assertEquals(ManualTester.Parse.i(matcher("x(.*)", "x123")), 123);
		assertEquals(ManualTester.Parse.i(matcher("x(.)(.*)", "x123"), 2), 23);
	}

	@Test
	public void shouldParseMatcherLong() {
		assertEquals(ManualTester.Parse.l(matcher("x(.*)", "x")), null);
		Assert.thrown(() -> ManualTester.Parse.l(matcher("x(.*)", "x123x")));
		assertEquals(ManualTester.Parse.l(matcher("x(.*)", "x123")), 123L);
		assertEquals(ManualTester.Parse.l(matcher("x(.)(.*)", "x123"), 2), 23L);
	}

	@Test
	public void shouldParseMatcherDouble() {
		assertEquals(ManualTester.Parse.d(matcher("x(.*)", "x")), null);
		Assert.thrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x1.23x")));
		assertEquals(ManualTester.Parse.d(matcher("x(.*)", "x1.23")), 1.23);
		assertEquals(ManualTester.Parse.d(matcher("x(.)(.*)", "x1.23"), 2), .23);
	}

	@Test
	public void shouldParseMatcherLen() {
		assertEquals(ManualTester.Parse.len(matcher("x(.*)", "x")), 0);
		assertEquals(ManualTester.Parse.len(matcher("x(.*)", "x123")), 3);
		assertEquals(ManualTester.Parse.len(matcher("x(.)(.*)", "x123"), 2), 2);
	}

	@Test
	public void shouldConsumeMatches() {
		ManualTester.Parse.consumeFirst(matcher("(?:(a)|(b)|(c))", "b"), (x, i) -> {
			assertEquals(x, "b");
			assertEquals(i, 2);
		});
		Assert.isNull(
			ManualTester.Parse.consumeFirst(matcher("(?:(a)|(b)|(c))", "a"), 2, (_, _) -> fail()));
	}

	@Test
	public void shouldApplyMatches() {
		assertEquals(
			ManualTester.Parse.applyFirst(matcher("(?:(a)|(b)|(c))", "b"), (x, i) -> x + i), "b2");
	}

	@Test
	public void shouldFailIfNoSubject() {
		Assert.thrown(() -> ManualTester.builderList(List.of()));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteCommandsFromStdIn() {
		try (var stdIo = SystemIo.of()) {
			stdIo.in(inputStream("?;*;-;+;@0;:;~0;!\n"));
			stdIo.out(IoStream.nullPrint());
			ManualTester.builderArray("test", 1).promptSgr(null).build().run();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBuildAnInstance() {
		fastMode.close();
		try (var sys = SystemIo.of()) {
			sys.in(inputStream("!\n"));
			sys.out(IoStream.nullPrint());
			ManualTester.builder("test", String::valueOf).in(System.in).out(System.out)
				.err(System.err).indent("  ")
				.promptSgr(AnsiEscape.csi.sgr().fgColor(BasicColor.cyan, false))
				.preProcessor(String.class, (m, s) -> m.out(s)).preProcessor(m -> m.out("test"))
				.listenTo().delayMs(0).errorDelayMs(0).build().run();
		}
	}

	@Test
	public void shouldProvideStringRepresentation() {
		try (var _ = SystemIoCaptor.of();
			var m = ManualTester.builderArray("test", "x").command("a", (_, _, _) -> {}, "a")
				.command("b", (_, _, _) -> {}, "b").command("c", (_, _, _) -> {}, "c").build()) {
			assertFind(m, "2,12");
		}
	}

	@Test
	public void shouldProvideCommandSeparators() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builder("test").separatorSgr(null)
			.separator("--test--").command(".*", (_, _, _) -> {}, "end-test").build()) {
			sys.in.print("x\n!\n");
			m.run();
			assertFind(sys.out, "(?s)--test--.*end-test");
		}
	}

	@Test
	public void shouldProvideInputConsumerCommands() {
		try (var sys = SystemIoCaptor.of();
			var m = ManualTester.builderArray(1).command(Integer.class, (t, s, i) -> {
				if (!String.valueOf(i).equals(s)) return false;
				t.out("found:" + i);
				return true;
			}, "test-help").build()) {
			sys.in.print("0;1;!\n");
			m.run();
			assertFind(sys.out, "found:1");
			assertFind(sys.err, "(?i)invalid command: 0");
		}
	}

	@Test
	public void shouldPrintErrors() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builder("test").build()) {
			m.errf("err-%s", "test");
			m.err(new IOException(), true);
			assertFind(sys.err, "err-test");
			assertFind(sys.err, "IOException");
		}
	}

	@Test
	public void shouldPrintBytesReadFromInputStream() throws IOException {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builder("test").build()) {
			m.readBytes(inputStream());
			assertTrue(sys.out.isEmpty());
			m.readBytes(inputStream(1, -1, 0));
			assertFind(sys.out, "01 ff 00");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPrintBytesWrittenToOutputStream() throws IOException {
		try (var sys = SystemIoCaptor.of(); var pipe = PipedStream.of();
			var m = ManualTester.builder("test").build()) {
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
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1)
			.command(Integer.class, "i", (_, _, _) -> {}, "int-cmd").build()) {
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
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1)
			.command(Integer.class, "i", (t, _, _) -> t.out("int-cmd"), "i").build()) {
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
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray(null, 1)
			.preProcessor(Integer.class, (t, _) -> t.out("int-cmd")).build()) {
			sys.in.print(":;!\n");
			m.run();
			assertNotFound(sys.out, "int-cmd");
			sys.in.print("+\n!\n"); // move to next subject first
			m.run();
			assertFind(sys.out, "int-cmd");
		}
	}

	@Test
	public void shouldProvideRuntimeExecutionWarning() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1, 1.0)
			.command(Object.class, "x(\\d+)", rt(checkInt()), null).build()) {
			sys.in.print("x9\nx0\nx1\n");
			Assert.thrown(RuntimeInterruptedException.class, m::run);
			assertMatch(sys.err, "\\s*0\\s*");
		}
	}

	@Test
	public void shouldApplyHistory() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1, 1.0)
			.history(2, 1).historical(s -> s.length() > 1).build()) {
			sys.in.print("++\n--\n<\n<1\n<5\n!\n");
			m.run();
			assertFind(sys.out, "(?s)test>.*?1\\.0>.*?test>.*?test>.*?1>");
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldRepeatCommands() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1, 1.0)
			.command("x", exitAfterCount(3, sys.in), "x help").build()) {
			sys.in.print("^3;:;+;x\n");
			m.run();
			assertFind(sys.out, "(?s)String.*?Integer.*?Double");
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldRepeatCommandsUntilInputAvailable() {
		try (var sys = SystemIoCaptor.of(); var m = ManualTester.builderArray("test", 1, 1.0)
			.command("x", exitAfterCount(3, sys.in), "x help").build()) {
			sys.in.print("^100;:;+;x\n");
			m.run();
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldCaptureEvents() {
		try (var sys = SystemIoCaptor.of()) {
			var events = ManualTester.EventCatcher.of();
			events.add("event ok");
			events.execute(() -> {});
			events.execute(() -> throwIt(new Exception("event error")));
			sys.in.print("!\n");
			try (var m = ManualTester.builder("test").preProcessor(events).build()) {
				m.run();
			}
			assertFind(sys.out, "event ok");
			assertFind(sys.err, "event error");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldHandleInputIoErrors() throws IOException {
		try (var sys = SystemIoCaptor.of(); var in = TestInputStream.of()) {
			in.to.writeAscii("\n");
			in.read.error.setFrom(IOX);
			try (var m = ManualTester.builder("test").in(in).build()) {
				m.run(); // exits on error
			}
			assertFind(sys.err, "IOException");
		}
	}

	@Test
	public void shouldHandleCommandErrors() {
		try (var sys = SystemIoCaptor.of()) {
			ErrorGen error = ErrorGen.of();
			try (var m = ManualTester.builder(1)
				.command(Integer.class, "x", (_, _, _) -> error.callWithInterrupt(), "x").build()) {
				error.setFrom(IOX, INX, RIX); // IO, Interrupted, then RuntimeInterrupted exceptions
				sys.in.print("x\nx\nx\n");
				Assert.thrown(m::run); // exits on InterruptedException
				assertFind(sys.err, "IOException");
				Assert.thrown(m::run); // exits on RuntimeInterruptedException
			}
		}
	}

	@Test
	public void shouldRunCycle() {
		try (var sys = SystemIoCaptor.of();
			var m = ManualTester.builder("test")
				.command("c(\\-?\\d+)", (t, r, _) -> t.startCycle(cycle(Parse.i(r))), "help")
				.command("C", (t, _, _) -> t.stopCycle(), "help").build()) {
			sys.in.print("C\nc100\nc200\nC\n!\n");
			m.run();
			assertFind(sys.out,
				"(?s)started.*test-cycle:100.*stopped.*started.*test-cycle:200.*stopped");
			assertString(sys.err, "");
		}
	}

	private static Matcher matcher(String pattern, String input) {
		var m = Pattern.compile(pattern).matcher(input);
		assertTrue(m.matches(), "Pattern \"%s\" does not match \"%s\"", pattern, input);
		return m;
	}

	private static Action.Match<Object> checkInt() {
		return (_, m, _) -> {
			switch (i(m)) {
				case 0 -> throw new RuntimeException("0");
				case 1 -> throw new RuntimeInterruptedException("1");
				case 2 -> throw new InterruptedException("2");
				default -> {}
			}
		};
	}

	private static Action.Match<Object> exitAfterCount(int count, PrintStream in) {
		var counter = Counter.of(0);
		return (_, _, _) -> {
			if (counter.inc(1) >= count) in.print("!\n");
		};
	}

	private static CycleRunner.Cycle cycle(int delayMs) {
		return new CycleRunner.Cycle() {
			@Override
			public int cycle(int sequence) {
				return delayMs;
			}

			@Override
			public String name() {
				return "test-cycle";
			}

			@Override
			public String toString() {
				return name() + ":" + delayMs;
			}
		};
	}
}
