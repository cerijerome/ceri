package ceri.ent.web;

public class SampleHeader {
	public static final SampleHeader Safari_MacOSX =
		new SampleHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
			+ "AppleWebKit/600.3.18 (KHTML, like Gecko) Version/8.0.3 Safari/600.3.18",
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", "gzip, deflate",
			"en-us");
	public static final SampleHeader Chrome_MacOSX = new SampleHeader(
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
			+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36",
		"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"gzip, deflate, sdch", "en-US,en;q=0.8,es;q=0.6");
	public static final SampleHeader Firefox_MacOSX = new SampleHeader(
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0",
		"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", "en-US,en;q=0.5",
		"gzip, deflate");
	public final String userAgent;
	public final String accept;
	public final String acceptEncoding;
	public final String acceptLanguage;

	public SampleHeader(String userAgent, String accept, String acceptEncoding,
		String acceptLanguage) {
		this.userAgent = userAgent;
		this.accept = accept;
		this.acceptEncoding = acceptEncoding;
		this.acceptLanguage = acceptLanguage;
	}

}
