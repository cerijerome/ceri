package ceri.common.time;

import static ceri.common.time.TimeSupplier.millis;
import static ceri.common.time.TimeSupplier.nanos;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;
import ceri.common.test.Testing;
import ceri.common.time.Timer.State;

public class TimerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Timer t0 = Timer.of(1000, millis);
		Timer t1 = Timer.of(1000, MILLISECONDS);
		Timer.Snapshot s0 = new Timer.Snapshot(t0, State.started, 100, 101, 99);
		Timer.Snapshot s1 = new Timer.Snapshot(t0, State.started, 100, 101, 99);
		Timer.Snapshot s2 = new Timer.Snapshot(t1, State.started, 100, 101, 99);
		Timer.Snapshot s3 = new Timer.Snapshot(t0, State.paused, 100, 101, 99);
		Timer.Snapshot s4 = new Timer.Snapshot(t0, State.started, 101, 101, 99);
		Timer.Snapshot s5 = new Timer.Snapshot(t0, State.started, 100, 100, 99);
		Timer.Snapshot s6 = new Timer.Snapshot(t0, State.started, 100, 101, 100);
		Testing.exerciseEquals(s0, s1);
		Assert.notEqualAll(s0, s2, s3, s4, s5, s6);
	}

	@Test
	public void shouldProvideInfiniteTimers() {
		Assert.equal(Timer.INFINITE.isInfinite(), true);
		Assert.equal(Timer.infinite(SECONDS).isInfinite(), true);
		Assert.equal(Timer.infinite(nanos).isInfinite(), true);
		Assert.equal(Timer.of(0, nanos).isInfinite(), false);
		Assert.equal(Timer.ZERO.isInfinite(), false);
	}

	@Test
	public void shouldNotApplyInfiniteTimerRemainder() {
		var captor = Captor.ofInt();
		Timer.INFINITE.applyRemainingInt(captor::accept);
		Timer.infinite(nanos).applyRemainingInt(captor::accept);
		captor.verifyInt();
	}

	@Test
	public void shouldProvideInfiniteTimerSnapshot() {
		Assert.equal(Timer.INFINITE.snapshot().expired(), false);
		Assert.equal(Timer.INFINITE.snapshot().remaining(), 0L);
	}

	@Test
	public void shouldCalculateElapsedTime() {
		Timer.Snapshot s0 = Timer.of(100, nanos).start().snapshot();
		Assert.equal(s0.elapsed(), s0.period() - s0.remaining());
	}

	@Test
	public void shouldDetermineIfExpired() {
		Timer t = Timer.of(10000, millis);
		Assert.no(t.snapshot().expired()); // false - not started
		t.stop();
		Assert.yes(t.snapshot().expired()); // true - stopped
		t.start();
		Assert.no(t.snapshot().expired()); // false - still running
		t.stop();
		Assert.yes(t.snapshot().expired()); // true - stopped after running
	}

	@Test
	public void shouldDetermineRemainingTime() {
		Timer t = Timer.of(Integer.MAX_VALUE + 10000L, millis);
		Assert.equal(t.snapshot().remainingInt(), Integer.MAX_VALUE);
		Assert.equal(Timer.millis(-1).snapshot().remainingInt(), 0);
		Assert.equal(Timer.of(0, millis).snapshot().remainingInt(), 0);
	}

	@Test
	public void shouldDetermineRemainingIntTime() {
		Timer t = Timer.nanos(1000);
		Timer.Snapshot s0 = new Timer.Snapshot(t, State.started, 0, 0, Long.MIN_VALUE);
		Timer.Snapshot s1 = new Timer.Snapshot(t, State.started, 0, 0, Long.MAX_VALUE);
		Timer.Snapshot s2 = new Timer.Snapshot(t, State.started, 0, 0, 1000L);
		Assert.equal(s0.remainingInt(), Integer.MIN_VALUE);
		Assert.equal(s1.remainingInt(), Integer.MAX_VALUE);
		Assert.equal(s2.remainingInt(), 1000);
	}

	@Test
	public void shouldApplyRemainingTime() throws Exception {
		CallSync.Consumer<Long> consumer = CallSync.consumer(null, true);
		Timer.INFINITE.applyRemaining(consumer::accept);
		Timer.of(0, millis).applyRemaining(consumer::accept);
		consumer.assertCalls(0);
		Timer.of(Long.MAX_VALUE, millis).applyRemaining(consumer::accept);
		Assert.yes(consumer.awaitAuto() > 0);
	}

	@Test
	public void shouldApplyRemainingIntTime() throws Exception {
		CallSync.Consumer<Integer> consumer = CallSync.consumer(null, true);
		Timer.INFINITE.applyRemainingInt(consumer::accept);
		Timer.of(0, millis).applyRemainingInt(consumer::accept);
		consumer.assertCalls(0);
		Timer.of(Long.MAX_VALUE, millis).applyRemainingInt(consumer::accept);
		Assert.yes(consumer.awaitAuto() > 0);
	}

	@Test
	public void shouldPauseIfRunning() {
		Timer t = Timer.secs(10000);
		Assert.no(t.pause()); // false - not started
		t.start();
		Assert.yes(t.pause()); // true - was running
		t.stop();
		Assert.no(t.pause()); // false - stopped
	}

	@Test
	public void shouldResumeIfPaused() {
		Timer t = Timer.micros(10000);
		Assert.no(t.resume()); // false - not started
		t.start();
		Assert.no(t.resume()); // false - running
		t.pause();
		Assert.yes(t.resume()); // true - was paused
		t.stop();
		Assert.no(t.resume()); // false - stopped
	}

}
