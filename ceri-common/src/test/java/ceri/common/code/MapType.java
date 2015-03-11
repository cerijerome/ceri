package ceri.common.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapType {
	private static final Pattern MAP_REGEX = Pattern.compile("(Map)\\s*<(.*),\\s*(.*)>");

	public final String type;
	public final String keyType;
	public final String valueType;

	private MapType(String type, String genericKeyType, String genericValueType) {
		this.type = type;
		this.keyType = genericKeyType;
		this.valueType = genericValueType;
	}

	public static MapType createFrom(String value) {
		Matcher m = MAP_REGEX.matcher(value);
		if (m.find()) return new MapType(m.group(1), m.group(2), m.group(3));
		return null;
	}

}
