package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.ErrorGen.RTX;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.process.Parameters;
import ceri.common.test.TestProcess.TestProcessor;
import ceri.common.time.Timeout;

public class TestProcessBehavior {

	@Test
	public void shouldInitialize() throws IOException {
		try (var p = TestProcess.of(null, null, 0)) {
			assertEquals(p.exitValue(), 0);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReset() throws IOException {
		try (var p = TestProcess.of("in", "err", 0)) {
			p.getOutputStream().write(ArrayUtil.bytes.of(1, 2, 3));
			assertEquals(p.getInputStream().available(), 2);
			assertEquals(p.getErrorStream().available(), 3);
			p.out.assertAvailable(3);
			p.resetState();
			assertEquals(p.getInputStream().available(), 0);
			assertEquals(p.getErrorStream().available(), 0);
			p.out.assertAvailable(0);
		}
	}

	@Test
	public void shouldProvideProcessor() throws IOException {
		TestProcessor p = TestProcess.processor("test");
		assertEquals(p.exec(Parameters.of("a", "b", "c")), "test");
		p.assertParameters("a", "b", "c");
	}

	@Test
	public void shouldResetProcessor() throws IOException {
		TestProcessor p = TestProcess.processor("test");
		p.exec.error.setFrom(RTX);
		p.reset();
		assertEquals(p.exec(), "test");
	}

	@Test
	public void shouldWaitForProcess() throws IOException, InterruptedException {
		try (var p = TestProcess.of("in", "err", 1)) {
			assertEquals(p.waitFor(), 1);
			p.exitValue.error.setFrom(INX);
			assertThrown(() -> p.waitFor());
		}
	}

	@Test
	public void shouldWaitForProcessWithTimeout() throws IOException, InterruptedException {
		try (var p = TestProcess.of("in", "err", 1)) {
			assertEquals(p.waitFor(100, TimeUnit.MILLISECONDS), true);
			p.waitFor.assertValues(Timeout.millis(100));
		}
	}

}
