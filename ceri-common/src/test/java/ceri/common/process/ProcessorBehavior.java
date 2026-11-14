package ceri.common.process;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestProcess;
import ceri.common.test.Testing;

public class ProcessorBehavior {
	private TestProcess process;

	@AfterEach
	public void afterEach() {
		process = Testing.close(process);
	}

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
		process = TestProcess.of("stdout", "stderr", -1);
		var p = processor(process).captureStdOut(true).build();
		Assert.equal(p.exec("test"), "stdout");
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws IOException {
		process = TestProcess.of("stdout", "", -1);
		var p0 = processor(process).verifyErr(true).build();
		Assert.equal(p0.exec("test"), "");
		process.close();
		process = TestProcess.of("stdout", "stderr", -1);
		var p1 = processor(process).verifyErr(true).build();
		Assert.io(() -> p1.exec("test"));
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws IOException {
		process = TestProcess.of("stdout", "stderr", 0);
		var p0 = processor(process).verifyExitValue(true).build();
		Assert.equal(p0.exec("test"), "");
		process.close();
		process = TestProcess.of("stdout", "stderr", -1);
		var p1 = processor(process).verifyExitValue(true).build();
		Assert.io(() -> p1.exec("test"));
	}

	@Test
	public void shouldVerifyTimeout() throws IOException {
		process = TestProcess.of("stdout", "stderr", 0);
		var p0 = processor(process).build();
		Assert.equal(p0.exec("test"), "");
		process.close();
		process = TestProcess.of("stdout", "stderr", 0);
		process.alive.autoResponses(true);
		var p1 = processor(process).build();
		Assert.io(() -> p1.exec("test"));
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws IOException {
		process = TestProcess.of("stdout", "stderr", 0);
		var p = processor(process).timeoutMs(0).build();
		Assert.equal(p.exec("test"), "");
	}

	@Test
	public void shouldAllowNoTimeout() throws IOException {
		process = TestProcess.of("stdout", "stderr", 0);
		var p = processor(process).noTimeout().build();
		Assert.equal(p.exec("test"), "");
	}

	@Test
	public void shouldStopOnInterruption() throws IOException {
		process = TestProcess.of("stdout", "stderr", -1);
		var p = processor(process).noTimeout().build();
		process.alive.error.setFrom(ErrorGen.RIX);
		Assert.thrown(() -> p.exec("test"));
		process.out.assertAvailable(0);
	}

	@Test
	public void shouldCheckIfProcessIsAlive() throws IOException {
		process = TestProcess.of();
		process.alive.autoResponses(true, false);
		var p = processor(process).timeoutMs(1000).build();
		Assert.string(p.exec("test"), "");
	}

	private static Processor.Builder processor(Process process) {
		return Processor.builder().pollMs(0).timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).processStarter(_ -> () -> process);
	}
}
