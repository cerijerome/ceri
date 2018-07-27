package ceri.common.io;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KeyCheckerBehavior {
	private @Mock InputStream in;
	private @Mock Thread thread;
	private @Mock Predicate<String> checkFunction;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldShutdownOnClose() {
		try (KeyChecker k = KeyChecker.of()) {
			//
		}
	}

	@Test
	public void shouldIgnoreNoInput() throws IOException {
		try (KeyChecker k = keyChecker()) {
			verify(in, timeout(1000).atLeastOnce()).available();
		}
		verifyNoMoreInteractions(checkFunction, thread);
	}

	@Test
	public void shouldCheckInput() throws IOException {
		when(in.available()).thenReturn(1);
		try (KeyChecker k = keyChecker()) {
			verify(in, timeout(1000).atLeastOnce()).available();
		}
		verify(checkFunction, atLeastOnce()).test(new String(""));
		verifyNoMoreInteractions(thread);
	}

	@Test
	public void shouldInterruptOnIoError() throws IOException {
		doThrow(new IOException()).when(in).available();
		try (KeyChecker k = keyChecker()) {
			verify(in, timeout(1000).atLeastOnce()).available();
		}
		verify(thread, atLeastOnce()).interrupt();
	}

	@Test
	public void shouldInterruptOnMatch() throws IOException {
		when(in.available()).thenReturn(1);
		when(checkFunction.test(anyString())).thenReturn(true);
		try (KeyChecker k = keyChecker()) {
			verify(in, timeout(1000).atLeastOnce()).available();
		}
		verify(checkFunction, atLeastOnce()).test(new String(""));
		verify(thread, atLeastOnce()).interrupt();
	}

	private KeyChecker keyChecker() {
		return KeyChecker.builder().pollMs(1).in(in).threadToInterrupt(thread)
			.checkFunction(checkFunction).build();
	}

}
