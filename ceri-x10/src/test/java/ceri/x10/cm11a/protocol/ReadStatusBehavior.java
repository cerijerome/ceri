package ceri.x10.cm11a.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;
import ceri.common.io.ByteArrayDataInput;
import ceri.x10.type.House;

public class ReadStatusBehavior {

	@Test
	public void shouldWriteAndReadToStreams() throws IOException {
		Date date = new Date((System.currentTimeMillis() / 1000) * 1000);
		ReadStatus status = new ReadStatus.Builder().date(date).build();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(out);
		status.writeTo(data);
		ReadStatus status2 = ReadStatus.readFrom(new ByteArrayDataInput(out.toByteArray(), 0));
		assertThat(status, is(status2));
	}

	@Test
	public void shouldObeyEqualsContract() {
		Date date = new Date((System.currentTimeMillis() / 1000) * 1000);
		ReadStatus status1 = new ReadStatus.Builder().date(date).build();
		ReadStatus status2 = new ReadStatus.Builder().date(date).build();
		ReadStatus status3 = new ReadStatus.Builder().date(new Date(date.getTime() + 1000)).build();
		ReadStatus status4 = new ReadStatus.Builder().addressed(1).build();
		ReadStatus status5 = new ReadStatus.Builder().batteryTimer(1).build();
		ReadStatus status6 = new ReadStatus.Builder().dim(1).build();
		ReadStatus status7 = new ReadStatus.Builder().firmware(2).build();
		ReadStatus status8 = new ReadStatus.Builder().house(House.P).build();
		ReadStatus status9 = new ReadStatus.Builder().onOff(1).build();
		assertThat(status1, is(status1));
		assertThat(status1, is(status2));
		assertFalse(status1.equals(null));
		assertFalse(status1.equals(new Object()));
		assertThat(status1, not(status3));
		assertThat(status1, not(status4));
		assertThat(status1, not(status5));
		assertThat(status1, not(status6));
		assertThat(status1, not(status7));
		assertThat(status1, not(status8));
		assertThat(status1, not(status9));
		assertThat(status1.hashCode(), is(status2.hashCode()));
		assertThat(status1.toString(), is(status2.toString()));
	}

}
