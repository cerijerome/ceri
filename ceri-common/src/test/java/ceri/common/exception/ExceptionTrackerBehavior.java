package ceri.common.exception;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseRecord;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import org.junit.Test;
import ceri.common.exception.ExceptionTracker.Key;
import ceri.common.io.IoExceptions;

public class ExceptionTrackerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseRecord(new Key(IOException.class, "test"), Method::invoke);
	}

	@Test
	public void shouldAllowNullMessages() {
		var tracker = ExceptionTracker.of();
		assertFalse(tracker.add(null));
		assertTrue(tracker.add(new IOException()));
		assertFalse(tracker.add(new IOException()));
	}

	@Test
	public void shouldMatchExactTypeAndMessageOnly() {
		var e0 = new IOException("test");
		var e1 = new IOException("test");
		var e2 = new IOException("Test");
		var e3 = new FileNotFoundException("test");
		var e4 = new IoExceptions.Runtime("test");
		var e5 = new IOException("test\0");
		var tracker = ExceptionTracker.of();
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
		var tracker = ExceptionTracker.of();
		assertTrue(tracker.add(new IOException("test")));
		assertFalse(tracker.add(new IOException("test")));
		tracker.clear();
		assertTrue(tracker.isEmpty());
		assertTrue(tracker.add(new IOException("test")));
		assertFalse(tracker.isEmpty());
	}

	@Test
	public void shouldDropExcessiveExceptions() {
		var tracker = ExceptionTracker.of(2);
		assertEquals(tracker.add(null), false);
		assertEquals(tracker.add(new Exception("1")), true);
		assertEquals(tracker.add(new Exception("2")), true);
		assertEquals(tracker.add(new Exception("3")), false);
		assertEquals(tracker.add(new Exception("2")), false);
	}

}
