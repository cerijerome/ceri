package ceri.common.code;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;

public class MapType {
	private static final Pattern MAP_REGEX = Pattern.compile("^(Map)\\s*<(.*)>$");
	public final String type;
	public final String keyType;
	public final String valueType;

	public static MapType of(String value) {
		Matcher m = RegexUtil.found(MAP_REGEX, value);
		if (m == null) return null;
		String generics = m.group(2);
		int i = commaIndex(generics);
		if (i <= 0) return null;
		String keyType = generics.substring(0, i).trim();
		String valueType = generics.substring(i + 1).trim();
		return new MapType(m.group(1), keyType, valueType);
	}

	private MapType(String type, String genericKeyType, String genericValueType) {
		this.type = type;
		this.keyType = genericKeyType;
		this.valueType = genericValueType;
	}

	private static int commaIndex(String s) {
		int len = s.length();
		int openBrackets = 0;
		for (int i = 1; i < len - 1; i++) {
			char ch = s.charAt(i);
			if (openBrackets == 0 && ch == ',') return i;
			if (ch == '<') openBrackets++;
			if (ch == '>') openBrackets--;
		}
		return -1;
	}

	public Class<?> typeClass() {
		return Map.class;
	}

	@Override
	public String toString() {
		return String.format("%s<%s, %s>", type, keyType, valueType);
	}

}
