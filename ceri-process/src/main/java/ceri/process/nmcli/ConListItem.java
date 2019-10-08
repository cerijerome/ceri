package ceri.process.nmcli;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import ceri.common.collection.CollectionUtil;
import ceri.common.date.ImmutableDate;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.log.process.parse.Columns;

/**
 * One line from nmcli con list
 * 
 * <pre>
 * NAME                  UUID            TYPE              TIMESTAMP-REAL                    
 * lon-c01               8X-4X-4X-12X    vpn               Mon 21 Dec 2015 02:06:57 PM PST   
 * Wired connection 1    8X-4X-4X-12X    802-3-ethernet    Mon 21 Dec 2015 02:06:57 PM PST
 * </pre>
 */
public class ConListItem {
	private static final String NAME_COLUMN = "NAME";
	private static final String UUID_COLUMN = "UUID";
	private static final String TYPE_COLUMN = "TYPE";
	private static final String TIMESTAMP_COLUMN = "TIMESTAMP-REAL";
	private static final String TIMESTAMP_NEVER = "never";
	private static final String DATE_FORMAT = "E d MMM y hh:mm:ss a z";
	public final String name;
	public final String uuid;
	public final String type;
	public final Date timestampReal;

	public static List<ConListItem> fromOutput(String output) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		List<Map<String, String>> lines = Columns.parseOutputWithHeader(output);
		return CollectionUtil.toList(map -> fromNameValues(dateFormat, map), lines);
	}

	private static ConListItem fromNameValues(DateFormat dateFormat, Map<String, String> map) {
		String name = map.get(NAME_COLUMN);
		String uuid = map.get(UUID_COLUMN);
		String type = map.get(TYPE_COLUMN);
		Date timestamp = parseTimestamp(dateFormat, map.get(TIMESTAMP_COLUMN));
		return new ConListItem(name, uuid, type, timestamp);
	}

	private static Date parseTimestamp(DateFormat dateFormat, String value) {
		if (value == null || TIMESTAMP_NEVER.equalsIgnoreCase(value)) return null;
		try {
			return dateFormat.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

	public ConListItem(String name, String uuid, String type, Date timestampReal) {
		this.name = name;
		this.uuid = uuid;
		this.type = type;
		this.timestampReal = ImmutableDate.create(timestampReal);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, uuid, type, timestampReal);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConListItem)) return false;
		ConListItem other = (ConListItem) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(uuid, other.uuid)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		if (!EqualsUtil.equals(timestampReal, other.timestampReal)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, uuid, type, timestampReal).toString();
	}

}
