package ceri.common.process;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.TestProcess;

public class ProcessUtilTest {

	@Test
	public void testStdOut() throws IOException {
		try (var process = TestProcess.of("test", null, 0, true)) {
			assertEquals(ProcessUtil.stdOut(process), "test");
		}
	}

	@Test
	public void testStdErr() throws IOException {
		try (var process = TestProcess.of(null, "test", 0, true)) {
			assertEquals(ProcessUtil.stdErr(process), "test");
		}
	}

	@Test
	public void testToString() {
		ProcessBuilder b = new ProcessBuilder("cmd1", "cmd 2", "cmd # 3");
		assertEquals(ProcessUtil.toString(b), "cmd1 \"cmd 2\" \"cmd # 3\"");
	}

	@SuppressWarnings("resource")
	@Test
	public void testNullProcess() throws InterruptedException, IOException {
		Process p = ProcessUtil.nullProcess();
		p.destroy();
		assertEquals(p.exitValue(), 0);
		assertEquals(p.waitFor(), 0);
		assertTrue(p.waitFor(1, TimeUnit.MILLISECONDS));
		assertArray(p.getInputStream().readAllBytes());
		assertArray(p.getErrorStream().readAllBytes());
		p.getOutputStream().write(new byte[16]);
	}

}
