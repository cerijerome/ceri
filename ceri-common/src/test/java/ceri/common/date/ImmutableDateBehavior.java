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
	public void shouldNotAllowSetTime() {
		new ImmutableDate(new Date(0)).setTime(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetDate() {
		new ImmutableDate(new Date(0)).setDate(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetHours() {
		new ImmutableDate(new Date(0)).setHours(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetMinutes() {
		new ImmutableDate(new Date(0)).setMinutes(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetMonth() {
		new ImmutableDate(new Date(0)).setMonth(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetSeconds() {
		new ImmutableDate(new Date(0)).setSeconds(0);
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotAllowSetYear() {
		new ImmutableDate(new Date(0)).setYear(0);
	}
	
}