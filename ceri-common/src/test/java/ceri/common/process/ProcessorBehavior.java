package ceri.common.process;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestProcess;

public class ProcessorBehavior {

	@Test
	public void shouldBuildFromProcessor() {
		var p0 = Processor.builder().timeoutMs(777).build();
		Assert.equal(Processor.builder(p0).timeoutMs, 777);
	}

	@Test
	public void shouldIgnoreEmptyCommand() throws IOException {
		Assert.isNull(Processor.DEFAULT.exec());
		Assert.thrown(() -> Processor.DEFAULT.exec(""));
	}

	@Test
	public void shouldCaptureStdOut() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			var p = processor(process).captureStdOut(true).build();
			Assert.equal(p.exec("test"), "stdout");
		}
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws IOException {
		try (var process = TestProcess.of("stdout", "", -1)) {
			var p = processor(process).verifyErr(true).build();
			Assert.equal(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			var p = processor(process).verifyErr(true).build();
			Assert.io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			var p = processor(process).verifyExitValue(true).build();
			Assert.equal(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			var p = processor(process).verifyExitValue(true).build();
			Assert.io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldVerifyTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			var p = processor(process).build();
			Assert.equal(p.exec("test"), "");
		}
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			process.alive.autoResponses(true);
			var p = processor(process).build();
			Assert.io(() -> p.exec("test"));
		}
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			var p = processor(process).timeoutMs(0).build();
			Assert.equal(p.exec("test"), "");
		}
	}

	@Test
	public void shouldAllowNoTimeout() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", 0)) {
			var p = processor(process).noTimeout().build();
			Assert.equal(p.exec("test"), "");
		}
	}

	@Test
	public void shouldStopOnInterruption() throws IOException {
		try (var process = TestProcess.of("stdout", "stderr", -1)) {
			var p = processor(process).noTimeout().build();
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
