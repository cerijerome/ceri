package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ExceptionAdapterBehavior {

	@Test
	public void shouldNotAdaptMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class,
			IOException::new);
		Throwable t = new FileNotFoundException("test");
		assertThat(ad.apply(t), is(t));
		t = new IOException("test");
		assertThat(ad.apply(t), is(t));
	}

	@Test
	public void shouldAdaptNonMatchingTypes() {
		ExceptionAdapter<IOException> ad = ExceptionAdapter.of(IOException.class,
			IOException::new);
		Throwable t = new InterruptedException("test");
		IOException e = ad.apply(t);
		assertThat(e, is(not(t)));
		t = new Exception("test");
		e = ad.apply(t);
		assertThat(e, is(not(t)));
	}

	@Test
	public void shouldCreateAdapterFunctionFromClass() {
		TestUtil.assertThrown(() -> ExceptionAdapter.of(FileNotFoundException.class)); // no matching
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
		assertThat(ExceptionAdapter.NULL.apply(t), is(t));
		t = new IOException("test");
		assertThat(ExceptionAdapter.NULL.apply(t), is(t));
		t = new InterruptedException("test");
		assertThat(ExceptionAdapter.NULL.apply(t), is(t));
		t = new RuntimeException("test");
		assertThat(ExceptionAdapter.NULL.apply(t), is(t));
		t = new Exception("test");
		assertThat(ExceptionAdapter.NULL.apply(t), is(t));
	}

	@Test
	public void shouldAdaptRuntimeExceptions() {
		Throwable t = new IOException("test");
		RuntimeException e = ExceptionAdapter.RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new InterruptedException("test");
		e = ExceptionAdapter.RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new Exception("test");
		e = ExceptionAdapter.RUNTIME.apply(t);
		assertThat(e, is(not(t)));
		t = new IllegalArgumentException("test");
		e = ExceptionAdapter.RUNTIME.apply(t);
		assertThat(e, is(t));
		t = new RuntimeException("test");
		e = ExceptionAdapter.RUNTIME.apply(t);
		assertThat(e, is(t));
	}

}
