package ceri.common.process;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.INX;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestProcess;

public class ProcessorBehavior {

	@Test
	public void shouldBuildFromProcessor() {
		Processor p0 = Processor.builder().timeoutMs(777).build();
		assertEquals(Processor.builder(p0).timeoutMs, 777);
	}

	@Test
	public void shouldIgnoreEmptyCommand() throws IOException {
		assertNull(Processor.DEFAULT.exec());
		assertThrown(() -> Processor.DEFAULT.exec(""));
	}

	@Test
	public void shouldCaptureStdOut() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			Processor p = processor(process).captureStdOut(true).build();
			assertEquals(p.exec("test"), "stdout");
		}
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws IOException {
		try (var process = TestProcess.of("stdout", "", -1, true)) {
			Processor p = processor(process).verifyErr(true).build();
			assertNull(p.exec("test"));
		}
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			Processor p = processor(process).verifyErr(true).build();
			assertThrown(IOException.class, () -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			Processor p = processor(process).verifyExitValue(true).build();
			assertNull(p.exec("test"));
		}
		try (var process = TestProcess.of("stdout", "stderr", -1, true)) {
			Processor p = processor(process).verifyExitValue(true).build();
			assertThrown(IOException.class, () -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			Processor p = processor(process).build();
			assertNull(p.exec("test"));
		}
		try (var process = TestProcess.of("stdout", "stderr", 0, false)) {
			Processor p = processor(process).build();
			assertThrown(IOException.class, () -> p.exec("test"));
		}
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			Processor p = processor(process).timeoutMs(0).build();
			assertNull(p.exec("test"));
			process.exitValue.assertNoCall();
			process.waitFor.assertNoCall();
		}
	}

	@Test
	public void shouldAllowNoTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0, true)) {
			Processor p = processor(process).noTimeout().build();
			assertNull(p.exec("test"));
			process.waitFor.assertNoCall();
		}
	}

	@Test
	public void shouldStopOnInterruption() throws IOException {
		// Processor p = Processor.builder().noTimeout().captureStdOut(true).verifyErr(true)
		// .verifyExitValue(true).build();
		try (TestProcess process = TestProcess.of("stdout", "stderr", -1, true)) {
			Processor p = processor(process).noTimeout().build();
			process.exitValue.error.setFrom(INX);
			assertThrown(() -> p.exec("test"));
			process.out.assertAvailable(0);
		}
	}

	private static Processor.Builder processor(Process process) {
		return Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).processStarter(x -> () -> process);
	}
}
