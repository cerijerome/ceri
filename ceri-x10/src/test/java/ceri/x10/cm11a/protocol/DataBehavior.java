package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.time.DateUtil.UTC_EPOCH;
import static ceri.x10.command.House.L;
import static ceri.x10.command.Unit._12;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.time.LocalDate;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Encoder;
import ceri.x10.command.Unit;

public class DataBehavior {

	@Test
	public void testEncode() {
		assertThat(Data.encode(null, (Unit) null), is(0));
		assertThat(Data.encode(null, _12), is(0x0b));
		assertThat(Data.encode(L, (Unit) null), is(0xb0));
	}

	@Test
	public void testShortChecksum() {
		assertThat(Data.shortChecksum(0xffff), is(0xfe));
		assertThat(Data.shortChecksum(0x9876), is(0x0e));
		assertThat(Data.shortChecksum(0x1234), is(0x46));
	}

	@Test
	public void testWriteDateTo() {
		Encoder enc = ByteArray.Encoder.of();
		Data.writeDateTo(UTC_EPOCH, enc);
		assertArray(enc.bytes(), 0, 0, 0, 1, 4);
		enc.reset();
		Data.writeDateTo(UTC_EPOCH.plusDays(3), enc);
		assertArray(enc.bytes(), 0, 0, 0, 4, 0x40);
	}

	@Test
	public void testNearestDate() {
		assertThat(Data.nearestDate(LocalDate.of(1970, 1, 1), 90), is(LocalDate.of(1970, 3, 31)));
		assertThat(Data.nearestDate(LocalDate.of(1970, 1, 1), 200), is(LocalDate.of(1969, 7, 19)));
		assertThat(Data.nearestDate(LocalDate.of(1970, 12, 1), 100), is(LocalDate.of(1971, 4, 10)));
	}

}
