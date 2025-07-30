package ceri.log.util;

import static ceri.common.test.AssertUtil.assertOrdered;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Captor;

public class CloseableTrackerBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseInReverseOrder() {
		var captor = Captor.ofInt();
		var tracker = CloseableTracker.of();
		tracker.add(() -> captor.accept(0));
		tracker.add(() -> captor.accept(1));
		tracker.add(() -> captor.accept(2));
		tracker.close();
		captor.verifyInt(2, 1, 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideCreatedObjects() {
		var captor = Captor.ofInt();
		Functions.Closeable c0 = () -> captor.accept(0);
		Functions.Closeable c1 = () -> captor.accept(1);
		Functions.Closeable c2 = () -> captor.accept(2);
		var tracker = CloseableTracker.of();
		tracker.add(c0);
		tracker.add(c1);
		tracker.add(c2);
		assertOrdered(tracker.list(), c0, c1, c2);
	}

}
