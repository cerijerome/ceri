package ceri.common.date;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.TimeZone;
import org.junit.Test;

public class DateUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(DateUtil.class);
	}

	@Test
	public void testFormatIso() {
		assertThat(DateUtil.formatIso(0), is("1970-01-01T00:00:00Z"));
		assertThat(DateUtil.formatIso(-1000), is("1969-12-31T23:59:59Z"));
		assertThat(DateUtil.formatIso(-999), is("1969-12-31T23:59:59Z"));
		assertThat(DateUtil.formatIso(999), is("1970-01-01T00:00:00Z"));
		assertThat(DateUtil.formatIso(1000), is("1970-01-01T00:00:01Z"));
	}

	@Test
	public void testFormatLocalIso() {
		int offset = TimeZone.getDefault().getOffset(0);
		if (offset < 0) assertTrue(DateUtil.formatLocalIso(0).startsWith("1969-12-31"));
		else assertTrue(DateUtil.formatLocalIso(0).startsWith("1970-01-01"));
	}

}
