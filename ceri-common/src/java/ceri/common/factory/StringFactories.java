package ceri.common.factory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StringFactories {
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final Factory<String, char[]> FROM_CHAR_ARRAY =
		new Factory.Base<String, char[]>() {
			@Override
			protected String createNonNull(char[] from) {
				return String.valueOf(from);
			}
		};

	public static final Factory<String, Object> FROM_OBJECT =
		new Factory.Base<String, Object>() {
			@Override
			protected String createNonNull(Object from) {
				return String.valueOf(from);
			}
		};

	public static final Factory<String, Date> FROM_DATE = Factories.threadSafe(fromDate(
		DEFAULT_DATE_FORMAT, null));

	public static Factory<String, Date> fromDate(String format) {
		return fromDate(format, null);
	}
	
	public static Factory<String, Date> fromDate(String format, TimeZone timeZone) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		if (timeZone != null) dateFormat.setTimeZone(timeZone);
		return new Factory.Base<String, Date>() {
			@Override
			protected String createNonNull(Date from) {
				return dateFormat.format(from);
			}
		};
	}

	public static final Factory<Date, String> TO_DATE = Factories.threadSafe(toDate(
		DEFAULT_DATE_FORMAT, null));

	public static Factory<Date, String> toDate(String format) {
		return toDate(format, null);
	}
	
	public static Factory<Date, String> toDate(String format, TimeZone timeZone) {
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

	public static final Factory<char[], String> TO_CHAR_ARRAY =
		new Factory.Base<char[], String>() {
			@Override
			protected char[] createNonNull(String from) {
				return from.toCharArray();
			}
		};

	public static final Factory<Boolean, String> TO_BOOLEAN =
		new Factory.Base<Boolean, String>() {
			@Override
			protected Boolean createNonNull(String from) {
				return Boolean.valueOf(from.trim());
			}
		};

	public static final Factory<Byte, String> TO_BYTE = new Factory.Base<Byte, String>() {
		@Override
		protected Byte createNonNull(String from) {
			return Byte.valueOf(from.trim());
		}
	};

	public static final Factory<Short, String> TO_SHORT =
		new Factory.Base<Short, String>() {
			@Override
			protected Short createNonNull(String from) {
				return Short.valueOf(from.trim());
			}
		};

	public static final Factory<Integer, String> TO_INT =
		new Factory.Base<Integer, String>() {
			@Override
			protected Integer createNonNull(String from) {
				return Integer.valueOf(from.trim());
			}
		};

	public static final Factory<Long, String> TO_LONG = new Factory.Base<Long, String>() {
		@Override
		protected Long createNonNull(String from) {
			return Long.valueOf(from.trim());
		}
	};

	public static final Factory<Float, String> TO_FLOAT =
		new Factory.Base<Float, String>() {
			@Override
			protected Float createNonNull(String from) {
				return Float.valueOf(from.trim());
			}
		};

	public static final Factory<Double, String> TO_DOUBLE =
		new Factory.Base<Double, String>() {
			@Override
			protected Double createNonNull(String from) {
				return Double.valueOf(from.trim());
			}
		};

}
