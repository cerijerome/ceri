package ceri.process.uptime;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestProcess;

public class UptimeBehavior {

	@Test
	public void shouldCalculateMillisecondUptime() throws IOException {
		assertUptime(" 03:14:20 up 1 min, 2 users, load average: 2.28, 1.29, 0.50 ", 0, 0, 1);
		assertUptime(" 04:12:29 up 59 min, 5 users, load average: 0.06, 0.08, 0.48", 0, 0, 59);
		assertUptime("05:14:09 up 2:01, 5 users, load average: 0.13, 0.10, 0.45", 0, 2, 1);
		assertUptime(" 14:34:44 up 18:49,  2 users,  load average: 0.00, 0.00, 0.00", 0, 18, 49);
		assertUptime("03:13:19 up 1 day, 0 min, 8 users, load average: 0.01, 0.04, 0.05", 1, 0, 0);
		assertUptime("04:13:19 up 1 day, 1:00, 8 users, load average: 0.02, 0.05, 0.21", 1, 1, 0);
		assertUptime("15:17  up 34 days, 12:28, 5 users, load averages: 1.31 1.35 1.56", 34, 12,
			28);
	}

	@Test
	public void shouldFailForInvalidOutput() {
		assertThrown(() -> uptime("").uptimeMs().parse());
	}
	
	private static void assertUptime(String output, int days, int hours, int minutes)
		throws IOException {
		assertEquals(uptime(output).uptimeMs().parse(), ms(days, hours, minutes));
	}

	private static long ms(int days, int hours, int minutes) {
		return ((days * 24 + hours) * 60 + minutes) * 60 * 1000L;
	}

	private static Uptime uptime(String output) {
		return Uptime.of(TestProcess.processor(output));
	}

}
