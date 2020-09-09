package ceri.x10.cm11a.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.util.Date;
import org.junit.Test;
import ceri.x10.type.House;

public class WriteStatusBehavior {

	@Test
	public void shouldObeyEqualsContract() {
		WriteStatus status1 = WriteStatus.DEFAULT;
		WriteStatus status2 = WriteStatus.builder().date(status1.date).build();
		WriteStatus status3 = WriteStatus.builder().date(status1.date.plusSeconds(1)).build();
		WriteStatus status4 = WriteStatus.builder().clearBatteryTimer(true).build();
		WriteStatus status5 = WriteStatus.builder().clearMonitoredStatus(true).build();
		WriteStatus status6 = WriteStatus.builder().purgeTimer(true).build();
		WriteStatus status7 = WriteStatus.builder().house(House.I).build();
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
