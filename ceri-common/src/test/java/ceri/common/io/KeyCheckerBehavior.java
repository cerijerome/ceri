package ceri.common.io;

import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.concurrent.BooleanCondition;

public class KeyCheckerBehavior {

	@Test
	public void shouldInterruptCurrentThread() throws IOException, InterruptedException {
		InputStream stdin = System.in;
		BooleanCondition sync = BooleanCondition.create();
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			System.setIn(in);
			try (KeyChecker kc = KeyChecker.create(s -> {
				sync.signal();
				return true;
			}, 100L)) {
				assertTrue(sync.await(1000));
				Thread.interrupted();
			}
		} finally {
			System.setIn(stdin);
		}
	}

	@Test
	public void shouldInterruptIfStreamError() throws IOException {
		InputStream stdin = System.in;
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.available()).thenThrow(new IOException());
			System.setIn(in);
			try (KeyChecker kc = KeyChecker.create()) {
				assertException(InterruptedException.class, () -> Thread.sleep(1000));
			}
		} finally {
			System.setIn(stdin);
		}
	}

}
