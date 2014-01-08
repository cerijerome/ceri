package ceri.common.date;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;

public class ImmutableTimeZoneBehavior {

	@Test
	public void shouldAllowAllNonSetterMethods() {
		TimeZone tz = new ImmutableTimeZone(TimeZone.getTimeZone("GMT"));
		tz.equals(tz);
		tz.clone();
		tz.hashCode();
		tz.toString();
		tz.getDisplayName();
		tz.getDisplayName(Locale.US);
		tz.getDisplayName(true, TimeZone.LONG);
		tz.getDisplayName(false, TimeZone.SHORT, Locale.CHINA);
		tz.getDSTSavings();
		tz.getID();
		tz.getOffset(0);
		tz.getOffset(1, 1, 1, 1, 1, 1);
		tz.getRawOffset();
		tz.hasSameRules(tz);
		tz.inDaylightTime(new Date(0));
		tz.observesDaylightTime();
		tz.useDaylightTime();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetID() {
		TimeZone tz = new ImmutableTimeZone(TimeZone.getTimeZone("GMT"));
		tz.setID("GMT");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetRawOffset() {
		TimeZone tz = new ImmutableTimeZone(TimeZone.getTimeZone("GMT"));
		tz.setRawOffset(0);
	}
	
}
