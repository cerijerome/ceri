package ceri.x10.cm11a.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.util.Date;
import org.junit.Test;
import ceri.x10.type.House;

public class WriteStatusBehavior {

	@Test
	public void shouldObeyEqualsContract() {
		WriteStatus status1 = WriteStatus.createDefault();
		WriteStatus status2 = new WriteStatus.Builder().date(status1.date).build();
		WriteStatus status3 =
			new WriteStatus.Builder().date(new Date(status1.date.getTime() + 1000)).build();
		WriteStatus status4 = new WriteStatus.Builder().clearBatteryTimer(true).build();
		WriteStatus status5 = new WriteStatus.Builder().clearMonitoredStatus(true).build();
		WriteStatus status6 = new WriteStatus.Builder().purgeTimer(true).build();
		WriteStatus status7 = new WriteStatus.Builder().house(House.I).build();
		assertThat(status1, is(status1));
		assertThat(status1, is(status2));
		assertFalse(status1.equals(null));
		assertFalse(status1.equals(new Object()));
		assertThat(status1, not(status3));
		assertThat(status1, not(status4));
		assertThat(status1, not(status5));
		assertThat(status1, not(status6));
		assertThat(status1, not(status7));
		assertThat(status1.hashCode(), is(status2.hashCode()));
		assertThat(status1.toString(), is(status2.toString()));
	}

}
