package ceri.common.exception;

import static ceri.common.exception.ExceptionAdapter.NULL;
import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.IoUtil;
import ceri.common.test.TestUtil;

public class ExceptionAdapterBehavior {

	@Test
	public void shouldNotAdaptMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Throwable t = new FileNotFoundException("test");
		assertThat(ad.apply(t), is(t));
		t = new IOException("test");
		assertThat(ad.apply(t), is(t));
	}

	@Test
	public void shouldReturnType() {
		assertThat(RUNTIME.get(() -> "test"), is("test"));
		assertThrown(RuntimeException.class, () -> RUNTIME.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.get(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnBoolean() {
		assertThat(RUNTIME.getBoolean(() -> false), is(false));
		assertThrown(RuntimeException.class, () -> RUNTIME.getBoolean(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.getBoolean(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnInt() {
		assertThat(RUNTIME.getInt(() -> Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThrown(RuntimeException.class, () -> RUNTIME.getInt(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeException.class, () -> IoUtil.IO_ADAPTER.getInt(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void shouldReturnLong() {
		assertThat(RUNTIME.getLong(() -> Long.MAX_VALUE), is(Long.MAX_VALUE));
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
		assertThat(e, is(not(t)));
		t = new Exception("test");
		e = ad.apply(t);
		assertThat(e, is(not(t)));
	}

	@Test
	public void shouldCreateAdapterFunctionFromClass() {
		TestUtil.assertThrown(() -> ExceptionAdapter.of(FileNotFoundException.class)); // no
																						// matching
		// con.
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class);
		Throwable t = new FileNotFoundException("test");
		assertThat(ad.apply(t), is(t));
		t = new IOException("test");
		assertThat(ad.apply(t), is(t));
		t = new InterruptedException("test");
		IOException e = ad.apply(t);
		assertThat(e, is(not(t)));
		t = new Exception("test");
		e = ad.apply(t);
		assertThat(e, is(not(t)));
	}

	@Test
	public void shouldAdaptNoExceptionsIfNull() {
		Throwable t = new FileNotFoundException("test");
		assertThat(NULL.apply(t), is(t));
		t = new IOException("test");
		assertThat(NULL.apply(t), is(t));
		t = new InterruptedException("test");
		assertThat(NULL.apply(t), is(t));
		t = new RuntimeException("test");
		assertThat(NULL.apply(t), is(t));
		t = new Exception("test");
		assertThat(NULL.apply(t), is(t));
	}

	@Test
	public void shouldAdaptRuntimeExceptions() {
		Throwable t = new IOException("test");
		RuntimeException e = RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new InterruptedException("test");
		e = RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new Exception("test");
		e = RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new IllegalArgumentException("test");
		e = RUNTIME.apply(t);
		assertThat(e, is(t));
		t = new RuntimeException("test");
		e = RUNTIME.apply(t);
		assertThat(e, is(t));
	}

}
