package ceri.common.util;

import java.util.Locale;
import ceri.common.array.Array;
import ceri.common.text.Strings;

public class Locales {
	private Locales() {}

	/**
	 * Creates a locale object from a lang_COUNTRY_VARIANT string.
	 */
	public static Locale from(String localeStr) {
		if (Strings.isBlank(localeStr)) return Locale.ROOT;
		var split = localeStr.split("_", 3);
		int i = 0;
		return Locale.of(Array.at(split, i++, ""), Array.at(split, i++, ""),
			Array.at(split, i, ""));
	}

	/**
	 * Returns a locale with the last part removed as follows: lang_COUNTRY_VARIANT => lang_COUNTRY
	 * => lang => [empty] => [empty]
	 */
	public static Locale parentOf(Locale locale) {
		var lang = locale.getLanguage();
		var country = locale.getCountry();
		var variant = locale.getVariant();
		if (!Strings.isBlank(variant)) return Locale.of(lang, country);
		if (!Strings.isBlank(country)) return Locale.of(lang);
		return Locale.ROOT;
	}
}
