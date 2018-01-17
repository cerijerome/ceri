package ceri.common.code;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;

public class CollectionType {
	private static final Pattern COLLECTION_REGEX =
		Pattern.compile("^(List|Set|Collection)\\s*<(.*)>$");
	public final String type;
	public final String itemType;

	public static CollectionType of(String value) {
		Matcher m = RegexUtil.found(COLLECTION_REGEX, value);
		if (m == null) return null;
		return new CollectionType(m.group(1), m.group(2));
	}

	private CollectionType(String type, String itemType) {
		this.type = type;
		this.itemType = itemType;
	}

	public boolean isList() {
		return "List".equals(type);
	}
	
	public Class<?> typeClass() {
		if ("List".equals(type)) return List.class;
		if ("Set".equals(type)) return Set.class;
		if ("Collection".equals(type)) return Collection.class;
		throw new IllegalArgumentException("Unknown collection type: " + type);
	}

	@Override
	public String toString() {
		return String.format("%s<%s>", type, itemType);
	}

}
