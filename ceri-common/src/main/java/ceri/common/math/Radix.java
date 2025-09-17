package ceri.common.math;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ceri.common.collection.Enums;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.collection.Maps;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;
import ceri.common.util.Validate;

/**
 * Radix support.
 */
public enum Radix {
	NULL(0, new Digits(0, 0, 0, 0)),
	HEX(16, new Digits(2, 4, 8, 16), "0x", "0X", "#"),
	DEC(10, new Digits(3, 5, 10, 20)),
	OCT(8, new Digits(3, 6, 11, 22), "0"),
	BIN(2, new Digits(8, 16, 32, 64), "0b", "0B");

	private static final Map<String, Radix> MAP = map();
	public static final int MIN = Character.MIN_RADIX;
	public static final int MAX = Character.MAX_RADIX;
	/** Radix number. */
	public final int n;
	/** Text format prefix. */
	public final List<String> prefix;
	/** Maximum unsigned digits. */
	public final Digits digits;

	/**
	 * A radix and prefix.
	 */
	public record Prefix(Radix radix, String prefix) {
		public static final Prefix NULL = new Prefix(Radix.NULL, "");
		private static final int MAX = Streams.from(MAP.keySet()).mapToInt(String::length).max(0);

		/**
		 * Tries to find radix prefix at start of string. Must have at least one char after the
		 * prefix. Returns NULL if no match.
		 */
		public static Prefix find(CharSequence s) {
			return find(s, 0);
		}

		/**
		 * Tries to find radix prefix at string offset. Must have at least one char after the
		 * prefix. Returns NULL if no match.
		 */
		public static Prefix find(CharSequence s, int offset) {
			int len = Strings.length(s);
			offset = Maths.limit(offset, 0, len);
			for (int i = Math.min(Prefix.MAX, len - offset - 1); i > 0; i--) {
				var prefix = Strings.sub(s, offset, i);
				var radix = from(prefix);
				if (radix.isValid()) return new Prefix(radix, prefix);
				i = prefix.length();
			}
			return NULL;
		}

		/**
		 * Returns true if this prefix is valid.
		 */
		public boolean isValid() {
			return radix() != null && radix().isValid();
		}
	}

	/**
	 * Maximum unsigned digits by primitive data type.
	 */
	public record Digits(int ubyte, int ushort, int uint, int ulong) {}

	/**
	 * Validate radix is within java supported range.
	 */
	public static int validate(int radix) {
		Validate.validateRange(radix, MIN, MAX, "Radix");
		return radix;
	}

	/**
	 * Looks up radix from prefix. Returns NULL radix if not found.
	 */
	public static Radix from(String prefix) {
		if (prefix == null) return NULL;
		if (prefix.isEmpty()) return DEC;
		return MAP.getOrDefault(prefix, NULL);
	}

	private Radix(int n, Digits digits, String... prefix) {
		this.n = n;
		this.prefix = Immutable.wrap(Arrays.asList(prefix));
		this.digits = digits;
	}

	/**
	 * Returns the main prefix.
	 */
	public String prefix() {
		return Lists.at(prefix, 0, "");
	}

	/**
	 * Returns true if this radix is valid.
	 */
	public boolean isValid() {
		return n > 0;
	}

	private static Map<String, Radix> map() {
		var map = Maps.<String, Radix>of();
		for (var radix : Enums.of(Radix.class))
			for (var prefix : radix.prefix)
				map.put(prefix, radix);
		return Immutable.wrap(map);
	}
}