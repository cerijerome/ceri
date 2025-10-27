package ceri.common.test;

import org.junit.Test;

public class TimeCollectorBehavior {

	@Test
	public void shouldCollectTimingData() {
		try (var sys = SystemIoCaptor.of()) {
			var tc = TimeCollector.millis(10, 1);
			tc.start();
			tc.end();
			tc.start();
			tc.end();
			tc.report(false);
			Assert.find(sys.out, "(?s)total.*mean.*median.*range");
			tc.report(true);
			Assert.find(sys.out, "(?s)total.*mean.*median.*range.*\\[.*\\]");
		}
	}

	@Test
	public void shouldDelay() {
		var tc = TimeCollector.micros(10, 1);
		tc.delayRemaining();
		tc.start();
		tc.delayPeriod();
		tc.delayRemaining();
		tc.delayRemaining(-1000);
		tc.end();
	}

}
