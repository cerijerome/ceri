package ceri.common.test;

import org.junit.Test;
import ceri.common.concurrent.Locker;

public class CycleRunnerBehavior {

	@Test
	public void shouldNotFailForNullCycle() {
		try (var cr = CycleRunner.of(Locker.of())) {
			cr.start(null);
		}
	}

	@Test
	public void shouldRunCycle() {
		var sync = CallSync.<Integer, Integer>function(0);
		try (var cr = CycleRunner.of(Locker.of())) {
			cr.start(i -> sync.apply(i));
			sync.await(0);
			sync.await(0);
			sync.await(-1);
		}
	}

}
