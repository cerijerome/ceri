package ceri.x10.cm11a.entry;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Immutable;
import ceri.x10.command.House;

public class StatusBehavior {

	@Test
	public void shouldWriteAndReadToStreams() {
		LocalDateTime date = epochSeconds();
		Status status = new Status.Builder().date(date).build();
		Immutable bytes = ByteArray.Encoder.of().apply(status::encode).immutable();
		Status status2 = Status.decode(bytes.reader(0));
		assertThat(status, is(status2));
	}

	// TODO: redo
	@Test
	public void shouldObeyEqualsContract() {
		LocalDateTime date = epochSeconds();
		Status status1 = new Status.Builder().date(date).build();
		Status status2 = new Status.Builder().date(date).build();
		// ReadStatus status3 = new ReadStatus.Builder().date(new Date(date.getTime() +
		// 1000)).build();
		Status status4 = new Status.Builder().addressed(1).build();
		Status status5 = new Status.Builder().batteryTimer(1).build();
		Status status6 = new Status.Builder().dim(1).build();
		Status status7 = new Status.Builder().firmware(2).build();
		Status status8 = new Status.Builder().house(House.P).build();
		Status status9 = new Status.Builder().onOff(1).build();
		assertThat(status1, is(status1));
		assertThat(status1, is(status2));
		assertNotEquals(null, status1);
		assertNotEquals(status1, new Object());
		// assertThat(status1, not(status3));
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
