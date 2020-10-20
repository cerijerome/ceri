package ceri.common.process;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ProcessUtilTest {

	@SuppressWarnings("resource")
	@Test
	public void testStdOut() throws IOException {
		try (InputStream in = TestUtil.inputStream("hello")) {
			Process process = mock(Process.class);
			when(process.getInputStream()).thenReturn(in);
			assertEquals(ProcessUtil.stdOut(process), "hello");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testStdErr() throws IOException {
		try (InputStream in = TestUtil.inputStream("hello")) {
			Process process = mock(Process.class);
			when(process.getErrorStream()).thenReturn(in);
			assertEquals(ProcessUtil.stdErr(process), "hello");
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
