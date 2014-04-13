package ceri.common.property;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.StringUtil;

/**
 * Represents an immutable lookup key with '.' separator.
 */
public class Key {
	public static final Key NULL = new Key("");
	public static final char SEPARATOR = '.';
	private static final Pattern SEPARATOR_REGEX = Pattern.compile("\\.");
	public final String value;

	private Key(String value) {
		this.value = value;
	}
	
	public static Key create(String...parts) {
		return create(null, parts);
	}

	public static Key createWithPrefix(String prefix, String...parts) {
		return create(Key.create(prefix), parts);
	}

	public static Key create(Key key, String...parts) {
		String value = createValue(key, parts);
		if (value.isEmpty()) return NULL;
		return new Key(value);
	}

	private static String createValue(Key prefix, String...parts) {
		if (prefix != null && (parts == null || parts.length == 0)) return prefix.value;
		StringBuilder b = new StringBuilder();
		if (prefix != null && prefix != NULL) b.append(prefix);
		if (parts != null) for (String part : parts) {
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
	
	public Iterator<String> partIterator() {
		return asParts().iterator();
	}
	
	public List<String> asParts() {
		if (this == NULL) return Collections.emptyList();
		String[] parts = SEPARATOR_REGEX.split(value);
		return ArrayUtil.asList(parts);
	}
	
	public int parts() {
		return StringUtil.count(value, SEPARATOR) + 1;
	}

	public boolean hasParent() {
		return value.indexOf(SEPARATOR) != -1;
	}

	public Key parent() {
		int i = value.lastIndexOf(SEPARATOR);
		if (i == -1) return NULL;
		return new Key(value.substring(0, i));
	}

	public Key orphan() {
		int i = value.indexOf(SEPARATOR);
		if (i == -1 || i == value.length() - 1) return NULL;
		return new Key(value.substring(i + 1));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Key)) return false;
		Key other = (Key)obj;
		return EqualsUtil.equals(value, other.value);
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
