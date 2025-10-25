package ceri.common.process;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.io;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestProcess;

public class ProcessorBehavior {

	@Test
	public void shouldBuildFromProcessor() {
		Processor p0 = Processor.builder().timeoutMs(777).build();
		assertEquals(Processor.builder(p0).timeoutMs, 777);
	}

	@Test
	public void shouldIgnoreEmptyCommand() throws IOException {
		Assert.isNull(Processor.DEFAULT.exec());
		Assert.thrown(() -> Processor.DEFAULT.exec(""));
	}

	@Test
	public void shouldCaptureStdOut() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			Processor p = processor(process).captureStdOut(true).build();
			assertEquals(p.exec("test"), "stdout");
		}
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws IOException {
		try (var process = TestProcess.of("stdout", "", -1)) {
			Processor p = processor(process).verifyErr(true).build();
			assertEquals(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			Processor p = processor(process).verifyErr(true).build();
			io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			Processor p = processor(process).verifyExitValue(true).build();
			assertEquals(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			Processor p = processor(process).verifyExitValue(true).build();
			io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			Processor p = processor(process).build();
			assertEquals(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			process.alive.autoResponses(true);
			Processor p = processor(process).build();
			io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			Processor p = processor(process).timeoutMs(0).build();
			assertEquals(p.exec("test"), "");
		}
	}

	@Test
	public void shouldAllowNoTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			Processor p = processor(process).noTimeout().build();
			assertEquals(p.exec("test"), "");
		}
	}

	@Test
	public void shouldStopOnInterruption() throws IOException {
		try (TestProcess process = TestProcess.of("stdout", "stderr", -1)) {
			Processor p = processor(process).noTimeout().build();
			process.alive.error.setFrom(ErrorGen.RIX);
			Assert.thrown(() -> p.exec("test"));
			process.out.assertAvailable(0);
		}
	}

	private static Processor.Builder processor(Process process) {
		return Processor.builder().pollMs(0).timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).processStarter(_ -> () -> process);
	}
}
