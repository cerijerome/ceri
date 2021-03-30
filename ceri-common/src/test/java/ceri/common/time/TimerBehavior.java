package ceri.common.time;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.time.Timer.State;

public class TimerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Timer t0 = Timer.millis(1000);
		Timer t1 = Timer.millis(1000);
		Timer.Snapshot s0 = new Timer.Snapshot(t0, State.started, 100, 101, 99);
		Timer.Snapshot s1 = new Timer.Snapshot(t0, State.started, 100, 101, 99);
		Timer.Snapshot s2 = new Timer.Snapshot(t1, State.started, 100, 101, 99);
		Timer.Snapshot s3 = new Timer.Snapshot(t0, State.paused, 100, 101, 99);
		Timer.Snapshot s4 = new Timer.Snapshot(t0, State.started, 101, 101, 99);
		Timer.Snapshot s5 = new Timer.Snapshot(t0, State.started, 100, 100, 99);
		Timer.Snapshot s6 = new Timer.Snapshot(t0, State.started, 100, 101, 100);
		exerciseEquals(s0, s1);
		assertAllNotEqual(s0, s2, s3, s4, s5, s6);
	}

	@Test
	public void shouldSupportTimeUnits() {
		assertEquals(Timer.millis().unit, TimeUnit.MILLISECONDS);
		assertEquals(Timer.millis(1000).snapshot().unit(), TimeUnit.MILLISECONDS);
		assertEquals(Timer.micros().unit, TimeUnit.MICROSECONDS);
		assertEquals(Timer.micros(1000).snapshot().unit(), TimeUnit.MICROSECONDS);
		assertEquals(Timer.nanos().unit, TimeUnit.NANOSECONDS);
		assertEquals(Timer.nanos(1000).snapshot().unit(), TimeUnit.NANOSECONDS);
	}

	@Test
	public void shouldCalculateElapsedTime() {
		Timer.Snapshot s0 = Timer.nanos(100).start().snapshot();
		assertEquals(s0.elapsed(), s0.period() - s0.remaining);
	}

	@Test
	public void shouldDetermineIfExpired() {
		Timer t = Timer.millis(10000);
		assertFalse(t.snapshot().expired()); // false - not started
		t.stop();
		assertTrue(t.snapshot().expired()); // true - stopped
		t.start();
		assertFalse(t.snapshot().expired()); // false - still running
		t.stop();
		assertTrue(t.snapshot().expired()); // true - stopped after running
	}

	@Test
	public void shouldDetermineRemainingTime() {
		Timer t = Timer.millis(Integer.MAX_VALUE + 10000L);
		assertEquals(t.snapshot().remainingInt(), Integer.MAX_VALUE);
		assertEquals(Timer.millis(-1).snapshot().remainingInt(), 0);
		assertEquals(Timer.millis(0).snapshot().remainingInt(), 0);
	}

	@Test
	public void shouldDetermineRemainingIntTime() {
		Timer t = Timer.nanos(1000);
		Timer.Snapshot s0 = new Timer.Snapshot(t, State.started, 0, 0, Long.MIN_VALUE);
		Timer.Snapshot s1 = new Timer.Snapshot(t, State.started, 0, 0, Long.MAX_VALUE);
		Timer.Snapshot s2 = new Timer.Snapshot(t, State.started, 0, 0, 1000L);
		assertEquals(s0.remainingInt(), Integer.MIN_VALUE);
		assertEquals(s1.remainingInt(), Integer.MAX_VALUE);
		assertEquals(s2.remainingInt(), 1000);
	}

	@Test
	public void shouldApplyRemainingTime() throws Exception {
		CallSync.Accept<Long> consumer = CallSync.consumer(null, true);
		Timer.INFINITE.applyRemaining(consumer::accept);
		Timer.millis(0).applyRemaining(consumer::accept);
		consumer.assertNoCall();
		Timer.millis(Long.MAX_VALUE).applyRemaining(consumer::accept);
		assertTrue(consumer.awaitAuto() > 0);
	}

	@Test
	public void shouldApplyRemainingIntTime() throws Exception {
		CallSync.Accept<Integer> consumer = CallSync.consumer(null, true);
		Timer.INFINITE.applyRemainingInt(consumer::accept);
		Timer.millis(0).applyRemainingInt(consumer::accept);
		consumer.assertNoCall();
		Timer.millis(Long.MAX_VALUE).applyRemainingInt(consumer::accept);
		assertTrue(consumer.awaitAuto() > 0);
	}

	@Test
	public void shouldPauseIfRunning() {
		Timer t = Timer.millis(10000);
		assertFalse(t.pause()); // false - not started
		t.start();
		assertTrue(t.pause()); // true - was running
		t.stop();
		assertFalse(t.pause()); // false - stopped
	}

	@Test
	public void shouldResumeIfPaused() {
		Timer t = Timer.millis(10000);
		assertFalse(t.resume()); // false - not started
		t.start();
		assertFalse(t.resume()); // false - running
		t.pause();
		assertTrue(t.resume()); // true - was paused
		t.stop();
		assertFalse(t.resume()); // false - stopped
	}

}
