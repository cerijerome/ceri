package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
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
		assertFalse(tracker.add(null));
		assertTrue(tracker.add(new IOException()));
		assertFalse(tracker.add(new IOException()));
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
		assertTrue(tracker.add(e0));
		assertFalse(tracker.add(e0));
		assertFalse(tracker.add(e1));
		assertTrue(tracker.add(e2));
		assertTrue(tracker.add(e3));
		assertTrue(tracker.add(e4));
		assertTrue(tracker.add(e5));
		assertFalse(tracker.add(e0));
		assertEquals(tracker.size(), 5);
	}

	@Test
	public void shouldClearExceptions() {
		ExceptionTracker tracker = ExceptionTracker.of();
		assertTrue(tracker.add(new IOException("test")));
		assertFalse(tracker.add(new IOException("test")));
		tracker.clear();
		assertTrue(tracker.isEmpty());
		assertTrue(tracker.add(new IOException("test")));
		assertFalse(tracker.isEmpty());
	}

}
