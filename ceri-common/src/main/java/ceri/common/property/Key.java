package ceri.common.property;

import java.util.Objects;
import ceri.common.text.StringUtil;

/**
 * A string-based key with separator.
 */
public record Key(Separator separator, String value) {

	/**
	 * Creates a key with joined parts.
	 */
	public static Key of(Separator separator, String... parts) {
		Objects.requireNonNull(separator);
		Objects.requireNonNull(parts);
		return new Key(separator, separator.join(parts));
	}

	/**
	 * Creates a key with partially sanitized parts. Leading and trailing separators are removed
	 * from each part.
	 */
	public static Key chomped(Separator separator, String... parts) {
		Objects.requireNonNull(separator);
		Objects.requireNonNull(parts);
		return of(separator, separator.chomp(parts));
	}

	/**
	 * Creates a key with fully sanitized parts. Leading, trailing, and duplicate separators are
	 * removed from each part.
	 */
	public static Key normalized(Separator separator, String... parts) {
		Objects.requireNonNull(separator);
		Objects.requireNonNull(parts);
		return of(separator, separator.normalize(parts));
	}

	public Key {
		Objects.requireNonNull(separator);
		Objects.requireNonNull(value);
	}

	/**
	 * Returns true if the key is empty.
	 */
	public boolean isRoot() {
		return StringUtil.empty(value());
	}

	/**
	 * Joins non-empty sub-keys with the separator.
	 */
	public Key append(String... subs) {
		return key(separator().join(value(), subs));
	}

	/**
	 * Join non-empty strings with the separator, first removing any leading and trailing separators
	 * from each string.
	 */
	public Key chomp(String... subs) {
		return key(separator().chomp(value(), subs));
	}

	/**
	 * Join non-empty strings with the separator, first removing any leading, trailing or duplicate
	 * separators from each string.
	 */
	public Key normalize(String... subs) {
		return key(separator().normalize(value(), subs));
	}

	/**
	 * Join non-empty strings with a new separator, first removing any leading, trailing or
	 * duplicate separators from each string.
	 */
	public Key normalize(Separator separator, String... subs) {
		return key(separator().normalize(separator, value(), subs));
	}

	private Key key(String value) {
		if (value == value()) return this;
		return new Key(separator(), value);
	}
}
