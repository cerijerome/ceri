package ceri.common.net;

import org.junit.Test;
import ceri.common.test.Assert;

public class UrlsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Urls.class);
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
