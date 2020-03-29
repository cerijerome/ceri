package ceri.common.process;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
			assertThat(ProcessUtil.stdOut(process), is("hello"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testStdErr() throws IOException {
		try (InputStream in = TestUtil.inputStream("hello")) {
			Process process = mock(Process.class);
			when(process.getErrorStream()).thenReturn(in);
			assertThat(ProcessUtil.stdErr(process), is("hello"));
		}
	}

	@Test
	public void testToString() {
		ProcessBuilder b = new ProcessBuilder("cmd1", "cmd 2", "cmd # 3");
		assertThat(ProcessUtil.toString(b), is("cmd1 \"cmd 2\" \"cmd # 3\""));
	}

	@SuppressWarnings("resource")
	@Test
	public void testNullProcess() throws InterruptedException, IOException {
		Process p = ProcessUtil.nullProcess();
		p.destroy();
		assertThat(p.exitValue(), is(0));
		assertThat(p.waitFor(), is(0));
		assertThat(p.waitFor(1, TimeUnit.MILLISECONDS), is(true));
		assertArray(p.getInputStream().readAllBytes());
		assertArray(p.getErrorStream().readAllBytes());
		p.getOutputStream().write(new byte[16]);
	}

}
