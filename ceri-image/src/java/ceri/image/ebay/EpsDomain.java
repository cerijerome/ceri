package ceri.image.ebay;

import java.util.regex.Pattern;

public enum EpsDomain {
	ebayimg("http://[^\\.]+\\.ebayimg\\.com/.*");
	
	private final Pattern pattern;
	
	private EpsDomain(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}
	
	public boolean matches(String url) {
		return pattern.matcher(url).matches();
	}
	
	/**
	 * Tries to find a matching EPS domain for the url.
	 * Returns null if no match.
	 */
	public static EpsDomain domain(String url) {
		for (EpsDomain domain : values()) {
			if (domain.matches(url)) return domain;
		}
		return null;
	}
	
}
