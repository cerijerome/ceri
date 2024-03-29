package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.time.DateUtil.UTC_EPOCH;
import static ceri.x10.command.House.L;
import static ceri.x10.command.Unit._12;
import java.time.LocalDate;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Encoder;
import ceri.x10.command.Unit;

public class DataBehavior {

	@Test
	public void testEncode() {
		assertEquals(Data.encode(null, (Unit) null), 0);
		assertEquals(Data.encode(null, _12), 0x0b);
		assertEquals(Data.encode(L, (Unit) null), 0xb0);
	}

	@Test
	public void testShortChecksum() {
		assertEquals(Data.shortChecksum(0xffff), 0xfe);
		assertEquals(Data.shortChecksum(0x9876), 0x0e);
		assertEquals(Data.shortChecksum(0x1234), 0x46);
	}

	@Test
	public void testWriteDateTo() {
		Encoder enc = ByteArray.Encoder.of();
		Data.writeDateTo(UTC_EPOCH, enc);
		assertArray(enc.bytes(), 0, 0, 0, 1, 4);
		enc.reset();
		Data.writeDateTo(UTC_EPOCH.plusHours(1), enc);
		assertArray(enc.bytes(), 0, 60, 0, 1, 4);
		enc.reset();
		Data.writeDateTo(UTC_EPOCH.plusDays(3), enc);
		assertArray(enc.bytes(), 0, 0, 0, 4, 0x40);
	}

	@Test
	public void testNearestDate() {
		assertEquals(Data.nearestDate(LocalDate.of(1970, 1, 1), 90), LocalDate.of(1970, 3, 31));
		assertEquals(Data.nearestDate(LocalDate.of(1970, 1, 1), 200), LocalDate.of(1969, 7, 19));
		assertEquals(Data.nearestDate(LocalDate.of(1970, 12, 1), 100), LocalDate.of(1971, 4, 10));
	}

}
