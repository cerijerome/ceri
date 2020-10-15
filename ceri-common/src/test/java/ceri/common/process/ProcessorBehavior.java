package ceri.common.process;

import static ceri.common.process.ProcessTestUtil.command;
import static ceri.common.process.ProcessTestUtil.mockProcess;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;

public class ProcessorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Processor t = Processor.builder().timeoutMs(1000).build();
		Processor eq0 = Processor.builder().timeoutMs(1000).build();
		Processor eq1 = Processor.builder(t).build();
		Processor ne0 = Processor.DEFAULT;
		Processor ne1 = Processor.builder(t).noTimeout().build();
		Processor ne2 = Processor.builder(t).captureStdOut(false).build();
		Processor ne3 = Processor.builder(t).verifyErr(false).build();
		Processor ne4 = Processor.builder(t).verifyExitValue(false).build();
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldIgnoreEmptyCommand() throws IOException {
		assertNull(Processor.DEFAULT.exec());
		assertThrown(() -> Processor.DEFAULT.exec(""));
	}

	@Test
	public void shouldCaptureStdOut() throws IOException, InterruptedException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(true).verifyErr(false)
			.verifyExitValue(false).build();
		ProcessCommand cmd = command(mockProcess("stdout", "stderr", -1, true), "test");
		assertThat(p.exec(cmd), is("stdout"));
	}

	@Test
	public void shouldVerifyEmptyStdErr() throws InterruptedException, IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(true)
			.verifyExitValue(false).build();
		ProcessCommand cmd1 = command(mockProcess("stdout", "", -1, true), "test");
		assertNull(p.exec(cmd1));
		ProcessCommand cmd2 = command(mockProcess("stdout", "stderr", -1, true), "test");
		assertThrown(IOException.class, () -> p.exec(cmd2));
	}

	@Test
	public void shouldVerifyEmptyExitCode() throws InterruptedException, IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(true).build();
		ProcessCommand cmd1 = command(mockProcess("stdout", "stderr", 0, true), "test");
		assertNull(p.exec(cmd1));
		ProcessCommand cmd2 = command(mockProcess("stdout", "stderr", -1, true), "test");
		assertThrown(IOException.class, () -> p.exec(cmd2));
	}

	@Test
	public void shouldVerifyTimeout() throws InterruptedException, IOException {
		Processor p = Processor.builder().timeoutMs(1).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		ProcessCommand cmd1 = command(mockProcess("stdout", "stderr", 0, true), "test");
		assertNull(p.exec(cmd1));
		ProcessCommand cmd2 = command(mockProcess("stdout", "stderr", 0, false), "test");
		assertThrown(IOException.class, () -> p.exec(cmd2));
	}

	@Test
	public void shouldNotVerifyZeroTimeout() throws InterruptedException, IOException {
		Processor p = Processor.builder().timeoutMs(0).captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		Process mock = mockProcess("stdout", "stderr", 0, true);
		assertNull(p.exec(command(mock, "test")));
		verify(mock, never()).waitFor();
		verify(mock, never()).waitFor(anyLong(), any());
	}

	@Test
	public void shouldAllowNoTimeout() throws InterruptedException, IOException {
		Processor p = Processor.builder().noTimeout().captureStdOut(false).verifyErr(false)
			.verifyExitValue(false).build();
		Process mock = mockProcess("stdout", "stderr", 0, true);
		assertNull(p.exec(command(mock, "test")));
		verify(mock, never()).waitFor(anyLong(), any());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldStopOnInterruption() throws InterruptedException {
		Processor p = Processor.builder().noTimeout().captureStdOut(true).verifyErr(true)
			.verifyExitValue(true).build();
		Process mock = mockProcess("stdout", "stderr", -1, true);
		when(mock.waitFor()).thenThrow(InterruptedException.class);
		assertThrown(RuntimeInterruptedException.class, () -> p.exec(command(mock, "test")));
		verify(mock, never()).getOutputStream();
		verify(mock, never()).exitValue();
	}

}
