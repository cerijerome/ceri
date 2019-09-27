package ceri.common.test;

import static ceri.common.test.TestUtil.assertThrown;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.function.ExceptionRunnable;

public class TestThreadBehavior {
	private @Mock ExceptionRunnable<IOException> runnable;
	private TestThread<IOException> thread;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		thread = TestThread.create(runnable);
	}

	@After
	public void tidyUp() {
		try {
			thread.stop();
		} catch (Exception e) {
			// ignore
		}
	}

	@Test
	public void shouldNotAllowMultipleStarts() {
		thread.start();
		TestUtil.assertThrown(() -> thread.start());
	}

	@Test
	public void shouldCaptureException() throws IOException {
		doThrow(new IOException()).when(runnable).run();
		thread.start();
		assertThrown(IOException.class, () -> thread.stop(1));
		verify(runnable, timeout(1000).atLeastOnce()).run();
	}

	@Test
	public void shouldInterruptThreadOnStop() throws IOException {
		doAnswer(i -> {
			Thread.sleep(1000);
			return null;
		}).when(runnable).run();
		thread.start();
		TestUtil.assertThrown(() -> thread.stop(1));
		verify(runnable, timeout(1000).atLeastOnce()).run();
	}

	@Test
	public void shouldStopWithoutExceptionIfNotSleeping() throws IOException {
		thread.start();
		thread.stop(1000);
		verify(runnable, timeout(1000).atLeastOnce()).run();
	}

	@Test
	public void shouldFailJoinIfNotStarted() {
		TestUtil.assertThrown(() -> thread.join(1));
	}

	@Test
	public void shouldFailJoinIfNotComplete() throws IOException {
		doAnswer(i -> {
			Thread.sleep(1000);
			return null;
		}).when(runnable).run();
		thread.start();
		TestUtil.assertThrown(() -> thread.join(1));
	}

}
