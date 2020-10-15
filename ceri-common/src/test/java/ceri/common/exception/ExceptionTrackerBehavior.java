package ceri.common.exception;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.exception.ExceptionTracker.Key;
import ceri.common.io.RuntimeIoException;

public class ExceptionTrackerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Key k0 = new Key(IOException.class, "test");
		Key k1 = new Key(IOException.class, "test");
		Key k2 = new Key(FileNotFoundException.class, "test");
		Key k3 = new Key(IOException.class, null);
		Key k4 = new Key(null, "test");
		exerciseEquals(k0, k1);
		assertAllNotEqual(k0, k2, k3, k4);
	}

	@Test
	public void shouldAllowNullMessages() {
		ExceptionTracker tracker = ExceptionTracker.of();
		assertThat(tracker.add(null), is(false));
		assertThat(tracker.add(new IOException()), is(true));
		assertThat(tracker.add(new IOException()), is(false));
	}

	@Test
	public void shouldMatchExactTypeAndMessageOnly() {
		IOException e0 = new IOException("test");
		IOException e1 = new IOException("test");
		IOException e2 = new IOException("Test");
		IOException e3 = new FileNotFoundException("test");
		Exception e4 = new RuntimeIoException("test");
		IOException e5 = new IOException("test\0");
		ExceptionTracker tracker = ExceptionTracker.of();
		assertThat(tracker.add(e0), is(true));
		assertThat(tracker.add(e0), is(false));
		assertThat(tracker.add(e1), is(false));
		assertThat(tracker.add(e2), is(true));
		assertThat(tracker.add(e3), is(true));
		assertThat(tracker.add(e4), is(true));
		assertThat(tracker.add(e5), is(true));
		assertThat(tracker.add(e0), is(false));
		assertThat(tracker.size(), is(5));
	}

	@Test
	public void shouldClearExceptions() {
		ExceptionTracker tracker = ExceptionTracker.of();
		assertThat(tracker.add(new IOException("test")), is(true));
		assertThat(tracker.add(new IOException("test")), is(false));
		tracker.clear();
		assertThat(tracker.isEmpty(), is(true));
		assertThat(tracker.add(new IOException("test")), is(true));
		assertThat(tracker.isEmpty(), is(false));
	}

}
