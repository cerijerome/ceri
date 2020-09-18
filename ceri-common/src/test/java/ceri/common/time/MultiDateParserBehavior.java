package ceri.common.time;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class MultiDateParserBehavior {

	@Test
	public void shouldReturnNullIfNoFormats() throws ParseException {
		MultiDateParser parser = new MultiDateParser();
		assertThat(parser.parse(""), is((Date)null));
		assertThat(parser.parse("1970"), is((Date)null));
	}

	@Test
	public void shouldThrowExceptionForInvalidDateFormat() {
		MultiDateParser parser = new MultiDateParser("yyyy", "MM-yyyy");
		TestUtil.assertThrown(() -> parser.parse("aaaa"));
	}

	@Test
	public void shouldFormatInDefaultTimeZoneIfTimeZoneNotSpecified() {
		MultiDateParser parser = new MultiDateParser("yyyy/MM/dd HH:mm:ss");
		Date date = new Date((System.currentTimeMillis() / 1000) * 1000); // round to 0 ms

		// Get values of current date/time in default timezone
		Calendar cal = Calendar.getInstance(); // default timezone
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1; // starts at 0
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		String calStr =
			String.format("%04d/%02d/%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
		String parsedStr = parser.format(date);
		assertThat(parsedStr, is(calStr));
	}

	@Test
	public void shouldParseInSequence() throws ParseException {
		TimeZone GMT = TimeZone.getTimeZone("GMT");
		MultiDateParser parser =
			new MultiDateParser(GMT, "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH", "yyyy/MM");
		Date date = parser.parse("1970/1/1 10:10");
		Calendar cal = Calendar.getInstance(GMT);
		cal.setTime(date);
		// verify 2nd format matched
		assertThat(cal.get(Calendar.YEAR), is(1970));
		assertThat(cal.get(Calendar.MONTH), is(0));
		assertThat(cal.get(Calendar.DATE), is(1));
		assertThat(cal.get(Calendar.HOUR), is(10));
		assertThat(cal.get(Calendar.MINUTE), is(0)); // shouldn't match minutes
		assertThat(cal.get(Calendar.SECOND), is(0)); // shouldn't match seconds
	}

	@Test
	public void shouldFormatAndParseSymmetrically() throws ParseException {
		MultiDateParser parser = new MultiDateParser("yyyy/MM/dd HH:mm:ss");
		long ms = (System.currentTimeMillis() / 1000) * 1000; // round to 0 millisec
		Date date = new Date(ms);
		String s1 = parser.format(date);
		Date parsedDate = parser.parse(s1);
		String s2 = parser.format(parsedDate);
		assertThat(parsedDate, is(date));
		assertThat(s1, is(s2));
	}

}
