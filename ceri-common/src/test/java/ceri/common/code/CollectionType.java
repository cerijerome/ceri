package ceri.common.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionType {
	private static final Pattern COLLECTION_REGEX = Pattern
		.compile("(List|Set|Collection)\\s*<(.*)>");

	public final String type;
	public final String itemType;

	private CollectionType(String type, String itemType) {
		this.type = type;
		this.itemType = itemType;
	}

	public static CollectionType createFrom(String value) {
		Matcher m = COLLECTION_REGEX.matcher(value);
		if (m.find()) return new CollectionType(m.group(1), m.group(2));
		return null;
	}

}
