package ceri.x10.cm11a.protocol;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.date.DateUtil;
import ceri.x10.type.House;

public class ReadStatusBehavior {

	@Test
	public void shouldWriteAndReadToStreams() {
		LocalDateTime date = epochSeconds();
		ReadStatus status = new ReadStatus.Builder().date(date).build();
		Immutable bytes = ByteArray.Encoder.of().apply(status::encode).immutable();
		ReadStatus status2 = ReadStatus.decode(bytes.reader(0));
		assertThat(status, is(status2));
	}

	// TODO: redo
	@Test
	public void shouldObeyEqualsContract() {
		LocalDateTime date = epochSeconds();
		ReadStatus status1 = new ReadStatus.Builder().date(date).build();
		ReadStatus status2 = new ReadStatus.Builder().date(date).build();
		//ReadStatus status3 = new ReadStatus.Builder().date(new Date(date.getTime() + 1000)).build();
		ReadStatus status4 = new ReadStatus.Builder().addressed(1).build();
		ReadStatus status5 = new ReadStatus.Builder().batteryTimer(1).build();
		ReadStatus status6 = new ReadStatus.Builder().dim(1).build();
		ReadStatus status7 = new ReadStatus.Builder().firmware(2).build();
		ReadStatus status8 = new ReadStatus.Builder().house(House.P).build();
		ReadStatus status9 = new ReadStatus.Builder().onOff(1).build();
		assertThat(status1, is(status1));
		assertThat(status1, is(status2));
		assertNotEquals(null, status1);
		assertNotEquals(status1, new Object());
		//assertThat(status1, not(status3));
		assertThat(status1, not(status4));
		assertThat(status1, not(status5));
		assertThat(status1, not(status6));
		assertThat(status1, not(status7));
		assertThat(status1, not(status8));
		assertThat(status1, not(status9));
		assertThat(status1.hashCode(), is(status2.hashCode()));
		assertThat(status1.toString(), is(status2.toString()));
	}

	private static LocalDateTime epochSeconds() {
		return Instant.now().truncatedTo(SECONDS).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
}
