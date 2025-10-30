package ceri.common.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.process.Parameters;
import ceri.common.test.TestProcess.TestProcessor;
import ceri.common.time.Timeout;

public class TestProcessBehavior {

	@Test
	public void shouldInitialize() throws IOException {
		try (var p = TestProcess.of(null, null, 0)) {
			Assert.equal(p.exitValue(), 0);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReset() throws IOException {
		try (var p = TestProcess.of("in", "err", 0)) {
			p.getOutputStream().write(Array.bytes.of(1, 2, 3));
			Assert.equal(p.getInputStream().available(), 2);
			Assert.equal(p.getErrorStream().available(), 3);
			p.out.assertAvailable(3);
			p.resetState();
			Assert.equal(p.getInputStream().available(), 0);
			Assert.equal(p.getErrorStream().available(), 0);
			p.out.assertAvailable(0);
		}
	}

	@Test
	public void shouldProvideProcessor() throws IOException {
		TestProcessor p = TestProcess.processor("test");
		Assert.equal(p.exec(Parameters.of("a", "b", "c")), "test");
		p.assertParameters("a", "b", "c");
	}

	@Test
	public void shouldResetProcessor() throws IOException {
		TestProcessor p = TestProcess.processor("test");
		p.exec.error.setFrom(ErrorGen.RTX);
		p.reset();
		Assert.equal(p.exec(), "test");
	}

	@Test
	public void shouldWaitForProcess() throws IOException, InterruptedException {
		try (var p = TestProcess.of("in", "err", 1)) {
			Assert.equal(p.waitFor(), 1);
			p.exitValue.error.setFrom(ErrorGen.INX);
			Assert.thrown(() -> p.waitFor());
		}
	}

	@Test
	public void shouldWaitForProcessWithTimeout() throws IOException, InterruptedException {
		try (var p = TestProcess.of("in", "err", 1)) {
			Assert.equal(p.waitFor(100, TimeUnit.MILLISECONDS), true);
			p.waitFor.assertValues(Timeout.millis(100));
		}
	}
}
