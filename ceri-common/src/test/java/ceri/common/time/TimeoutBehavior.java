package ceri.common.time;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertSame;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.TestUtil;

public class TimeoutBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Timeout t = Timeout.of(7, SECONDS);
		Timeout eq0 = Timeout.of(7, SECONDS);
		Timeout ne0 = Timeout.of(8, SECONDS);
		Timeout ne1 = Timeout.of(7, MILLISECONDS);
		Timeout ne2 = Timeout.NULL;
		Timeout ne3 = Timeout.ZERO;
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldConvertUnits() {
		Timeout t = Timeout.of(111, SECONDS);
		assertSame(t.convert(SECONDS), t);
		assertEquals(t.convert(MILLISECONDS), Timeout.millis(111000));
		assertEquals(t.convert(MINUTES), Timeout.of(1, MINUTES));
		assertEquals(Timeout.NULL.convert(MINUTES), Timeout.NULL);
	}

	@Test
	public void shouldApply() throws InterruptedException {
		Timeout t = Timeout.millis(1);
		Lock lock = new ReentrantLock();
		ConcurrentUtil.lockedRun(lock, () -> {
			Condition c = lock.newCondition();
			assertFalse(t.applyTo(c::await));
			assertNull(Timeout.NULL.applyTo(c::await));
		});
	}

	@Test
	public void shouldAccept() {
		Timeout t = Timeout.millis(1);
		try (var exec =
			new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1))) {
			t.acceptBy(exec::setKeepAliveTime);
			Timeout.NULL.acceptBy(exec::setKeepAliveTime);
		}
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(Timeout.NULL.toString(), "null");
		assertEquals(Timeout.micros(666).toString(), "666µs");
		assertEquals(Timeout.nanos(333).toString(), "333ns");
	}

}
