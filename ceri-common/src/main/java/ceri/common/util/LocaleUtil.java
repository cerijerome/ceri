package ceri.common.util;

import java.util.Locale;
import ceri.common.text.StringUtil;

public class LocaleUtil {

	private LocaleUtil() {}

	/**
	 * Creates a locale object from a lang_COUNTRY_VARIANT string.
	 */
	public static Locale fromString(String localeStr) {
		if (StringUtil.isBlank(localeStr)) return new Locale("");
		String[] split = localeStr.split("_", 3);
		String lang = split[0];
		String country = split.length > 1 ? split[1] : "";
		String variant = split.length > 2 ? split[2] : "";
		return new Locale(lang, country, variant);
	}

	/**
	 * Returns a locale with the last part removed as follows: lang_COUNTRY_VARIANT => lang_COUNTRY
	 * => lang => [empty] => [empty]
	 */
	public static Locale parentOf(Locale locale) {
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		if (!StringUtil.isBlank(variant)) return new Locale(lang, country);
		if (!StringUtil.isBlank(country)) return new Locale(lang);
		return Locale.ROOT;
	}

}
