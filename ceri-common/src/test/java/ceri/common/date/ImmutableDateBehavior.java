package ceri.common.date;

import java.util.Date;
import org.junit.Test;

public class ImmutableDateBehavior {

	@SuppressWarnings("deprecation")
	@Test
	public void shouldAllowAllNonSetterMethods() {
		Date d = new ImmutableDate(new Date(0));
		d.after(d);
		d.before(d);
		d.clone();
		d.compareTo(d);
		d.getDate();
		d.getDay();
		d.getHours();
		d.getMinutes();
		d.getMonth();
		d.getSeconds();
		d.getTime();
		d.getTimezoneOffset();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetDate() {
		Date d = new ImmutableDate(new Date(0));
		d.setTime(0);
	}
	
}
