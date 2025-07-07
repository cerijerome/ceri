package ceri.common.exception;

import static ceri.common.exception.ExceptionAdapter.none;
import static ceri.common.exception.ExceptionAdapter.runtime;
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

public class ExceptionAdapterBehavior {

	@Test
	public void shouldNotAdaptMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Exception t = new FileNotFoundException("test");
		assertEquals(ad.apply(t), t);
		t = new IOException("test");
		assertEquals(ad.apply(t), t);
	}

	@Test
	public void shouldReturnType() {
		assertEquals(runtime.get(() -> null), null);
		assertEquals(runtime.get(() -> "test"), "test");
		assertRte(() -> runtime.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> runtime.get(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.get(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnBoolean() {
		assertFalse(runtime.getBool(() -> false));
		assertTrue(runtime.getBool(() -> true));
		assertRte(() -> runtime.getBool(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> runtime.getBool(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.getBool(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnInt() {
		assertEquals(runtime.getInt(() -> Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(runtime.getInt(() -> Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertRte(() -> runtime.getInt(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> runtime.getInt(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.getInt(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnLong() {
		assertEquals(runtime.getLong(() -> Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(runtime.getLong(() -> Long.MAX_VALUE), Long.MAX_VALUE);
		assertRte(() -> runtime.getLong(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> runtime.getLong(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.getLong(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnDouble() {
		assertEquals(runtime.getDouble(() -> Double.MIN_VALUE), Double.MIN_VALUE);
		assertEquals(runtime.getDouble(() -> Double.MAX_VALUE), Double.MAX_VALUE);
		assertRte(() -> runtime.getDouble(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> runtime.getDouble(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.getDouble(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldAdaptNonMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Exception t = new InterruptedException("test");
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
		Exception t = new FileNotFoundException("test");
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
		Exception t = new FileNotFoundException("test");
		assertEquals(none.apply(t), t);
		t = new IOException("test");
		assertEquals(none.apply(t), t);
		t = new InterruptedException("test");
		assertEquals(none.apply(t), t);
		t = new RuntimeException("test");
		assertEquals(none.apply(t), t);
		t = new Exception("test");
		assertEquals(none.apply(t), t);
	}

	@Test
	public void shouldAdaptRuntimeExceptions() {
		Exception t = new IOException("test");
		RuntimeException e = runtime.apply(t);
		assertNotEquals(e, t);
		t = new InterruptedException("test");
		e = runtime.apply(t);
		assertNotEquals(e, t);
		t = new Exception("test");
		e = runtime.apply(t);
		assertNotEquals(e, t);
		t = new IllegalArgumentException("test");
		e = runtime.apply(t);
		assertEquals(e, t);
		t = new RuntimeException("test");
		e = runtime.apply(t);
		assertEquals(e, t);
	}
}
