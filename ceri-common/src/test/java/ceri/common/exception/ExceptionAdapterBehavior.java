package ceri.common.exception;

import static ceri.common.exception.ExceptionAdapter.NULL;
import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
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
		assertEquals(RUNTIME.get(() -> null), null);
		assertEquals(RUNTIME.get(() -> "test"), "test");
		assertRte(() -> RUNTIME.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> RUNTIME.get(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> IoUtil.IO_ADAPTER.get(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnBoolean() {
		assertFalse(RUNTIME.getBoolean(() -> false));
		assertTrue(RUNTIME.getBoolean(() -> true));
		assertRte(() -> RUNTIME.getBoolean(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> RUNTIME.getBoolean(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> IoUtil.IO_ADAPTER.getBoolean(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnInt() {
		assertEquals(RUNTIME.getInt(() -> Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(RUNTIME.getInt(() -> Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertRte(() -> RUNTIME.getInt(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> RUNTIME.getInt(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> IoUtil.IO_ADAPTER.getInt(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnLong() {
		assertEquals(RUNTIME.getLong(() -> Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(RUNTIME.getLong(() -> Long.MAX_VALUE), Long.MAX_VALUE);
		assertRte(() -> RUNTIME.getLong(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> RUNTIME.getLong(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> IoUtil.IO_ADAPTER.getLong(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnDouble() {
		assertEquals(RUNTIME.getDouble(() -> Double.MIN_VALUE), Double.MIN_VALUE);
		assertEquals(RUNTIME.getDouble(() -> Double.MAX_VALUE), Double.MAX_VALUE);
		assertRte(() -> RUNTIME.getDouble(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> RUNTIME.getDouble(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> IoUtil.IO_ADAPTER.getDouble(() -> {
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
		assertThrown(() -> ExceptionAdapter.of(FileNotFoundException.class));
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
