package ceri.common.process;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import ceri.common.test.TestProcess;

public class ProcessorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Processor t = Processor.builder().timeoutMs(1000).build();
		Processor eq0 = Processor.builder().timeoutMs(1000).build();
		Processor eq1 = Processor.builder(t).build();
		Processor ne0 = Processor.DEFAULT;
		Processor ne1 = Processor.builder(t).noTimeout().build();
		Processor ne2 = Processor.builder(t).captureStdOut(false).build();
		Processor ne3 = Processor.builder(t).verifyErr(false).build();
		Processor ne4 = Processor.builder(t).verifyExitValue(false).build();
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldIgnoreEmptyCommand() throws IOException {
		assertNull(Processor.DEFAULT.exec());
		assertThrown(() -> Processor.DEFAULT.exec(""));
	}

	@Test
	public void shouldCaptureStdOut() throws IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(true).verifyErr(false)
			.verifyExitValue(false).build();
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			ProcessCommand cmd = command(process, "test");
			assertEquals(p.exec(cmd), "stdout");
		}
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(true)
			.verifyExitValue(false).build();
		try (var process = TestProcess.of("stdout", "", -1, true)) {
			ProcessCommand cmd1 = command(process, "test");
			assertNull(p.exec(cmd1));
		}
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			ProcessCommand cmd2 = command(process, "test");
			assertThrown(IOException.class, () -> p.exec(cmd2));
		}
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(true).build();
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			ProcessCommand cmd1 = command(process, "test");
			assertNull(p.exec(cmd1));
		}
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			ProcessCommand cmd2 = command(process, "test");
			assertThrown(IOException.class, () -> p.exec(cmd2));
		}
	}

	@Test
	public void shouldVerifyTimeout() throws IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			ProcessCommand cmd1 = command(process, "test");
			assertNull(p.exec(cmd1));
		}
		try (var process = TestProcess.of("stdout", "stderr", 0, false)) {
			ProcessCommand cmd2 = command(process, "test");
			assertThrown(IOException.class, () -> p.exec(cmd2));
		}
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws IOException {
		Processor p = Processor.builder().timeoutMs(0).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			assertNull(p.exec(command(process, "test")));
			process.exitValue.assertNoCall();
			process.waitFor.assertNoCall();
		}
	}

	@Test
	public void shouldAllowNoTimeout() throws IOException {
		Processor p = Processor.builder().noTimeout().captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			assertNull(p.exec(command(process, "test")));
			process.waitFor.assertNoCall();
		}
	}

	@Test
	public void shouldStopOnInterruption() throws IOException {
		Processor p = Processor.builder().noTimeout().captureStdOut(true).verifyErr(true)
			.verifyExitValue(true).build();
		try (TestProcess process = TestProcess.of("stdout", "stderr", -1, true)) {
			process.exitValue.error.setFrom(INX);
			assertThrown(() -> p.exec(command(process, "test")));
			process.out.assertAvailable(0);
		}
	}

	private static ProcessCommand command(Process process, String... commands) {
		List<String> list = Arrays.asList(commands);
		return ProcessCommand.of(() -> process, () -> list);
	}

}
