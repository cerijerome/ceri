package ceri.common.util;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.util.BasicUtil.uncheckedCast;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.junit.Test;
import ceri.common.function.ExceptionConsumer;
import ceri.common.util.Timer.State;

public class TimerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Timer t0 = Timer.of(1000);
		Timer t1 = Timer.of(1000);
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
	public void shouldDetermineIfExpired() {
		Timer t = Timer.of(10000);
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
		Timer t = Timer.of(Integer.MAX_VALUE + 10000L);
		assertThat(t.snapshot().remainingInt(), is(Integer.MAX_VALUE));
		assertThat(Timer.of(-1).snapshot().remainingInt(), is(0));
		assertThat(Timer.of(0).snapshot().remainingInt(), is(0));
	}

	@Test
	public void shouldApplyRemainingTime() throws Exception {
		ExceptionConsumer<Exception, Integer> consumer =
			uncheckedCast(mock(ExceptionConsumer.class));
		Timer.INFINITE.applyRemaining(consumer);
		Timer.of(0).applyRemaining(consumer);
		verifyNoMoreInteractions(consumer);
	}

	@Test
	public void shouldPauseIfRunning() {
		Timer t = Timer.of(10000);
		assertFalse(t.pause()); // false - not started
		t.start();
		assertTrue(t.pause()); // true - was running
		t.stop();
		assertFalse(t.pause()); // false - stopped
	}

	@Test
	public void shouldResumeIfPaused() {
		Timer t = Timer.of(10000);
		assertFalse(t.resume()); // false - not started
		t.start();
		assertFalse(t.resume()); // false - running
		t.pause();
		assertTrue(t.resume()); // true - was paused
		t.stop();
		assertFalse(t.resume()); // false - stopped
	}

}
