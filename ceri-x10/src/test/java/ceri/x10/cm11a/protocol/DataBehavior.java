package ceri.x10.cm11a.protocol;

import java.time.LocalDate;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.test.Assert;
import ceri.common.time.Dates;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class DataBehavior {

	@Test
	public void testEncode() {
		Assert.equal(X10Data.encode(null, (Unit) null), 0);
		Assert.equal(X10Data.encode(null, Unit._12), 0x0b);
		Assert.equal(X10Data.encode(House.L, (Unit) null), 0xb0);
	}

	@Test
	public void testShortChecksum() {
		Assert.equal(X10Data.shortChecksum(0xffff), 0xfe);
		Assert.equal(X10Data.shortChecksum(0x9876), 0x0e);
		Assert.equal(X10Data.shortChecksum(0x1234), 0x46);
	}

	@Test
	public void testWriteDateTo() {
		var enc = ByteArray.Encoder.of();
		X10Data.writeDateTo(Dates.UTC_EPOCH, enc);
		Assert.array(enc.bytes(), 0, 0, 0, 1, 4);
		enc.reset();
		X10Data.writeDateTo(Dates.UTC_EPOCH.plusHours(1), enc);
		Assert.array(enc.bytes(), 0, 60, 0, 1, 4);
		enc.reset();
		X10Data.writeDateTo(Dates.UTC_EPOCH.plusDays(3), enc);
		Assert.array(enc.bytes(), 0, 0, 0, 4, 0x40);
	}

	@Test
	public void testNearestDate() {
		Assert.equal(X10Data.nearestDate(LocalDate.of(1970, 1, 1), 90), LocalDate.of(1970, 3, 31));
		Assert.equal(X10Data.nearestDate(LocalDate.of(1970, 1, 1), 200), LocalDate.of(1969, 7, 19));
		Assert.equal(X10Data.nearestDate(LocalDate.of(1970, 12, 1), 100), LocalDate.of(1971, 4, 10));
	}
}
