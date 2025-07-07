package ceri.log.util;

import static ceri.common.test.AssertUtil.assertIterable;
import org.junit.Test;
import ceri.common.function.Excepts.RuntimeCloseable;
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
		RuntimeCloseable c0 = () -> captor.accept(0);
		RuntimeCloseable c1 = () -> captor.accept(1);
		RuntimeCloseable c2 = () -> captor.accept(2);
		var tracker = CloseableTracker.of();
		tracker.add(c0);
		tracker.add(c1);
		tracker.add(c2);
		assertIterable(tracker.list(), c0, c1, c2);
	}

}
