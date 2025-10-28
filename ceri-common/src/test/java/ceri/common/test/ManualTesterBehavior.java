package ceri.common.test;

import static ceri.common.test.ManualTester.rt;
import static ceri.common.test.ManualTester.Parse.i;
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
	private SystemIo sys;
	private SystemIoCaptor sysCap;
	private PipedStream pipe;
	private TestInputStream tin;
	private ManualTester m;

	@Before
	public void before() {
		fastMode = ManualTester.fastMode();
	}

	@After
	public void after() {
		m = Testing.close(m);
		tin = Testing.close(tin);
		pipe = Testing.close(pipe);
		sysCap = Testing.close(sysCap);
		sys = Testing.close(sys);
		Closeables.close(fastMode);
	}

	@Test
	public void shouldParseMatcherBoolean() {
		Assert.equal(ManualTester.Parse.b(matcher("x(.*)", "x")), null);
		Assert.equal(ManualTester.Parse.b(matcher("x(.*)", "x1")), true);
		Assert.equal(ManualTester.Parse.b(matcher("x(.*)", "xYES")), true);
		Assert.equal(ManualTester.Parse.b(matcher("x(.*)", "xtrue")), true);
		Assert.equal(ManualTester.Parse.b(matcher("x(.*)", "x0")), false);
		Assert.equal(ManualTester.Parse.b(matcher("x(.)(.)", "x01"), 2), true);
	}

	@Test
	public void shouldParseMatcherChar() {
		Assert.equal(ManualTester.Parse.c(matcher("x(.*)", "x")), null);
		Assert.equal(ManualTester.Parse.c(matcher("x(.*)", "xxyz")), 'x');
		Assert.equal(ManualTester.Parse.c(matcher("x(.)(.*)", "xxyz"), 2), 'y');
	}

	@Test
	public void shouldParseMatcherInt() {
		Assert.equal(ManualTester.Parse.i(matcher("x(.*)", "x")), null);
		Assert.thrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x123x")));
		Assert.equal(ManualTester.Parse.i(matcher("x(.*)", "x123")), 123);
		Assert.equal(ManualTester.Parse.i(matcher("x(.)(.*)", "x123"), 2), 23);
	}

	@Test
	public void shouldParseMatcherLong() {
		Assert.equal(ManualTester.Parse.l(matcher("x(.*)", "x")), null);
		Assert.thrown(() -> ManualTester.Parse.l(matcher("x(.*)", "x123x")));
		Assert.equal(ManualTester.Parse.l(matcher("x(.*)", "x123")), 123L);
		Assert.equal(ManualTester.Parse.l(matcher("x(.)(.*)", "x123"), 2), 23L);
	}

	@Test
	public void shouldParseMatcherDouble() {
		Assert.equal(ManualTester.Parse.d(matcher("x(.*)", "x")), null);
		Assert.thrown(() -> ManualTester.Parse.i(matcher("x(.*)", "x1.23x")));
		Assert.equal(ManualTester.Parse.d(matcher("x(.*)", "x1.23")), 1.23);
		Assert.equal(ManualTester.Parse.d(matcher("x(.)(.*)", "x1.23"), 2), .23);
	}

	@Test
	public void shouldParseMatcherLen() {
		Assert.equal(ManualTester.Parse.len(matcher("x(.*)", "x")), 0);
		Assert.equal(ManualTester.Parse.len(matcher("x(.*)", "x123")), 3);
		Assert.equal(ManualTester.Parse.len(matcher("x(.)(.*)", "x123"), 2), 2);
	}

	@Test
	public void shouldConsumeMatches() {
		ManualTester.Parse.consumeFirst(matcher("(?:(a)|(b)|(c))", "b"), (x, i) -> {
			Assert.equal(x, "b");
			Assert.equal(i, 2);
		});
		Assert.isNull(ManualTester.Parse.consumeFirst(matcher("(?:(a)|(b)|(c))", "a"), 2,
			(_, _) -> Assert.fail()));
	}

	@Test
	public void shouldApplyMatches() {
		Assert.equal(
			ManualTester.Parse.applyFirst(matcher("(?:(a)|(b)|(c))", "b"), (x, i) -> x + i), "b2");
	}

	@Test
	public void shouldFailIfNoSubject() {
		Assert.thrown(() -> ManualTester.builderList(List.of()));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteCommandsFromStdIn() {
		sys = SystemIo.of();
		sys.in(Testing.inputStream("?;*;-;+;@0;:;~0;!\n"));
		sys.out(IoStream.nullPrint());
		ManualTester.builderArray("test", 1).promptSgr(null).build().run();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBuildAnInstance() {
		fastMode.close();
		sys = SystemIo.of();
		sys.in(Testing.inputStream("!\n"));
		sys.out(IoStream.nullPrint());
		ManualTester.builder("test", String::valueOf).in(System.in).out(System.out).err(System.err)
			.indent("  ").promptSgr(AnsiEscape.csi.sgr().fgColor(BasicColor.cyan, false))
			.preProcessor(String.class, (m, s) -> m.out(s)).preProcessor(m -> m.out("test"))
			.listenTo().delayMs(0).errorDelayMs(0).build().run();
	}

	@Test
	public void shouldProvideStringRepresentation() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", "x").command("a", (_, _, _) -> {}, "a")
			.command("b", (_, _, _) -> {}, "b").command("c", (_, _, _) -> {}, "c").build();
		Assert.find(m, "2,12");
	}

	@Test
	public void shouldProvideCommandSeparators() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builder("test").separatorSgr(null).separator("--test--")
			.command(".*", (_, _, _) -> {}, "end-test").build();
		sysCap.in.print("x\n!\n");
		m.run();
		Assert.find(sysCap.out, "(?s)--test--.*end-test");
	}

	@Test
	public void shouldProvideInputConsumerCommands() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray(1).command(Integer.class, (t, s, i) -> {
			if (!String.valueOf(i).equals(s)) return false;
			t.out("found:" + i);
			return true;
		}, "test-help").build();
		sysCap.in.print("0;1;!\n");
		m.run();
		Assert.find(sysCap.out, "found:1");
		Assert.find(sysCap.err, "(?i)invalid command: 0");
	}

	@Test
	public void shouldPrintErrors() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builder("test").build();
		m.errf("err-%s", "test");
		m.err(new IOException(), true);
		Assert.find(sysCap.err, "err-test");
		Assert.find(sysCap.err, "IOException");
	}

	@Test
	public void shouldPrintBytesReadFromInputStream() throws IOException {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builder("test").build();
		m.readBytes(Testing.inputStream());
		Assert.yes(sysCap.out.isEmpty());
		m.readBytes(Testing.inputStream(1, -1, 0));
		Assert.find(sysCap.out, "01 ff 00");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPrintBytesWrittenToOutputStream() throws IOException {
		sysCap = SystemIoCaptor.of();
		pipe = PipedStream.of();
		var m = ManualTester.builder("test").build();
		m.writeAscii(pipe.out(), "");
		Assert.yes(sysCap.out.isEmpty());
		Assert.equal(pipe.in().available(), 0);
		m.writeAscii(pipe.out(), "ascii");
		Assert.find(sysCap.out, "ascii");
		Assert.read(pipe.in(), "ascii".getBytes());
	}

	@Test
	public void shouldShowHelpForMatchingCommandsOnly() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1)
			.command(Integer.class, "i", (_, _, _) -> {}, "int-cmd").build();
		sysCap.in.print("!\n"); // shows help for first subject
		m.run();
		Assert.notFound(sysCap.out, "int-cmd");
		sysCap.in.print("+;?;!\n"); // move to next subject
		m.run();
		Assert.find(sysCap.out, "int-cmd");
	}

	@Test
	public void shouldExecuteCommandOnlyIfMatchingType() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1)
			.command(Integer.class, "i", (t, _, _) -> t.out("int-cmd"), "i").build();
		sysCap.in.print("i\n!\n"); // invalid command
		m.run();
		Assert.notFound(sysCap.out, "int-cmd");
		Assert.find(sysCap.err, "Invalid");
		sysCap.in.print("+;i\n!\n"); // move to next subject first
		m.run();
		Assert.find(sysCap.out, "int-cmd");
	}

	@Test
	public void shouldPreProcessOnlyIfMatchingType() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray(null, 1)
			.preProcessor(Integer.class, (t, _) -> t.out("int-cmd")).build();
		sysCap.in.print(":;!\n");
		m.run();
		Assert.notFound(sysCap.out, "int-cmd");
		sysCap.in.print("+\n!\n"); // move to next subject first
		m.run();
		Assert.find(sysCap.out, "int-cmd");
	}

	@Test
	public void shouldProvideRuntimeExecutionWarning() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1, 1.0)
			.command(Object.class, "x(\\d+)", rt(checkInt()), null).build();
		sysCap.in.print("x9\nx0\nx1\n");
		Assert.thrown(RuntimeInterruptedException.class, m::run);
		Assert.match(sysCap.err, "\\s*0\\s*");
	}

	@Test
	public void shouldApplyHistory() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1, 1.0).history(2, 1).historical(s -> s.length() > 1)
			.build();
		sysCap.in.print("++\n--\n<\n<1\n<5\n!\n");
		m.run();
		Assert.find(sysCap.out, "(?s)test>.*?1\\.0>.*?test>.*?test>.*?1>");
		Assert.string(sysCap.err, "");
	}

	@Test
	public void shouldRepeatCommands() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1, 1.0)
			.command("x", exitAfterCount(3, sysCap.in), "x help").build();
		sysCap.in.print("^3;:;+;x\n");
		m.run();
		Assert.find(sysCap.out, "(?s)String.*?Integer.*?Double");
		Assert.string(sysCap.err, "");
	}

	@Test
	public void shouldRepeatCommandsUntilInputAvailable() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builderArray("test", 1, 1.0)
			.command("x", exitAfterCount(3, sysCap.in), "x help").build();
		sysCap.in.print("^100;:;+;x\n");
		m.run();
		Assert.string(sysCap.err, "");
	}

	@Test
	public void shouldCaptureEvents() {
		sysCap = SystemIoCaptor.of();
		var events = ManualTester.EventCatcher.of();
		events.add("event ok");
		events.execute(() -> {});
		events.execute(() -> Assert.throwIt(new Exception("event error")));
		sysCap.in.print("!\n");
		try (var m = ManualTester.builder("test").preProcessor(events).build()) {
			m.run();
		}
		Assert.find(sysCap.out, "event ok");
		Assert.find(sysCap.err, "event error");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldHandleInputIoErrors() {
		sysCap = SystemIoCaptor.of();
		tin = TestInputStream.of();
		tin.to.writeAscii("\n");
		tin.read.error.setFrom(ErrorGen.IOX);
		m = ManualTester.builder("test").in(tin).build();
		m.run(); // exits on error
		m.close();
		Assert.find(sysCap.err, "IOException");
	}

	@Test
	public void shouldHandleCommandErrors() {
		sysCap = SystemIoCaptor.of();
		ErrorGen error = ErrorGen.of();
		m = ManualTester.builder(1)
			.command(Integer.class, "x", (_, _, _) -> error.callWithInterrupt(), "x").build();
		error.setFrom(ErrorGen.IOX, ErrorGen.INX, ErrorGen.RIX);
		sysCap.in.print("x\nx\nx\n");
		Assert.thrown(m::run); // exits on InterruptedException
		Assert.find(sysCap.err, "IOException");
		Assert.thrown(m::run); // exits on RuntimeInterruptedException
	}

	@Test
	public void shouldRunCycle() {
		sysCap = SystemIoCaptor.of();
		m = ManualTester.builder("test")
			.command("c(\\-?\\d+)", (t, r, _) -> t.startCycle(cycle(Parse.i(r))), "help")
			.command("C", (t, _, _) -> t.stopCycle(), "help").build();
		sysCap.in.print("C\nc100\nc200\nC\n!\n");
		m.run();
		Assert.find(sysCap.out,
			"(?s)started.*test-cycle:100.*stopped.*started.*test-cycle:200.*stopped");
		Assert.string(sysCap.err, "");
	}

	private static Matcher matcher(String pattern, String input) {
		var m = Pattern.compile(pattern).matcher(input);
		Assert.yes(m.matches(), "Pattern \"%s\" does not match \"%s\"", pattern, input);
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
