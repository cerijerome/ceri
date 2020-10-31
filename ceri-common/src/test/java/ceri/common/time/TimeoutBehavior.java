package ceri.common.time;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;

public class TimeoutBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Timeout t = Timeout.of(7, SECONDS);
		Timeout eq0 = Timeout.of(7, SECONDS);
		Timeout ne0 = Timeout.of(8, SECONDS);
		Timeout ne1 = Timeout.of(7, MILLISECONDS);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldConvertUnits() {
		Timeout t = Timeout.of(111, SECONDS);
		assertSame(t.convert(SECONDS), t);
		assertEquals(t.convert(MILLISECONDS), Timeout.of(111000, MILLISECONDS));
		assertEquals(t.convert(MINUTES), Timeout.of(1, MINUTES));
	}

	@Test
	public void shouldApply() throws InterruptedException {
		Timeout t = Timeout.of(1, MILLISECONDS);
		Lock lock = new ReentrantLock();
		ConcurrentUtil.execute(lock, () -> {
			Condition c = lock.newCondition();
			assertFalse(t.applyTo(c::await));
		});
	}
}
