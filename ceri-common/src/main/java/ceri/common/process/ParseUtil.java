package ceri.common.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.text.StringUtil;

public class ParseUtil {
	private static final int INVALID_INDEX = -1;
	private static final int NAME_VALUE_SPLIT_LIMIT = 2;

	private ParseUtil() {}

	public static Map<String, String> toMap(List<NameValue> nameValues) {
		return StreamUtil.toMap(nameValues.stream().filter(nv -> nv.name != null), nv -> nv.name,
			nv -> nv.value);
	}

	public static List<NameValue> fromMap(Map<String, String> nameValues) {
		return CollectionUtil.toList(NameValue::new, nameValues);
	}

	public static List<NameValue> parseNameValues(String splitPattern, String output) {
		return parseNameValues(Pattern.compile(splitPattern), output);
	}

	public static List<NameValue> parseNameValues(Pattern splitPattern, String output) {
		List<String> lines = lines(output);
		return CollectionUtil.toList(line -> parseNameValue(splitPattern, line), lines);
	}

	public static NameValue parseNameValue(String splitPattern, String line) {
		return parseNameValue(Pattern.compile(splitPattern), line);
	}

	public static NameValue parseNameValue(Pattern splitPattern, String line) {
		String[] split = splitPattern.split(line, NAME_VALUE_SPLIT_LIMIT);
		String name = split.length > 0 ? split[0] : null;
		String value = split.length > 1 ? split[1] : null;
		return new NameValue(name, value);
	}

	public static List<String> lines(String output) {
		return Arrays.asList(StringUtil.NEWLINE_REGEX.split(output));
	}

	public static List<String> parseValues(String line, Pattern... patterns) {
		return parseValues(line, Arrays.asList(patterns));
	}

	public static List<String> parseValues(String line, Collection<Pattern> patterns) {
		int i = 0;
		List<String> values = new ArrayList<>();
		for (Pattern pattern : patterns) {
			i = parseValue(line, i, pattern, values);
			if (i == INVALID_INDEX || i >= line.length()) break;
		}
		return values;
	}

	private static int parseValue(String line, int i, Pattern pattern, List<String> values) {
		Matcher m = pattern.matcher(line);
		if (!m.find(i)) return INVALID_INDEX;
		values.add(m.group().trim());
		return m.end();
	}

}
