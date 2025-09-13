package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIllegalState;
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
	public void shouldAdaptNoExceptionsIfNull() {
		Exception t = new FileNotFoundException("test");
		assertEquals(ExceptionAdapter.none.apply(t), t);
		t = new IOException("test");
		assertEquals(ExceptionAdapter.none.apply(t), t);
		t = new InterruptedException("test");
		assertEquals(ExceptionAdapter.none.apply(t), t);
		t = new RuntimeException("test");
		assertEquals(ExceptionAdapter.none.apply(t), t);
		t = new Exception("test");
		assertEquals(ExceptionAdapter.none.apply(t), t);
	}

	@Test
	public void shouldNotThrow() {
		assertIllegalState(() -> ExceptionAdapter.shouldNotThrow.get(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void shouldThrowErrors() {
		assertAssertion(() -> ExceptionAdapter.runtime.apply(new AssertionError()));
	}

	@Test
	public void shouldNotAdaptMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Exception t = new FileNotFoundException("test");
		assertEquals(ad.apply(t), t);
		t = new IOException("test");
		assertEquals(ad.apply(t), t);
	}

	@Test
	public void shouldRun() {
		assertRte(() -> ExceptionAdapter.runtime.run(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> ExceptionAdapter.runtime.run(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.run(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGet() {
		assertEquals(ExceptionAdapter.runtime.get(() -> null), null);
		assertEquals(ExceptionAdapter.runtime.get(() -> "test"), "test");
		assertRte(() -> ExceptionAdapter.runtime.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class, () -> ExceptionAdapter.runtime.get(() -> {
			throw new InterruptedException();
		}));
		assertRte(() -> ExceptionAdapter.io.get(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGetBool() {
		assertFalse(ExceptionAdapter.runtime.getBool(() -> false));
		assertTrue(ExceptionAdapter.runtime.getBool(() -> true));
		assertRte(() -> ExceptionAdapter.runtime.getBool(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getBool(() -> {
				throw new InterruptedException();
			}));
		assertRte(() -> ExceptionAdapter.io.getBool(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGetByte() {
		assertEquals(ExceptionAdapter.runtime.getByte(() -> Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(ExceptionAdapter.runtime.getByte(() -> Byte.MAX_VALUE), Byte.MAX_VALUE);
		assertRte(() -> ExceptionAdapter.runtime.getByte(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getByte(() -> {
				throw new InterruptedException();
			}));
		assertRte(() -> ExceptionAdapter.io.getByte(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGetInt() {
		assertEquals(ExceptionAdapter.runtime.getInt(() -> Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(ExceptionAdapter.runtime.getInt(() -> Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertRte(() -> ExceptionAdapter.runtime.getInt(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getInt(() -> {
				throw new InterruptedException();
			}));
		assertRte(() -> ExceptionAdapter.io.getInt(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGetLong() {
		assertEquals(ExceptionAdapter.runtime.getLong(() -> Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(ExceptionAdapter.runtime.getLong(() -> Long.MAX_VALUE), Long.MAX_VALUE);
		assertRte(() -> ExceptionAdapter.runtime.getLong(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getLong(() -> {
				throw new InterruptedException();
			}));
		assertRte(() -> ExceptionAdapter.io.getLong(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldGetDouble() {
		assertEquals(ExceptionAdapter.runtime.getDouble(() -> Double.MIN_VALUE), Double.MIN_VALUE);
		assertEquals(ExceptionAdapter.runtime.getDouble(() -> Double.MAX_VALUE), Double.MAX_VALUE);
		assertRte(() -> ExceptionAdapter.runtime.getDouble(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getDouble(() -> {
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
	public void shouldAdaptRuntimeExceptions() {
		Exception t = new IOException("test");
		RuntimeException e = ExceptionAdapter.runtime.apply(t);
		assertNotEquals(e, t);
		t = new InterruptedException("test");
		e = ExceptionAdapter.runtime.apply(t);
		assertNotEquals(e, t);
		t = new Exception("test");
		e = ExceptionAdapter.runtime.apply(t);
		assertNotEquals(e, t);
		t = new IllegalArgumentException("test");
		e = ExceptionAdapter.runtime.apply(t);
		assertEquals(e, t);
		t = new RuntimeException("test");
		e = ExceptionAdapter.runtime.apply(t);
		assertEquals(e, t);
	}
}
