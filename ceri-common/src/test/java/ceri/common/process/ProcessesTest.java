package ceri.common.process;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestProcess;

public class ProcessesTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Processes.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void testNullProcess() throws InterruptedException, IOException {
		var p = Processes.NULL;
		p.destroy();
		Assert.equal(p.exitValue(), 0);
		Assert.equal(p.waitFor(), 0);
		Assert.yes(p.waitFor(1, TimeUnit.MILLISECONDS));
		Assert.array(p.getInputStream().readAllBytes());
		Assert.array(p.getErrorStream().readAllBytes());
		p.getOutputStream().write(new byte[16]);
	}
	
	@Test
	public void testStdOut() throws IOException {
		try (var process = TestProcess.of("test", null, 0)) {
			Assert.equal(Processes.stdOut(process), "test");
		}
	}

	@Test
	public void testStdErr() throws IOException {
		try (var process = TestProcess.of(null, "test", 0)) {
			Assert.equal(Processes.stdErr(process), "test");
		}
	}

	@Test
	public void testToString() {
		var b = new ProcessBuilder("cmd1", "cmd 2", "cmd # 3");
		Assert.equal(Processes.toString(b), "cmd1 \"cmd 2\" \"cmd # 3\"");
	}
}
