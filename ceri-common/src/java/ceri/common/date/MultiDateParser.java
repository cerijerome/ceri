package ceri.common.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import ceri.common.collection.CollectionUtil;

/**
 * Encapsulates a list of date format patterns for parsing dates.
 * Attempts parsing in the given order, so use the most stringent pattern first.
 * Not thread-safe due to DateFormat. 
 */
public class MultiDateParser {
	public final List<String> patterns;
	private final List<DateFormat> dateFormats;

	/**
	 * Constructs the parser with given patterns in default timezone.
	 */
	public MultiDateParser(String...patterns) {
		this(null, patterns);
	}
	
	/**
	 * Constructs the parser with given patterns in given timezone.
	 */
	public MultiDateParser(TimeZone tz, String...patterns) {
		List<DateFormat> dateFormats = new ArrayList<>();
		for (String format : patterns) {
			DateFormat dateFormat = new SimpleDateFormat(format);
			if (tz != null) dateFormat.setTimeZone(tz);
			dateFormats.add(dateFormat);
		}
		this.dateFormats = Collections.unmodifiableList(dateFormats);
		this.patterns = CollectionUtil.immutableList(patterns);
	}
	
	/**
	 * Tries each format in order until no parse exception is thrown.
	 */
	public Date parse(String dateString) throws ParseException {
		ParseException firstEx = null;
		for (DateFormat dateFormat : dateFormats) {
			try {
				return dateFormat.parse(dateString);
			} catch (ParseException e) {
				if (firstEx == null) firstEx = e;
			}
		}
		if (firstEx == null) throw new AssertionError("firstEx should not be null");
		throw firstEx;
	}

	/**
	 * Formats the given date using the first pattern in the list.
	 */
	public String format(Date date) {
		return format(date, 0);
	}

	/**
	 * Formats the given date using the pattern at given index.
	 */
	public String format(Date date, int index) {
		return dateFormats.get(index).format(date);
	}

}
