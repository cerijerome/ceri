package ceri.common.factory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Factories converting between dates.
 */
public class DateFactories {
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final Factory<Date, Long> FROM_LONG = new Factory.Base<Date, Long>() {
		@Override
		protected Date createNonNull(Long value) {
			return new Date(value);
		}
	};

	public static final Factory<Long, Date> TO_LONG = new Factory.Base<Long, Date>() {
		@Override
		protected Long createNonNull(Date value) {
			return value.getTime();
		}
	};

	/**
	 * Converts date to string format yyyy-MM-dd.
	 */
	public static final Factory<String, Date> TO_STRING = Factories.threadSafe(toString(
		DEFAULT_DATE_FORMAT, null));

	/**
	 * Converts date to given string date format.
	 */
	public static Factory<String, Date> toString(String format) {
		return toString(format, null);
	}
	
	/**
	 * Converts date to given string date format in time zone.
	 */
	public static Factory<String, Date> toString(String format, TimeZone timeZone) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		if (timeZone != null) dateFormat.setTimeZone(timeZone);
		return new Factory.Base<String, Date>() {
			@Override
			protected String createNonNull(Date from) {
				return dateFormat.format(from);
			}
		};
	}

	/**
	 * Converts string in format yyyy-MM-dd to date. 
	 */
	public static final Factory<Date, String> FROM_STRING = Factories.threadSafe(fromString(
		DEFAULT_DATE_FORMAT, null));

	/**
	 * Converts string in given date format to date. 
	 */
	public static Factory<Date, String> fromString(String format) {
		return fromString(format, null);
	}
	
	/**
	 * Converts string in given date format and time zone to date. 
	 */
	public static Factory<Date, String> fromString(String format, TimeZone timeZone) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		if (timeZone != null) dateFormat.setTimeZone(timeZone);
		dateFormat.setLenient(true);
		return new Factory.Base<Date, String>() {
			@Override
			protected Date createNonNull(String from) {
				try {
					return dateFormat.parse(from.trim());
				} catch (ParseException e) {
					throw new FactoryException(e);
				}
			}
		};
	}

}
