package ceri.common.property;

import ceri.common.util.StringUtil;

public class Key {
	public static final Key NULL = new Key("");
	private static final char SEPARATOR = '.';
	public final String value;

	private Key(String value) {
		this.value = value;
	}
	
	public static Key create(String...parts) {
		return create(null, parts);
	}

	public static Key create(Key key, String...parts) {
		String value = createValue(key, parts);
		if (value.isEmpty()) return NULL;
		return new Key(value);
	}

	private static String createValue(Key prefix, String...parts) {
		if (prefix != null && parts.length == 0) return prefix.value;
		StringBuilder b = new StringBuilder();
		if (prefix != null && prefix != NULL) b.append(prefix);
		for (String part : parts) {
			if (part == null || part.isEmpty()) continue;
			if (b.length() > 0) b.append('.');
			b.append(part);
		}
		return b.toString();
	}
	
	@Override
	public Key clone() {
		return create(this);
	}

	public Key child(String...parts) {
		return create(this, parts);
	}
	
	public int parts() {
		return StringUtil.count(value, SEPARATOR);
	}

	public boolean hasParent() {
		return value.indexOf(SEPARATOR) != -1;
	}

	public Key parent() {
		int i = value.lastIndexOf(SEPARATOR);
		if (i == -1) return NULL;
		return new Key(value.substring(0, i));
	}

	@Override
	public boolean equals(Object obj) {
		return value.equals(obj);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}

}
