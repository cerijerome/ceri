package ceri.common.time;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * An immutable extension to TimeZone. UnsupportedOperationException is thrown for mutator methods.
 */
public class ImmutableTimeZone extends TimeZone {
	public static final TimeZone UTC = new ImmutableTimeZone(TimeZone.getTimeZone("UTC"));
	private static final long serialVersionUID = -3596503137048104086L;
	private final TimeZone timeZone;

	public static TimeZone getDefault() {
		return new ImmutableTimeZone(TimeZone.getDefault());
	}

	public ImmutableTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	public Object clone() {
		return new ImmutableTimeZone(timeZone);
	}

	@Override
	public boolean equals(Object obj) {
		return timeZone.equals(obj);
	}

	@Override
	public String getDisplayName(boolean daylight, int style, Locale locale) {
		return timeZone.getDisplayName(daylight, style, locale);
	}

	@Override
	public int getDSTSavings() {
		return timeZone.getDSTSavings();
	}

	@Override
	public String getID() {
		return timeZone.getID();
	}

	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
		return timeZone.getOffset(era, year, month, day, dayOfWeek, milliseconds);
	}

	@Override
	public int getOffset(long date) {
		return timeZone.getOffset(date);
	}

	@Override
	public int getRawOffset() {
		return timeZone.getRawOffset();
	}

	@Override
	public int hashCode() {
		return timeZone.hashCode();
	}

	@Override
	public boolean hasSameRules(TimeZone other) {
		return timeZone.hasSameRules(other);
	}

	@Override
	public boolean inDaylightTime(Date date) {
		return timeZone.inDaylightTime(date);
	}

	@Override
	public void setID(String ID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRawOffset(int offsetMillis) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return timeZone.toString();
	}

	@Override
	public boolean useDaylightTime() {
		return timeZone.useDaylightTime();
	}

}
