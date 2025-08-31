package ceri.common.net;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import ceri.common.text.Strings;

public class Urls {

	private Urls() {}
	
	/**
	 * Uses URLEncoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String encode(String s) {
		if (Strings.isEmpty(s)) return "";
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	/**
	 * Uses URLDecoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String decode(String s) {
		if (Strings.isEmpty(s)) return "";
		return URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

}
