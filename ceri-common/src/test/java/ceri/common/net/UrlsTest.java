package ceri.common.net;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.test.Assert;

public class UrlsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Urls.class);
	}

	@Test
	public void testUrlBuilder() {
		Assert.string(Urls.builder("http://test").url(), "http://test");
		Assert.string(
			Urls.builder("test/q").param("a", 123).param("a").param("a", "").param("b b", "x x")
				.form(false).param("b b", "x x").param(null, "x").uri(),
			"test/q?a=123&a&a=&b+b=x+x&b%20b=x%20x");
	}

	@Test
	public void testUrlBuilderCharset() {
		Assert.string(Urls.builder("test").param("\u00e9", 1).charset(StandardCharsets.US_ASCII)
			.param("\u00e9", 2).string(), "test?%C3%A9=1&%3F=2");
	}

	@Test
	public void testUrl() {
		Urls.url("http://example.com");
		Urls.url("https://example");
		Assert.thrown(IllegalArgumentException.class, () -> Urls.url("https://"));
	}

	@Test
	public void testEncode() {
		Assert.equal(Urls.encode(null), "");
		Assert.equal(Urls.encode(""), "");
		Assert.equal(Urls.encode("a b&c"), "a+b%26c");
	}

	@Test
	public void testDecode() {
		Assert.equal(Urls.decode(null), "");
		Assert.equal(Urls.decode(""), "");
		Assert.equal(Urls.decode("a+b%26c"), "a b&c");
	}

	@Test
	public void testValidEmails() {
		Assert.yes(Urls.isEmail("a@y.zz"));
		Assert.yes(Urls.isEmail("a.b@y.zz"));
		Assert.yes(Urls.isEmail("a@x.y.zz"));
	}

	@Test
	public void testInvalidEmails() {
		Assert.no(Urls.isEmail("@zzz"));
		Assert.no(Urls.isEmail("yyy"));
		Assert.no(Urls.isEmail("yyy@"));
		Assert.no(Urls.isEmail("a@z"));
		Assert.no(Urls.isEmail("a@z."));
		Assert.no(Urls.isEmail("a@yy.z"));
		Assert.no(Urls.isEmail("a@y.z."));
		Assert.no(Urls.isEmail("a@x.y.z"));
	}
}
