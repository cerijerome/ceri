package ceri.common.util;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.util.BasicUtil.uncheckedCast;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionLongConsumer;
import ceri.common.util.Timer.State;

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
		assertThat(Timer.micros(1000).unit, is(TimeUnit.MICROSECONDS));
		assertThat(Timer.micros(1000).snapshot().unit(), is(TimeUnit.MICROSECONDS));
		assertThat(Timer.nanos(1000).unit, is(TimeUnit.NANOSECONDS));
		assertThat(Timer.nanos(1000).snapshot().unit(), is(TimeUnit.NANOSECONDS));
	}

	@Test
	public void shouldCalculateElapsedTime() {
		Timer.Snapshot s0 = Timer.nanos(100).start().snapshot();
		assertThat(s0.elapsed(), is(s0.period() - s0.remaining));
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
		assertThat(t.snapshot().remainingInt(), is(Integer.MAX_VALUE));
		assertThat(Timer.millis(-1).snapshot().remainingInt(), is(0));
		assertThat(Timer.millis(0).snapshot().remainingInt(), is(0));
	}

	@Test
	public void shouldDetermineRemainingIntTime() {
		Timer t = Timer.nanos(1000);
		Timer.Snapshot s0 = new Timer.Snapshot(t, State.started, 0, 0, Long.MIN_VALUE);
		Timer.Snapshot s1 = new Timer.Snapshot(t, State.started, 0, 0, Long.MAX_VALUE);
		Timer.Snapshot s2 = new Timer.Snapshot(t, State.started, 0, 0, 1000L);
		assertThat(s0.remainingInt(), is(Integer.MIN_VALUE));
		assertThat(s1.remainingInt(), is(Integer.MAX_VALUE));
		assertThat(s2.remainingInt(), is(1000));
	}

	@Test
	public void shouldApplyRemainingTime() throws Exception {
		ExceptionLongConsumer<Exception> consumer =
			uncheckedCast(mock(ExceptionLongConsumer.class));
		Timer.INFINITE.applyRemaining(consumer);
		Timer.millis(0).applyRemaining(consumer);
		verifyNoMoreInteractions(consumer);
		Timer.millis(Long.MAX_VALUE).applyRemaining(consumer);
		verify(consumer).accept(anyLong());
	}

	@Test
	public void shouldApplyRemainingIntTime() throws Exception {
		ExceptionIntConsumer<Exception> consumer = uncheckedCast(mock(ExceptionIntConsumer.class));
		Timer.INFINITE.applyRemainingInt(consumer);
		Timer.millis(0).applyRemainingInt(consumer);
		verifyNoMoreInteractions(consumer);
		Timer.millis(Long.MAX_VALUE).applyRemainingInt(consumer);
		verify(consumer).accept(anyInt());
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
