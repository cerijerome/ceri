package ceri.x10.cm11a.entry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.x10.command.House;

public class ClockBehavior {

	@Test
	public void shouldObeyEqualsContract() {
		Clock status1 = Clock.of();
		Clock status2 = Clock.builder().date(status1.date).build();
		Clock status3 = Clock.builder().date(status1.date.plusSeconds(1)).build();
		Clock status4 = Clock.builder().clearBatteryTimer(true).build();
		Clock status5 = Clock.builder().clearMonitoredStatus(true).build();
		Clock status6 = Clock.builder().purgeTimer(true).build();
		Clock status7 = Clock.builder().house(House.I).build();
		assertThat(status1, is(status1));
		assertThat(status1, is(status2));
		assertNotEquals(null, status1);
		assertNotEquals(status1, new Object());
		assertThat(status1, not(status3));
		assertThat(status1, not(status4));
		assertThat(status1, not(status5));
		assertThat(status1, not(status6));
		assertThat(status1, not(status7));
		assertThat(status1.hashCode(), is(status2.hashCode()));
		assertThat(status1.toString(), is(status2.toString()));
	}

}
