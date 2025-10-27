package ceri.common.except;

import static ceri.common.test.TestUtil.exerciseRecord;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import org.junit.Test;
import ceri.common.except.ExceptionTracker.Key;
import ceri.common.io.IoExceptions;
import ceri.common.test.Assert;

public class ExceptionTrackerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		exerciseRecord(new Key(IOException.class, "test"), Method::invoke);
	}

	@Test
	public void shouldAllowNullMessages() {
		var tracker = ExceptionTracker.of();
		Assert.no(tracker.add(null));
		Assert.yes(tracker.add(new IOException()));
		Assert.no(tracker.add(new IOException()));
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
		Assert.yes(tracker.add(e0));
		Assert.no(tracker.add(e0));
		Assert.no(tracker.add(e1));
		Assert.yes(tracker.add(e2));
		Assert.yes(tracker.add(e3));
		Assert.yes(tracker.add(e4));
		Assert.yes(tracker.add(e5));
		Assert.no(tracker.add(e0));
		Assert.equal(tracker.size(), 5);
	}

	@Test
	public void shouldClearExceptions() {
		var tracker = ExceptionTracker.of();
		Assert.yes(tracker.add(new IOException("test")));
		Assert.no(tracker.add(new IOException("test")));
		tracker.clear();
		Assert.yes(tracker.isEmpty());
		Assert.yes(tracker.add(new IOException("test")));
		Assert.no(tracker.isEmpty());
	}

	@Test
	public void shouldDropExcessiveExceptions() {
		var tracker = ExceptionTracker.of(2);
		Assert.equal(tracker.add(null), false);
		Assert.equal(tracker.add(new Exception("1")), true);
		Assert.equal(tracker.add(new Exception("2")), true);
		Assert.equal(tracker.add(new Exception("3")), false);
		Assert.equal(tracker.add(new Exception("2")), false);
	}

}
