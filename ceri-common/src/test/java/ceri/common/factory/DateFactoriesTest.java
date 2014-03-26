package ceri.common.factory;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class DateFactoriesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(DateFactories.class);
	}
	
	@Test
	public void testNullConversions() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		assertNull(DateFactories.fromString("y", tz).create(null));
		assertNull(DateFactories.toString("y", tz).create(null));
	}

	@Test
	public void testLongConversions() {
		assertThat(DateFactories.FROM_LONG.create(Long.MAX_VALUE), is(new Date(Long.MAX_VALUE)));
		assertThat(DateFactories.FROM_LONG.create(Long.MIN_VALUE), is(new Date(Long.MIN_VALUE)));
		assertThat(DateFactories.FROM_LONG.create(0L), is(new Date(0)));
		assertNull(DateFactories.FROM_LONG.create(null));
		assertThat(DateFactories.TO_LONG.create(new Date(Long.MAX_VALUE)), is(Long.MAX_VALUE));
		assertThat(DateFactories.TO_LONG.create(new Date(Long.MIN_VALUE)), is(Long.MIN_VALUE));
		assertThat(DateFactories.TO_LONG.create(new Date(0)), is(0L));
		assertNull(DateFactories.TO_LONG.create(null));
	}

	@Test
	public void testDefaultStringConversion() {
		long offset = TimeZone.getDefault().getOffset(0);
		assertThat(DateFactories.FROM_STRING.create("1970-01-01"), is(new Date(-offset)));
		assertFailFrom(DateFactories.FROM_STRING, "xxx");
		assertThat(DateFactories.TO_STRING.create(new Date(-offset)), is("1970-01-01"));
		String s = "2013-12-31";
		assertThat(DateFactories.TO_STRING.create(DateFactories.FROM_STRING.create(s)), is(s));
	}

	@Test
	public void testStringFormatConversion() {
		long offset = TimeZone.getDefault().getOffset(0);
		assertThat(DateFactories.fromString("dd-MM-yyyy").create("01-01-1970"),
			is(new Date(-offset)));
		assertNull(DateFactories.fromString("y").create(null));
		assertFailFrom(DateFactories.fromString("dd-MM-yyyy"), "");
		assertThat(DateFactories.toString("dd-yyyy-MM").create(new Date(-offset)), is("01-1970-01"));
		String s = "12-31-2013";
		assertThat(DateFactories.toString("mm-dd-yyyy").create(
			DateFactories.fromString("mm-dd-yyyy").create(s)), is(s));
		assertNull(DateFactories.toString("y").create(null));
	}

	@Test
	public void testTimeZoneStringFormatConversion() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		assertThat(DateFactories.fromString("dd-MM-yyyy", tz).create("01-01-1970"), is(new Date(0)));
		assertFailFrom(DateFactories.fromString("dd-MM-yyyy", tz), "");
		assertThat(DateFactories.toString("dd-MM-yyyy", tz).create(new Date(0)), is("01-01-1970"));
		String s = "31-12-2013";
		assertThat(DateFactories.toString("dd-mm-yyyy", tz).create(
			DateFactories.fromString("dd-mm-yyyy", tz).create(s)), is(s));
	}

	private static <F> void assertFailFrom(final Factory<Date, F> factory, final F from) {
		TestUtil.assertException(FactoryException.class, () -> factory.create(from));
	}

}
