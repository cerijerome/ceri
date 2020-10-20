package ceri.common.exception;

import static ceri.common.exception.ExceptionAdapter.NULL;
import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.IoUtil;

public class ExceptionAdapterBehavior {

	@Test
	public void shouldNotAdaptMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Throwable t = new FileNotFoundException("test");
		assertEquals(ad.apply(t), t);
		t = new IOException("test");
		assertEquals(ad.apply(t), t);
	}

	@Test
	public void shouldReturnType() {
		assertEquals(RUNTIME.get(() -> "test"), "test");
		assertThrown(RuntimeException.class, () -> RUNTIME.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.get(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnBoolean() {
		assertFalse(RUNTIME.getBoolean(() -> false));
		assertThrown(RuntimeException.class, () -> RUNTIME.getBoolean(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.getBoolean(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnInt() {
		assertEquals(RUNTIME.getInt(() -> Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertThrown(RuntimeException.class, () -> RUNTIME.getInt(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.getInt(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnLong() {
		assertEquals(RUNTIME.getLong(() -> Long.MAX_VALUE), Long.MAX_VALUE);
		assertThrown(RuntimeException.class, () -> RUNTIME.getLong(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.getLong(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldAdaptNonMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Throwable t = new InterruptedException("test");
		IOException e = ad.apply(t);
		assertNotEquals(e, t);
		t = new Exception("test");
		e = ad.apply(t);
		assertNotEquals(e, t);
	}

	@Test
	public void shouldCreateAdapterFunctionFromClass() {
		assertThrown(() -> ExceptionAdapter.of(FileNotFoundException.class)); // no
																				// matching
		// con.
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class);
		Throwable t = new FileNotFoundException("test");
		assertEquals(ad.apply(t), t);
		t = new IOException("test");
		assertEquals(ad.apply(t), t);
		t = new InterruptedException("test");
		IOException e = ad.apply(t);
		assertNotEquals(e, t);
		t = new Exception("test");
		e = ad.apply(t);
		assertNotEquals(e, t);
	}

	@Test
	public void shouldAdaptNoExceptionsIfNull() {
		Throwable t = new FileNotFoundException("test");
		assertEquals(NULL.apply(t), t);
		t = new IOException("test");
		assertEquals(NULL.apply(t), t);
		t = new InterruptedException("test");
		assertEquals(NULL.apply(t), t);
		t = new RuntimeException("test");
		assertEquals(NULL.apply(t), t);
		t = new Exception("test");
		assertEquals(NULL.apply(t), t);
	}

	@Test
	public void shouldAdaptRuntimeExceptions() {
		Throwable t = new IOException("test");
		RuntimeException e = RUNTIME.apply(t);
		assertNotEquals(e, t);
		t = new InterruptedException("test");
		e = RUNTIME.apply(t);
		assertNotEquals(e, t);
		t = new Exception("test");
		e = RUNTIME.apply(t);
		assertNotEquals(e, t);
		t = new IllegalArgumentException("test");
		e = RUNTIME.apply(t);
		assertEquals(e, t);
		t = new RuntimeException("test");
		e = RUNTIME.apply(t);
		assertEquals(e, t);
	}

}
